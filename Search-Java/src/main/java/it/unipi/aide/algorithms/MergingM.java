package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.Compressor;
import it.unipi.aide.utils.FileManager;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MergingM
{
    private final String WORK_DIR_PATH;
    private final String PARTIALS_PATH;
    private final boolean COMPRESSION;
    private final int BLOCKS_COUNT;

    private CollectionInformation ci;

    long finalOffset = 0;
    long vFinalOffset = 0;

    private InvertedIndex invertedIndex = new InvertedIndex();

    public MergingM(String outputPath, boolean compression, int blocksCount)
    {
        this.WORK_DIR_PATH = outputPath;
        this.BLOCKS_COUNT = blocksCount;
        this.COMPRESSION = compression;

        System.out.println(String.format(
                "-----MERGING-----\nWORK_DIR_PATH = %s\nCOMPRESSION = %b\nBLOCKS_TO_COMPRESS = %d\n-----------------",
                WORK_DIR_PATH,
                COMPRESSION,
                BLOCKS_COUNT
        ));

        this.PARTIALS_PATH = WORK_DIR_PATH+"partial/";
        ci = new CollectionInformation(WORK_DIR_PATH);
    }

    /**
     * Merge partial blocks into one unique block
     */
    public void mergeBlocks(boolean debug)
    {
        long nTerms = 0;
        // Check if the directory with the blocks results exists
        if(FileManager.checkDir(PARTIALS_PATH))
        {
            // Create a channel for each block, both for docId, frequencies and vocabulary fragments
            FileChannel[] docIdFileChannel = new FileChannel[BLOCKS_COUNT];
            FileChannel[] frequenciesFileChannel = new FileChannel[BLOCKS_COUNT];
            FileChannel[] vocabulariesFileChannel = new FileChannel[BLOCKS_COUNT];

            // Create one offset for each block, both for docId, frequencies and vocabulary fragments
            long[] offsetDocId = new long[BLOCKS_COUNT];
            long[] offsetFrequency = new long[BLOCKS_COUNT];
            long[] offsetVocabulary = new long[BLOCKS_COUNT];

            long[] dimVocabularyFile = new long[BLOCKS_COUNT];

            TermInfo[] vocs = new TermInfo[BLOCKS_COUNT];

            String FdocPath = WORK_DIR_PATH + "docIDsBlock";
            String FfreqPath = WORK_DIR_PATH + "frequenciesBlock";
            String FvocPath = WORK_DIR_PATH + "vocabularyBlock";

            if(!FileManager.checkDir(FdocPath)) FileManager.createFile(FdocPath);
            if(!FileManager.checkDir(FfreqPath)) FileManager.createFile(FfreqPath);
            if(!FileManager.checkDir(FvocPath)) FileManager.createFile(FvocPath);

            try (
                    // Open FileChannels to each file
                    FileChannel finalDocIDChannel = (FileChannel) Files.newByteChannel(Paths.get(FdocPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    FileChannel finalFreqChannel = (FileChannel) Files.newByteChannel(Paths.get(FfreqPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    FileChannel finalVocChannel = (FileChannel) Files.newByteChannel(Paths.get(FvocPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    )
            {
                // INIT PHASE
                for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                {
                    String docPath = PARTIALS_PATH + "docIDsBlock-" + indexBlock;
                    String freqPath = PARTIALS_PATH + "frequenciesBlock-" + indexBlock;
                    String vocPath = PARTIALS_PATH + "vocabularyBlock-" + indexBlock;

                    // Open FileChannels to each file
                    docIdFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(docPath),
                            StandardOpenOption.READ);
                    frequenciesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                            StandardOpenOption.READ);
                    vocabulariesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                            StandardOpenOption.READ);

                    dimVocabularyFile[indexBlock] = vocabulariesFileChannel[indexBlock].size();

                    // Get first term for each block's vocabulary
                    vocs[indexBlock] = getNextVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
                    offsetVocabulary[indexBlock] += TermInfo.SIZE_PRE_MERGING;
                }

                // Until we have data to analyze
                while(true)
                {
                    // Get the smallest term along all vocabularies
                    String smallestTerm = getSmallestTerm(vocs);

                    // Create a TermInfo to merge those in different blocks
                    TermInfo finalTerm = new TermInfo(smallestTerm);
                    finalTerm.setOffset(finalOffset);

                    int finalFreq = 0;
                    int finalNPostings = 0;

                    List<byte[]> docsAcc = new ArrayList<>();
                    List<byte[]> freqAcc = new ArrayList<>();

                    // For each block...
                    for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                    {
                        // ...if current term is equal the smallest (and it's not null)...
                        if(vocs[indexBlock] != null && vocs[indexBlock].getTerm().equals(smallestTerm))
                        {
                            // Compression differs from no compression on different final offset
                            if(COMPRESSION){
                                // ...accumulates bytes from different blocks...
                                docsAcc.add(extractBytes(docIdFileChannel[indexBlock],offsetDocId[indexBlock],vocs[indexBlock].getNumPosting()));
                                freqAcc.add(extractBytes(frequenciesFileChannel[indexBlock],offsetFrequency[indexBlock],vocs[indexBlock].getNumPosting()));
                            }
                            else {
                                // ...transfer docs and freq into final buffer...
                                transferBytes(docIdFileChannel[indexBlock],
                                        offsetDocId[indexBlock],
                                        finalDocIDChannel,
                                        finalOffset,
                                        vocs[indexBlock].getNumPosting()
                                );
                                transferBytes(frequenciesFileChannel[indexBlock],
                                        offsetFrequency[indexBlock],
                                        finalFreqChannel,
                                        finalOffset,
                                        vocs[indexBlock].getNumPosting());

                                // ...update final offset, nPosting and frequency for that term as we merge blocks...
                                finalOffset += 4L * vocs[indexBlock].getNumPosting();
                            }

                            // NumPostings and TotalTermFrequency unchanged
                            finalNPostings += vocs[indexBlock].getNumPosting();
                            finalFreq += vocs[indexBlock].getTotalFrequency();

                            // Update the offsets for current block, unchanged if compression
                            offsetDocId[indexBlock] += 4L * vocs[indexBlock].getNumPosting();
                            offsetFrequency[indexBlock] += 4L * vocs[indexBlock].getNumPosting();
                            /*
                                If this block is finished, set its vocs to null and skip
                                 This happen because last time we extracted a term for this
                                 block, it was the last term in the list
                                Null is used as break condition
                             */
                            if (offsetVocabulary[indexBlock] >= dimVocabularyFile[indexBlock]) {
                                System.err.println("LOG:\t\tBlock #" + indexBlock + " exhausted.");
                                vocs[indexBlock] = null;
                                continue;
                            }
                            // Vocabulary shift
                            vocs[indexBlock] = getNextVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
                            offsetVocabulary[indexBlock] += TermInfo.SIZE_PRE_MERGING;

                        }
                    }

                    if(COMPRESSION)
                    {
                        // ... term ended, bytes are accumulated, compress the vectors ...
                        int totalBytesSummed = docsAcc.stream().mapToInt(vec -> vec.length).sum();
                        byte[] concatenatedDocsBytes = new byte[totalBytesSummed];
                        byte[] concatenatedFreqBytes = new byte[totalBytesSummed];

                        int os = 0;
                        for(byte[] v: docsAcc){
                            System.arraycopy(v,0, concatenatedDocsBytes, os, v.length);
                            os += v.length;
                        }
                        os = 0;
                        for(byte[] v: freqAcc){
                            System.arraycopy(v,0, concatenatedFreqBytes, os, v.length);
                            os += v.length;
                        }

                        byte[] compressedDocs = Compressor.VariableByteCompression(concatenatedDocsBytes);
                        byte[] compressedFreq = Compressor.UnaryCompression(concatenatedFreqBytes);

                    }

                    finalTerm.setNumPosting(finalNPostings);
                    finalTerm.setTotalFrequency(finalFreq);
                    // Vocabulary offset unchanged, finalTerm also
                    writeTermToDisk(finalVocChannel, finalTerm);

                    nTerms++;

                    if(nTerms % 100_000 == 0){
                        System.out.println(String.format("LOG:\t\t%d terms have been processed", nTerms));
                    }

                    /*
                        STOPPING CONDITION
                         if all the entry of vocs are null, we have finished
                         as we exhausted all blocks
                     */
                    boolean allNull = true;
                    for(TermInfo t: vocs)
                    {
                        // If at least one is not null, just continue
                        if(t != null )
                        {
                            allNull = false;
                            break;
                        }
                    }

                    if(allNull)
                        break;

                }

                // Delete temporary blocks
                FileManager.deleteDir(PARTIALS_PATH);
                System.out.println(String.format("LOG:\t\tTotal terms in the Lexicon is %d", nTerms));
                CollectionInformation.setTotalTerms(nTerms);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("ERR\t\tMerge error, directory " + PARTIALS_PATH + " doesn't exists!");
        }
    }

    /**
     * Write a TermInfo on the disk
     * @param finalVocChannel Vocabulary FileChannel
     * @param finalTerm Term to write
     * @throws IOException
     */
    private void writeTermToDisk(FileChannel finalVocChannel, TermInfo finalTerm) throws IOException
    {
        MappedByteBuffer tempBuffer = finalVocChannel.map(FileChannel.MapMode.READ_WRITE, vFinalOffset, TermInfo.SIZE_POST_MERGING);

        StringBuilder pattern = new StringBuilder("%-").append(TermInfo.SIZE_TERM).append("s");
        String paddedTerm = String.format(pattern.toString(), finalTerm.getTerm()).substring(0, TermInfo.SIZE_TERM); // Pad with spaces up to 64 characters

        // Write
        tempBuffer.put(paddedTerm.getBytes());
        tempBuffer.putInt(finalTerm.getTotalFrequency());
        tempBuffer.putLong(finalTerm.getOffset());
        tempBuffer.putLong(finalTerm.getBytesOccupiedDocid());
        tempBuffer.putLong(finalTerm.getBytesOccupiedFreq());
        tempBuffer.putInt(finalTerm.getNumPosting());

        vFinalOffset += TermInfo.SIZE_POST_MERGING;
    }

    /**
     * First term in lexicographic order between all terms
     * @param vocs TermInfo array to pick the terms from
     * @return Smallest term in lexicographic order
     */
    private String getSmallestTerm(TermInfo[] vocs)
    {
        String toRet = null;
        for(TermInfo t: vocs)
        {
            if(t == null) continue;
            if(toRet == null) toRet = t.getTerm();
            if(t.getTerm().compareTo(toRet) < 0) toRet = t.getTerm();
        }
        return toRet;
    }

    /**
     * Get next TermInfo from that channel
     * @param fileChannel FileChannel to retrieve the Term from
     * @param offsetVocabulary Offset at which the term is
     * @return Next TermInfo in line
     * @throws IOException
     */
    private TermInfo getNextVoc(FileChannel fileChannel, long offsetVocabulary) throws IOException
    {
        MappedByteBuffer tempBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, offsetVocabulary, TermInfo.SIZE_PRE_MERGING);

        byte[] termBytes = new byte[TermInfo.SIZE_TERM];
        tempBuffer.get(termBytes);

        int frequency = tempBuffer.getInt();
        long offset = tempBuffer.getLong();
        int nPosting = tempBuffer.getInt();

        String term = new String(termBytes).trim();

        return new TermInfo(term, frequency, offset, nPosting);
    }

    /**
     * Copy a window of bytes from one channel to another
     * @param fromChannel Source channel
     * @param fromOffset Source starting offset
     * @param toChannel Destination channel
     * @param toOffset Destination starting offset
     * @param nPostings How many elements (4 Bytes each) to copy
     * @throws IOException
     */
    private void transferBytes(FileChannel fromChannel, long fromOffset,
                               FileChannel toChannel, long toOffset, int nPostings) throws IOException
    {
        // Buffer from offset to offset + 4L*nPosting bytes
        MappedByteBuffer tempInBuff = fromChannel.map(FileChannel.MapMode.READ_ONLY,
                fromOffset, 4L*nPostings);
        MappedByteBuffer tempOutBuff = toChannel.map(FileChannel.MapMode.READ_WRITE,
                toOffset, 4L*nPostings);

        byte[] tempBytes = new byte[4 * nPostings];

        tempInBuff.get(tempBytes);
        tempOutBuff.put(tempBytes);
    }

    /**
     * Extract bytes from given channel at given offset and returns them
     *
     * @param fromChannel File Channel to extract Bytes from
     * @param offset At which offset to extract bytes
     * @param nPosting How many 4-bytes to extract
     * @return Array of Bytes extracted
     */
    private byte[] extractBytes(FileChannel fromChannel, long offset, int nPosting) throws IOException
    {
        // Buffer to extract bytes from
        MappedByteBuffer tempBuff = fromChannel.map(FileChannel.MapMode.READ_ONLY,
                offset, 4L*nPosting);
        // Where to place those bytes
        byte[] tempBytes = new byte[4 * nPosting];

        tempBuff.get(tempBytes);
        return tempBytes;
    }
}
