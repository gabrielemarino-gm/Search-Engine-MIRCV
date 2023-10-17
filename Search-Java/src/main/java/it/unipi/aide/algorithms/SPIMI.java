package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;

import java.io.*;
import java.lang.management.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Class that implement the SPIMI algorithm
 *
 * Creates Inverted Indexes for each split of a corpus
 * based on available memory
 *
 * NB: Final merging is temporarily made by another class
 */
public class SPIMI 
{
    private final int MAX_MEM;
    private final String inputPath;
    private final String outputPath;

    private Vocabulary vocabulary;
    private InvertedIndex invertedIndex;
    private final Preprocesser preprocesser;
    private int incrementalBlockNumber;
    private int numBlocksPosting;

    /**
     * SPIMI constructor
     * @param inputPath Where the Corpus to process is located
     * @param outputPath Where to output partial results
     * @param maxMem Max memory allowed in %
     * @param stemming Enable stemming
     */
    public SPIMI(String inputPath, String outputPath, int maxMem, boolean stemming) 
    {
        MAX_MEM = maxMem;
        this.inputPath = inputPath;
        this.outputPath = outputPath+"partial/";

        vocabulary = new Vocabulary();
        invertedIndex = new InvertedIndex();
        preprocesser = new Preprocesser(stemming);
        incrementalBlockNumber = 0;
        numBlocksPosting = 0;

        System.out.println("LOG:    SPIMI Parameters");
        System.out.println("LOG:        MAX_MEM = " + maxMem);
        System.out.println("LOG:        inputPath = " + inputPath);
        System.out.println("LOG:        outputPath = " + outputPath);
        System.out.println("LOG:        stemming = " + stemming);
    }

    /**
     * Write partial Inverted Index on the disk
     * @return true upon success, false otherwise
     */
    public boolean writeBlockToDisk(boolean debug)
    {
        String docPath = outputPath+"docIDsBlock-"+ incrementalBlockNumber;
        String freqPath = outputPath+"frequenciesBlock-"+ incrementalBlockNumber;
        String vocPath = outputPath+"vocabularyBlock-"+ incrementalBlockNumber;

        if(!FileManager.checkFile(docPath)) FileManager.createFile(docPath);
        if(!FileManager.checkFile(freqPath)) FileManager.createFile(freqPath);
        if(!FileManager.createFile(vocPath)) FileManager.createFile(vocPath);

        try (
                // Open a FileChannel for docId, frequencies and vocabulary fragments
                FileChannel docIdFileChannel = (FileChannel) Files.newByteChannel(Paths.get(docPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                FileChannel frequencyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                     StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                FileChannel vocabularyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                     StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE))
        {
            // Create the buffer where write the streams of bytes
            MappedByteBuffer docIdBuffer = docIdFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, numBlocksPosting*4L);
            MappedByteBuffer frequencyBuffer = frequencyFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, numBlocksPosting*4L);


            long vocOffset = 0;
            for (String t: vocabulary.getTerms())
            {
                // For each term I have to save into the vocabulary file.
                TermInfo termInfo = vocabulary.get(t);

                // Allocate the buffer for write:
                // + 64 byte for the term
                // + 4 byte for the frequency
                // + 8 byte for the offset
                // + 4 byte for the number of posting
                MappedByteBuffer vocabularyBuffer = vocabularyFileChannel.map(FileChannel.MapMode.READ_WRITE, vocOffset, TermInfo.SIZE_TERM+4L+8L+4L);
                vocOffset += TermInfo.SIZE_TERM + 4 + 8 + 4;

                // Write vocabulary entry
                String paddedTerm = String.format("%-64s", termInfo.getTerm()).substring(0, 64); // Pad with spaces up to 64 characters

                // Write
                vocabularyBuffer.put(paddedTerm.getBytes());
                vocabularyBuffer.putInt(termInfo.getTotalFrequency());
                vocabularyBuffer.putLong(termInfo.getOffset());
                vocabularyBuffer.putInt(termInfo.getNumPosting());

                // Write the other 2 files for DocId and Frequency
                for (Posting p: invertedIndex.getPostingList(t))
                {
                    docIdBuffer.putInt(p.getDocId());
                    frequencyBuffer.putInt(p.getFrequency());
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        // Debug version to write plain text
        if (debug)
        {
            FileManager.createDir(outputPath + "debug/");
            try
            {
                // Write inverted index to debug text file
                BufferedWriter indexWriter = new BufferedWriter(
                        new FileWriter(outputPath + "debug/Block-" + incrementalBlockNumber + ".txt")
                );

                indexWriter.write(invertedIndex.toString());
                indexWriter.close();

                // Write vocabulary to debug text file
                BufferedWriter vocabularyWriter = new BufferedWriter(
                        new FileWriter(outputPath + "debug/vocabulary-" + incrementalBlockNumber + ".txt")
                );

                vocabularyWriter.write(vocabulary.toString());
                vocabularyWriter.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return true;
    }


    /**
     * Executes the SPIMI algorithm as configured
     * @param debug Enable debug mode
     * @return the number of blocks created
     */
    public int algorithm(boolean debug)
    {
        System.out.println("Starting algorithm...");
        /*
        * TODO -> This should be a class variable, not a method variable
        *   -
        *  If i need to create and call SPIMI ond another corpus there will be
        *  duplicate docid
        */
        int docid = 0;

        // Starting cleaning the folder
        FileManager.cleanFolder(outputPath);
        Corpus corpus = new Corpus(inputPath);

        // For each documents
        for(String doc: corpus)
        {
            String[] docParts = doc.split("\t");

            /*
             * docid = Unique id assigned in incremental manner
             * pid = Unique name of the document
             */
            String pid = docParts[0];
            String text = docParts[1];
            List<String> tokens = preprocesser.process(text);

            ProcessedDocument document = new ProcessedDocument(pid, docid, tokens);
            // TODO -> Create a Document Index (pid, docid, #words, ...)
            docid++;

            if(document.getTokens().isEmpty()) continue;

            for (String t : document.getTokens())
            {
                // Add term to the vocabulary and to the inverted index.
                // If the term already exists, the method add the docId to the posting list
                vocabulary.add(t); // TODO Sistemare aggiornamento frequenza e numero posting
                invertedIndex.add(document.getDocid(), t);
                vocabulary.updateNumPosting(t, invertedIndex.getPostingList(t).size());
                numBlocksPosting++;
            }

            if (getPercentOfMemoryUsed() > MAX_MEM)
            // TODO -> Remember to swap this to memory threshold
            // if (numBlocksPosting > 100)
            {
                System.out.println("LOG:    Writing block #" + incrementalBlockNumber);

                if (writeBlockToDisk(debug))
                {
                    incrementalBlockNumber++;
                    numBlocksPosting = 0;
                    vocabulary = new Vocabulary();
                    invertedIndex = new InvertedIndex();
                }
                else
                {
                    System.out.println("ERROR: Not able to write the binary file");
                    break;
                }

                int i = 0;
                System.out.println("LOG:    Waiting for fre memory...");
                while (getPercentOfMemoryUsed() > MAX_MEM-20)
                {
                    if (i%1000000 == 0)
                        System.out.println("LOG:    Memory = " + getPercentOfMemoryUsed());
                    i++;
                    continue;
                }
                System.out.println("LOG:    Memory Free!");
            }

            if (docid%1000000 == 0)
                System.out.println("LOG:    Documets processed " + docid);
        }

        // We need to write the last block, that can stay in memory under the threshold
        if (writeBlockToDisk(debug))
        {
            System.out.println("LOG:    Writing block #" + incrementalBlockNumber);
            incrementalBlockNumber++;
            vocabulary = new Vocabulary();
            invertedIndex = new InvertedIndex();
        }
        else
        {
            System.out.println("ERROR: Not able to write the binary file");
        }

        // There will be 'incrementalBlockNumber' blocks, but the last one has index 'incrementalBlockNumber - 1'
        return incrementalBlockNumber;
    }

    /**
     * Support function to get used memory in %
     * @return xx.x% of memory used
     */
    private double getPercentOfMemoryUsed()
    {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long usedMemory = heapMemoryUsage.getUsed();
        long maxMemory = heapMemoryUsage.getMax();
        return ((double) usedMemory / maxMemory) * 100.0;
    }
}

