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

    long finalOffset = 0;
    long vFinalOffset = 0;

    private InvertedIndex invertedIndex = new InvertedIndex();

    public MergingM(String outputPath)
    {
        this.INPUT_PATH = outputPath+ "partial/";
        this.OUTPUT_PATH = outputPath + "complete/";
    }

    /**
     * Merge partial blocks into one unique block
     * @param numFiles How many partial blocks there are
     */
    public void mergeBlocks(int numFiles)
    {
        // Check if the directory with the blocks results exists
        if(FileManager.checkDir(INPUT_PATH))
        {
            // Create a channel for each block, both for docId, frequencies and vocabulary fragments
            FileChannel[] docIdFileChannel = new FileChannel[numFiles];
            FileChannel[] frequenciesFileChannel = new FileChannel[numFiles];
            FileChannel[] vocabulariesFileChannel = new FileChannel[numFiles];

            // Create one offset for each block, both for docId, frequencies and vocabulary fragments
            long[] offsetDocId = new long[numFiles];
            long[] offsetFrequency = new long[numFiles];
            long[] offsetVocabulary = new long[numFiles];

            long[] dimVocabularyFile = new long[numFiles];

            TermInfo[] vocs = new TermInfo[numFiles];

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
                for (int indexBlock = 0; indexBlock < numFiles; indexBlock++)
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

                    TermInfo finalTerm = new TermInfo(smallestTerm);
                    finalTerm.setOffset(finalOffset);

                    int finalFreq = 0;
                    int finalNPostings = 0;

                    // for each block, initialize all the data structure of a term needed
                    for (int indexBlock = 0; indexBlock < numFiles; indexBlock++)
                    {
                        // If current term is equal the smallest, transfer docs and freq into final buffer
                        if(vocs[indexBlock] != null && vocs[indexBlock].getTerm().equals(smallestTerm))
                        {
                            // Trasfering docIDs
                            transferBytes(docIdFileChannel[indexBlock], offsetDocId[indexBlock],
                                    finalDocIDChannel, finalOffset, vocs[indexBlock].getNumPosting());

                            // Transfering Frequencies
                            transferBytes(frequenciesFileChannel[indexBlock], offsetFrequency[indexBlock],
                                    finalFreqChannel, finalOffset, vocs[indexBlock].getNumPosting());

                            // Update the offset of the final file that contain the final inverted index
                            finalOffset += 4L * vocs[indexBlock].getNumPosting();

                            // Update the offset for the partial files
                            offsetDocId[indexBlock] += 4L * vocs[indexBlock].getNumPosting();
                            offsetFrequency[indexBlock] += 4L * vocs[indexBlock].getNumPosting();

                            // Update the number of posting and the total frequency of that term
                            finalNPostings += vocs[indexBlock].getNumPosting();
                            finalFreq += vocs[indexBlock].getTotalFrequency();

                            // if this block is finished, set its vocs to null and skip
                            if(offsetVocabulary[indexBlock] >= dimVocabularyFile[indexBlock])
                            {
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

                    // STOPPING CONDITION
                    // if all the entry of vocs are null, we have finished
                    boolean allNull = true;
                    for(TermInfo t: vocs)
                    {
                        if (t != null )
                            allNull = false;

                        break;
                    }

                    if(allNull)
                        break;
                }
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

    private void transferBytes(FileChannel fromChannel, long fromOffset,
                               FileChannel toChannel, long toOffset, int nPostings) throws IOException
    {
        // Buffer from offset for 4L*nPosting bytes
        MappedByteBuffer tempInBuff = fromChannel.map(FileChannel.MapMode.READ_WRITE,
                fromOffset, 4L*nPostings);

        byte[] tempBytes = new byte[4 * nPostings];
        tempInBuff.get(tempBytes);

        MappedByteBuffer tempOutBuff = toChannel.map(FileChannel.MapMode.READ_WRITE,
                toOffset, 4L*nPostings);

        tempOutBuff.put(tempBytes);
    }
}
