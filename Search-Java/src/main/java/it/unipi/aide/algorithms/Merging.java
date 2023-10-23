package it.unipi.aide.algorithms;

import it.unipi.aide.model.InvertedIndex;
import it.unipi.aide.model.Posting;
import it.unipi.aide.model.PostingList;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.FileManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Merging
{
    private final String INPUT_PATH;
    private final String OUTPUT_PATH;
    private final boolean COMPRESSION;
    private final int numFiles;
    private long partialOffset;

    private InvertedIndex invertedIndex = new InvertedIndex();

    // Write in binary
    private String docPath;
    private String freqPath;
    private String vocPath;

    public Merging(String outputPath, boolean compression, int numFiles)
    {
        this.INPUT_PATH = outputPath+ "partial/";
        this.OUTPUT_PATH = outputPath + "complete/";
        this.numFiles = numFiles;
        this.COMPRESSION = compression;
        this.partialOffset = 0;

        this.docPath = OUTPUT_PATH+"docIDsBlock";
        this.freqPath = OUTPUT_PATH+"frequenciesBlock";
        this.vocPath = OUTPUT_PATH+"vocabularyBlock";
    }

    /**
     * Merge partial blocks into one unique block
     */
    public void mergeBlocks(boolean debug)
    {
        chechDir(true);

        // Check if the directory with the blocks results exists
        if(FileManager.checkDir(INPUT_PATH))
        {
            // Create a channel for each block, both for docId, frequencies and vocabulary fragments
            FileChannel[] docIdFileChannel = new FileChannel[numFiles];
            MappedByteBuffer[] docIdBuffers = new MappedByteBuffer[numFiles];

            FileChannel[] frequenciesFileChannel = new FileChannel[numFiles];
            MappedByteBuffer[] frequenciesBuffers = new MappedByteBuffer[numFiles];

            FileChannel[] vocabulariesFileChannel = new FileChannel[numFiles];
            MappedByteBuffer[] vocabulariesBuffers = new MappedByteBuffer[numFiles];

            // Create one offset for each block, both for docId, frequencies and vocabulary fragments
            long[] offsetDocId = new long[numFiles];
            long[] offsetFrequency = new long[numFiles];
            long[] offsetVocabulary = new long[numFiles];

            // Unknown
            long[] dimVocabularyFile = new long[numFiles];
            boolean[] stoppingCondition = new boolean[numFiles];

            TermInfo[] termsToMerge = new TermInfo[numFiles];
            PostingList[] postingList = new PostingList[numFiles];
            TreeMap<String, Integer> mapOfTerm = new TreeMap<>();
            // Unknown

            int nTerms = 0;
            try
            {
                // initialize the FileChannel of all file needed
                for (int indexBlock = 0; indexBlock < numFiles; indexBlock++)
                {
                    String docPath = INPUT_PATH + "docIDsBlock-" + indexBlock;
                    String freqPath = INPUT_PATH + "frequenciesBlock-" + indexBlock;
                    String vocPath = INPUT_PATH + "vocabularyBlock-" + indexBlock;

                    docIdFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(docPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                    frequenciesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                    vocabulariesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                    // Maybe -> Maybe use the vocabulary as it brings more info?
                    dimVocabularyFile[indexBlock] = vocabulariesFileChannel[indexBlock].size();
                }

                // Until we have data to analyze
                while(true)
                {
                    // for each block, initialize all the data structure of a term needed
                    for (int indexBlock = 0; indexBlock < numFiles; indexBlock++)
                    {
                        // Check if we arrived at the end of the file of the current block
                        if (offsetVocabulary[indexBlock] >= dimVocabularyFile[indexBlock])
                        {
                            // If we finish to elaborate the file, just go to the next iteration
                            stoppingCondition[indexBlock] = true;
                            continue;
                        }

                        // Stating to read inside the vocabulary the length of the first posting list
                        vocabulariesBuffers[indexBlock] = vocabulariesFileChannel[indexBlock].map(FileChannel.MapMode.READ_WRITE,
                                offsetVocabulary[indexBlock], 64L + 4L + 8L + 4L);

                        byte[] termBytes = new byte[64];
                        vocabulariesBuffers[indexBlock].get(termBytes);
                        int frequency = vocabulariesBuffers[indexBlock].getInt();
                        long offset = vocabulariesBuffers[indexBlock].getLong();
                        int nPosting = vocabulariesBuffers[indexBlock].getInt();

                        String term = new String(termBytes).trim();

                        // I need to take in account the term that occur in each step, looping in the vocabulary of each block.
                        if (mapOfTerm.get(term) != null)
                        {
                            mapOfTerm.compute(term, (key, oldValue) -> oldValue + 1);
                        }
                        else
                        {
                            mapOfTerm.put(term, 1);
                        }

                        termsToMerge[indexBlock] = new TermInfo(term, frequency, offset, nPosting);
                        postingList[indexBlock] = new PostingList(new String(termBytes).trim());

                        // Need to read nPosting integers, i.e. the entire posting list for that term
                        docIdBuffers[indexBlock] = docIdFileChannel[indexBlock].map(FileChannel.MapMode.READ_WRITE, offsetDocId[indexBlock], 4L * nPosting);
                        frequenciesBuffers[indexBlock] = frequenciesFileChannel[indexBlock].map(FileChannel.MapMode.READ_WRITE, offsetFrequency[indexBlock], 4L * nPosting);

                        // Fill the posting list
                        postingList[indexBlock] = new PostingList(term);
                        for (int j = 0; j < nPosting; j++)
                        {
                            int d = docIdBuffers[indexBlock].getInt();
                            int f = frequenciesBuffers[indexBlock].getInt();
                            postingList[indexBlock].addPosting(new Posting(d, f));
                        }
                    }

                    // STOPPING CONDITION. If all the element of the boolean array are true then stop the while loop
                    BitSet bitSet = new BitSet();
                    for (int i = 0; i < stoppingCondition.length; i++)
                        bitSet.set(i, stoppingCondition[i]);

                    if (bitSet.cardinality() == stoppingCondition.length)
                        break;

                    // We need to retrieve the term lexicographically minor, because I'll merge that term
                    String minorTerm = mapOfTerm.firstKey();
                    PostingList mergePostingList = new PostingList(minorTerm);

                    for (int indexBlock = 0; indexBlock < numFiles; indexBlock++)
                    {
                        // if the posting list it's inherent at the minor term, merge it!
                        if (termsToMerge[indexBlock].getTerm().equals(minorTerm))
                        {
                            // Add all the posting. The method sort those postings for DocID.
                            mergePostingList.addPostingList(postingList[indexBlock]);

                            // Now we need to update the buffer offset of just the blocks merged
                            offsetDocId[indexBlock] += 4L * termsToMerge[indexBlock].getNumPosting();
                            offsetFrequency[indexBlock] += 4L * termsToMerge[indexBlock].getNumPosting();
                            offsetVocabulary[indexBlock] += 64 + 4 + 8 + 4;

                            nTerms++;
                        }
                    }

                    // Now just add the merged posting list to the global inverted index
                    invertedIndex.addPostingList(minorTerm, mergePostingList);

                    // Increment the number of total posting into the inverted index
                    writePostingList(minorTerm, invertedIndex.getPostingList(minorTerm), true);


                    // Clean data structure
                    // delete the term merged from the mapOfTerm
                    mapOfTerm.remove(minorTerm);
                    if (nTerms%1000000 == 0)
                        System.out.println("LOG:    Processed " + nTerms + " terms");
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            FileManager.cleanFolder(INPUT_PATH);

        }
        else
        {
            System.err.println("ERR:    Merge error, directory " + INPUT_PATH + " doesn't exists!");
        }
    }

    private void chechDir(boolean debug)
    {
        if(!FileManager.checkFile(docPath)) FileManager.createFile(docPath);
        if(!FileManager.checkFile(freqPath)) FileManager.createFile(freqPath);
        if(!FileManager.createFile(vocPath)) FileManager.createFile(vocPath);

        if (debug)
        {
            FileManager.createDir(OUTPUT_PATH + "debug/");
            FileManager.cleanFolder(OUTPUT_PATH + "debug/");
            FileManager.createFile(OUTPUT_PATH + "debug/invertedIndex.txt");
        }
    }

    private void writePostingList(String term, List<Posting> postingList, boolean debug)
    {
        int numPosting = postingList.size();

        try (
                // Open a FileChannel for docId, frequencies and vocabulary fragments
                FileChannel docIdFileChannel = (FileChannel) Files.newByteChannel(Paths.get(docPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                FileChannel frequencyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)

                // FileChannel vocabularyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                //        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)
        )
        {
            // Create the buffer where write the streams of bytes
            MappedByteBuffer docIdBuffer = docIdFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, numPosting*4L);
            MappedByteBuffer frequencyBuffer = frequencyFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, numPosting*4L);

            // Write that all Posting to the disk
            for (Posting p: postingList)
            {
                docIdBuffer.putInt(p.getDocId());
                frequencyBuffer.putInt(p.getFrequency());
                partialOffset += 4L;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (debug)
        {
            try
            {
                // Write inverted index to debug text file
                BufferedWriter indexWriter = new BufferedWriter(
                        new FileWriter(OUTPUT_PATH + "debug/invertedIndex.txt", true)
                );

                indexWriter.write(term+ ": ");
                for (Posting p: postingList)
                {
                    indexWriter.write(postingList.toString());
                }

                indexWriter.write("\n");
                indexWriter.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
