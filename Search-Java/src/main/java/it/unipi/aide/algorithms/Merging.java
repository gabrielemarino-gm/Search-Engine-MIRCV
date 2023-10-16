package it.unipi.aide.algorithms;

import it.unipi.aide.model.InvertedIndex;
import it.unipi.aide.model.Posting;
import it.unipi.aide.model.PostingList;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.FileManager;

import java.io.BufferedWriter;
import java.io.File;
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
    private final static String PARTIAL_PATH = "data/partial/";

    private InvertedIndex invertedIndex = new InvertedIndex();
    private String outputPath;

    public Merging(String outputPath)
    {
        this.outputPath = outputPath;
    }

    public void mergeBloks(int numFiles)
    {


        // Check if the directory with the blocks results exists
        if(FileManager.checkDir(PARTIAL_PATH))
        {
            // Create 4 array of considering the buffers for all the files in the path, for the docids and frequencies
            FileChannel[] docIdFileChannel = new FileChannel[numFiles];
            MappedByteBuffer[] docIdBuffers = new MappedByteBuffer[numFiles];

            FileChannel[] frequenciesFileChannel = new FileChannel[numFiles];
            MappedByteBuffer[] frequenciesBuffers = new MappedByteBuffer[numFiles];

            FileChannel[] vocabulariesFileChannel = new FileChannel[numFiles];
            MappedByteBuffer[] vocabulariesBuffers = new MappedByteBuffer[numFiles];


            long[] offsetDocId = new long[numFiles];
            long[] offsetFrequency = new long[numFiles];
            long[] offsetVocabulary = new long[numFiles];

            long[] dimBlockIndexFile = new long[numFiles];
            boolean[] stoppingCondition = new boolean[numFiles];

            TermInfo[] termsToMerge = new TermInfo[numFiles];
            PostingList[] postingList = new PostingList[numFiles];
            TreeMap<String, Integer> mapOfTerm = new TreeMap<>();


            try
            {
                // Until we have data to analyze
                while(true)
                {
                    // initialize the FileChannel of all file needed
                    for (int indexBlock = 0; indexBlock < numFiles; indexBlock++)
                    {
                        // System.out.println("DBG: Block-" + indexBlock);
                        docIdFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(PARTIAL_PATH + "docIDsBlock-" + indexBlock),
                                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                        frequenciesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(PARTIAL_PATH + "frequenciesBlock-" + indexBlock),
                                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                        vocabulariesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(PARTIAL_PATH + "vocabularyBlock-" + indexBlock),
                                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                        dimBlockIndexFile[indexBlock] = docIdFileChannel[indexBlock].size();
                    }

                    System.out.println();

                    // for each block, initialize all the data structure of a term needed
                    for (int indexBlock = 0; indexBlock < numFiles; indexBlock++)
                    {
                        // Check if we arrived at the end of the file of the current block
                        if (offsetDocId[indexBlock] >= dimBlockIndexFile[indexBlock])
                        {
                            // If we finish to elaborate the file, just go to the next iteration
                            stoppingCondition[indexBlock] = true;
                            continue;
                        }

                        // System.out.println("DBG:     block #" + indexBlock);
                        // System.out.println("DBG:     offset docID = " + offsetDocId[indexBlock]);
                        // System.out.println("DBG:     offset freq  = " + offsetFrequency[indexBlock]);
                        // System.out.println("DBG:     offset vocab = " + offsetVocabulary[indexBlock]);

                        // Stating to read inside the vocabulary the length of the first posting list
                        vocabulariesBuffers[indexBlock] = vocabulariesFileChannel[indexBlock].map(FileChannel.MapMode.READ_WRITE, offsetVocabulary[indexBlock], 64L + 4L + 4L + 4L);

                        byte[] termBytes = new byte[64];
                        vocabulariesBuffers[indexBlock].get(termBytes);
                        int frequency = vocabulariesBuffers[indexBlock].getInt();
                        int offset = vocabulariesBuffers[indexBlock].getInt();
                        int nPosting = vocabulariesBuffers[indexBlock].getInt();

                        String term = new String(termBytes).trim();
                        System.out.println("DBG:    TermInfo = " + term + " <" + frequency + ", " + offset + ", " + nPosting + ">");

                        // I need to take in account the term that occur in each step, looping in the vocabulary of each block.
                        if (mapOfTerm.get(term) != null)
                        {
                            mapOfTerm.compute(term, (key, oldValue) -> oldValue + 1);
                            // System.out.println("DBG:    Term not null: " + term + ", " + mapOfTerm.get(term));
                        }
                        else
                        {
                            mapOfTerm.put(term, 1);
                            // System.out.println("DBG:    Term null: " + term + ", " + mapOfTerm.get(term));
                        }

                        termsToMerge[indexBlock] = new TermInfo(frequency, offset, nPosting);
                        termsToMerge[indexBlock].setTerm(term);
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
                            // System.out.println("DBG: <" + d + ", " + f + ">");
                        }

                        System.out.println("DBG:   " + postingList[indexBlock]);
                    }

                    // We need to retrieve the term lexicographically minor, because I'll merge that term

                    String minorTerm = mapOfTerm.firstKey();
                    PostingList mergePostingList = new PostingList(minorTerm);

                    System.out.println("DBG:    termsToMerge[0] =" + termsToMerge[0]);
                    System.out.println("DBG:    termsToMerge[1] =" + termsToMerge[1]);
                    System.out.println("DBG:    termsToMerge[2] =" + termsToMerge[2]);

                    for (int indexBlock = 0; indexBlock < numFiles; indexBlock++)
                    {
                        // if the posting list it's inherent ate the minor term, merge it!
                        if (termsToMerge[indexBlock].getTerm().equals(minorTerm))
                        {
                            // Add all the posting. The method sort those postings for DocID.
                            mergePostingList.addPostingList(postingList[indexBlock]);

                            // Now we need to update the buffer offset of just the blocks merged
                            offsetDocId[indexBlock] += 4;
                            offsetFrequency[indexBlock] += 4;
                            offsetVocabulary[indexBlock] += 76;
                        }
                    }

                    // Now just add the merged posting list to the global inverted index
                    invertedIndex.addPostingList(minorTerm, mergePostingList);

                    // STOPPING CONDITION. If all the element of the boolean array are true then stop the while loop
                    BitSet bitSet = new BitSet();
                    for (int i = 0; i < stoppingCondition.length; i++)
                        bitSet.set(i, stoppingCondition[i]);

                    if (bitSet.cardinality() == stoppingCondition.length)
                        break;

                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            writeIndex(true);
        }
        else
        {
            System.err.println("ERR:    Merge error, directory " + PARTIAL_PATH + " doesn't exists!");
        }
    }

    private void writeIndex(boolean debug)
    {
        if (debug)
        {
            FileManager.createDir(outputPath + "debug");
            try
            {
                // Write inverted index to debug text file
                BufferedWriter indexWriter = new BufferedWriter(
                        new FileWriter(outputPath + "debug/invertedIndex.txt")
                );
                indexWriter.write(invertedIndex.toString());
                indexWriter.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
