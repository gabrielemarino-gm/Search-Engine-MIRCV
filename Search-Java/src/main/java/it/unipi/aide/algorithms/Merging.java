package it.unipi.aide.algorithms;

import it.unipi.aide.model.InvertedIndex;
import it.unipi.aide.model.Posting;
import it.unipi.aide.model.PostingList;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.FileManager;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Merging
{
    private final static String PARTIAL_PATH = "data/partial/";
    // TODO
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

            long offsetDocId = 0;
            long offsetFrequency = 0;
            long offsetVocabulary = 0;

            Map<String, TermInfo> termsToMerge = new HashMap<>();
            InvertedIndex invertedIndex = new InvertedIndex();
            PostingList[] postingList = new PostingList[numFiles];

            try
            {
                // for each block, initialize take data structure of a term
                for (int indexBlock=0; indexBlock<numFiles; indexBlock++)
                {
                    System.out.println("DBG: Block-" + indexBlock);
                    docIdFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(PARTIAL_PATH + "docIDsBlock-" + indexBlock),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                    frequenciesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(PARTIAL_PATH + "frequenciesBlock-" + indexBlock),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                    vocabulariesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(PARTIAL_PATH + "vocabularyBlock-" + indexBlock),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                    // Stating to read inside the vocabulary the length of the first posting list
                    vocabulariesBuffers[indexBlock] = vocabulariesFileChannel[indexBlock].map(FileChannel.MapMode.READ_WRITE, offsetVocabulary, 64L+4L+4L+4L);

                    // TODO NON FUNZIONA UN CAZZO
                    byte[] termBytes = new byte[64];
                    vocabulariesBuffers[indexBlock].get(termBytes);
                    int frequency = vocabulariesBuffers[indexBlock].getInt();
                    int offset = vocabulariesBuffers[indexBlock].getInt();
                    int nPosting = vocabulariesBuffers[indexBlock].getInt();

                    String stringa = new String(termBytes);
                    String term = new String(termBytes).trim();
                    System.out.println("DBG: " + term + " " + " <" + frequency + ", " + offset + ", " + nPosting + ">");
                    termsToMerge.put(term, new TermInfo(frequency, offset, nPosting));

                    postingList[indexBlock] = new PostingList(new String(termBytes).trim());

                    // Need to read nPosting integers, i.e. the entire posting list for that term
                    docIdBuffers[indexBlock] = docIdFileChannel[indexBlock].map(FileChannel.MapMode.READ_WRITE, offsetDocId, 4L*nPosting);
                    frequenciesBuffers[indexBlock] = frequenciesFileChannel[indexBlock].map(FileChannel.MapMode.READ_WRITE, offsetFrequency, 4L*nPosting);

                    // Fill the posting list
                    postingList[indexBlock] = new PostingList(term);
                    for(int j=0; j<nPosting; j++)
                    {
                        int d = docIdBuffers[indexBlock].getInt();
                        int f = frequenciesBuffers[indexBlock].getInt();
                        postingList[indexBlock].addPosting(new Posting(d, f));
                        System.out.println("DBG: <" + d + ", " + f + ">");
                    }


                    System.out.println();
                }

                // TODO Aggiornare gli offset!!!!!!!
                //      offsetDocId += 4L;
                //      offsetFrequency += 4L;
                //      offsetVocabulary += 36L;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("ERR:    Merge error, directory " + PARTIAL_PATH + " doesn't exists!");
        }
    }

}
