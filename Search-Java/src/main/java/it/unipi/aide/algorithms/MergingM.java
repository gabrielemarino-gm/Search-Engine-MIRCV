package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.FileManager;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MergingM
{
    private final String INPUT_PATH;
    private final String OUTPUT_PATH;
    private final boolean COMPRESSION;
    private final int BLOCKS_COUNT;

    long finalOffset = 0;
    long vFinalOffset = 0;

    private InvertedIndex invertedIndex = new InvertedIndex();

    public MergingM(String outputPath, boolean compression, int blocksCount)
    {
        this.INPUT_PATH = outputPath+ "partial/";
        this.OUTPUT_PATH = outputPath + "complete/";
        this.BLOCKS_COUNT = blocksCount;
        this.COMPRESSION = compression;
    }

    /**
     * Merge partial blocks into one unique block
     */
    public void mergeBlocks(boolean debug)
    {
        // Check if the directory with the blocks results exists
        if(FileManager.checkDir(INPUT_PATH))
        {
            FileManager.cleanFolder(OUTPUT_PATH);

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

            try
            {
                String FdocPath = OUTPUT_PATH + "docIDsBlock";
                String FfreqPath = OUTPUT_PATH + "frequenciesBlock";
                String FvocPath = OUTPUT_PATH + "vocabularyBlock";

                if(!FileManager.checkDir(FdocPath)) FileManager.createFile(FdocPath);
                if(!FileManager.checkDir(FfreqPath)) FileManager.createFile(FfreqPath);
                if(!FileManager.checkDir(FvocPath)) FileManager.createFile(FvocPath);

                // Open FileChannels to each file
                FileChannel finalDocIDChannel = (FileChannel) Files.newByteChannel(Paths.get(FdocPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                FileChannel finalFreqChannel = (FileChannel) Files.newByteChannel(Paths.get(FfreqPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                FileChannel finalVocChannel = (FileChannel) Files.newByteChannel(Paths.get(FvocPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                // INIT PHASE
                for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                {
                    String docPath = INPUT_PATH + "docIDsBlock-" + indexBlock;
                    String freqPath = INPUT_PATH + "frequenciesBlock-" + indexBlock;
                    String vocPath = INPUT_PATH + "vocabularyBlock-" + indexBlock;

                    // Open FileChannels to each file
                    docIdFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(docPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    frequenciesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    vocabulariesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                    dimVocabularyFile[indexBlock] = vocabulariesFileChannel[indexBlock].size();

                    // Get first term for each block's vocabulary
                    vocs[indexBlock] = getNextVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
                    offsetVocabulary[indexBlock] += TermInfo.SIZE;
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

                    // For each block...
                    for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                    {
                        // ...if current term is equal the smallest (and it's not null)...
                        if(vocs[indexBlock] != null && vocs[indexBlock].getTerm().equals(smallestTerm))
                        {
                            // ...transfer docs and freq into final buffer...
                            transferBytes(docIdFileChannel[indexBlock], offsetDocId[indexBlock],
                                    finalDocIDChannel, finalOffset, vocs[indexBlock].getNumPosting());
                            transferBytes(frequenciesFileChannel[indexBlock], offsetFrequency[indexBlock],
                                    finalFreqChannel, finalOffset, vocs[indexBlock].getNumPosting());

                            // ...update final offset, nPosting and frequency for that term as we merge blocks...
                            finalOffset += 4L * vocs[indexBlock].getNumPosting();
                            finalNPostings += vocs[indexBlock].getNumPosting();
                            finalFreq += vocs[indexBlock].getTotalFrequency();


                            // Update the offsets for current block
                            offsetDocId[indexBlock] += 4L * vocs[indexBlock].getNumPosting();
                            offsetFrequency[indexBlock] += 4L * vocs[indexBlock].getNumPosting();

                            /*
                                If this block is finished, set its vocs to null and skip
                                 This happen because last time we extracted a term for this
                                 block, it was the last term in the list
                                Null is used as break condition
                             */
                            if(offsetVocabulary[indexBlock] >= dimVocabularyFile[indexBlock])
                            {
                                System.err.println("LOG:    Block #" + indexBlock + " exhausted.");
                                vocs[indexBlock] = null;
                                continue;
                            }

                            vocs[indexBlock] = getNextVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
                            offsetVocabulary[indexBlock] += TermInfo.SIZE;
                        }
                    }

                    finalTerm.setNumPosting(finalNPostings);
                    finalTerm.setTotalFrequency(finalFreq);

                    writeTermToDisk(finalVocChannel, finalTerm);

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
                FileManager.cleanFolder(INPUT_PATH);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("ERR:    Merge error, directory " + INPUT_PATH + " doesn't exists!");
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
        MappedByteBuffer tempBuffer = finalVocChannel.map(FileChannel.MapMode.READ_WRITE, vFinalOffset, TermInfo.SIZE);

        String paddedTerm = String.format("%-64s", finalTerm.getTerm()).substring(0, 64); // Pad with spaces up to 64 characters

        // Write
        tempBuffer.put(paddedTerm.getBytes());
        tempBuffer.putInt(finalTerm.getTotalFrequency());
        tempBuffer.putLong(finalTerm.getOffset());
        tempBuffer.putInt(finalTerm.getNumPosting());

        vFinalOffset += TermInfo.SIZE;
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
        MappedByteBuffer tempBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offsetVocabulary, TermInfo.SIZE);

        byte[] termBytes = new byte[64];
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
        MappedByteBuffer tempInBuff = fromChannel.map(FileChannel.MapMode.READ_WRITE,
                fromOffset, 4L*nPostings);
        MappedByteBuffer tempOutBuff = toChannel.map(FileChannel.MapMode.READ_WRITE,
                toOffset, 4L*nPostings);

        byte[] tempBytes = new byte[4 * nPostings];

        tempInBuff.get(tempBytes);
        tempOutBuff.put(tempBytes);
    }
}
