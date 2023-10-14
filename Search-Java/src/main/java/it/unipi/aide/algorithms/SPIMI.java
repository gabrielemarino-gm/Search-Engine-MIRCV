package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;

import java.io.*;
import java.lang.management.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.ByteChannel;
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
    private int b;
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
        this.outputPath = outputPath;

        vocabulary = new Vocabulary();
        invertedIndex = new InvertedIndex();
        preprocesser = new Preprocesser(stemming);
        b = 0;
        numBlocksPosting = 0;

        // System.out.println("SPIMI Parameters:");
        // System.out.println("inputPath: " + inputPath);
        // System.out.println("outputPath: " + outputPath);
        // System.out.println("MAXMEM: " + MAX_MEM);
    }


    /**
     * Creates an iterator on the corpus to process
     * @return An iterator on the process
     */
    private Iterator<String> getNextDoc()
    {
        /*
        * TODO -> By allocating this 'lines' array and adding line from the file,
        *  we are moving the entire corpus in memory
        *   -
        *  Check if it's true, and if there is another way to do it
        */
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputPath))) 
        {
            String line;
            while ((line = br.readLine()) != null) 
            {
                lines.add(line);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        return lines.iterator();
    }

    /**
     * Write partial Inverted Index on the disk
     * @return true upon success, false otherwise
     */
    public boolean writeBlockToDisk(boolean debug)
    {
        try (FileChannel docIdFileChannel = (FileChannel) Files.newByteChannel(Paths.get(outputPath+"docIDsBlock-"+b),
                StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                FileChannel frequencyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(outputPath+"frequenciesBlock-"+b),
            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE))
        {
            // Create the buffer where write the streams of bytes
            MappedByteBuffer docIdBuffer = docIdFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, numBlocksPosting*4L);
            MappedByteBuffer frequencyBuffer = docIdFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, numBlocksPosting*4L);
            
            for (String t: vocabulary.getTerms())
            {
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

        // try
        // {
        //     // Write inverted index to binary file
        //     ObjectOutputStream indexStream = new ObjectOutputStream (
        //             Files.newOutputStream(Paths.get(outputPath + "/block/Block-" + b + ".bin"))
        //     );
        //     indexStream.writeObject(invertedIndex);
        //     indexStream.close();
//
        //     // Write vocabulary to binary file
        //     ObjectOutputStream vocabularyStream = new ObjectOutputStream (
        //             Files.newOutputStream(Paths.get(outputPath + "/vocabulary/vocabulary-" + b + ".bin"))
        //     );
        //     vocabularyStream.writeObject(vocabulary);
        //     vocabularyStream.close();
        // }
        // catch (IOException e)
        // {
        //     e.printStackTrace();
        //     return false;
        // }

        // Debug version to write plain text
        if (debug)
        {
            FileManager.createDir(outputPath + "debug");
            try
            {
                System.out.println("qui");
                // Write inverted index to debug text file
                BufferedWriter indexWriter = new BufferedWriter(
                        new FileWriter(outputPath + "debug/Block-" + b + ".txt")
                );
                indexWriter.write(invertedIndex.toString());
                indexWriter.close();

                // Write vocabulary to debug text file
                BufferedWriter vocabularyWriter = new BufferedWriter(
                        new FileWriter(outputPath + "debug/vocabulary-" + b + ".txt")
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
     */
    public void algorithm(boolean debug)
    {
        System.out.println("Starting algorithm...");
        /*
        * TODO -> This should be a class variable, not a method variable
        *   -
        *  If i need to create and call SPIMI ond another corpus there will be
        *  duplicate docid
        */
        int docid = 0;

        Iterator<String> docIterator = getNextDoc();

        // Starting cleaning the folder
        FileManager.cleanFolder(outputPath);

        while (docIterator.hasNext())
        {
            String docContent = docIterator.next();
            String[] docParts = docContent.split("\t");

            /*
             * docid = Unique id assigned in incremental manner
             * pid = Unique name of the document
             */
            String pid = docParts[0];
            String text = docParts[1];
            List<String> tokens = preprocesser.process(text);

            /*
            * TODO -> Empty document check not done
            *  tokens.size() == 0
            *  -
            *  The preprocessor already handles empty tokens
            *  no need to iterate tokens
            */

            ProcessedDocument document = new ProcessedDocument(pid, docid, tokens);
            docid++;

            // TODO -> Create a Document Index (pid, docid, #words, ...)
            for (String t : tokens)
            {
                if (t.isEmpty()) {
                    continue;
                }

                vocabulary.add(t);
                invertedIndex.add(document.getDocid(), t);
                numBlocksPosting++;
            }

            //if (getPercentOfMemoryUsed() > MAX_MEM)
            if (numBlocksPosting > 1000)
            {
                System.out.println("Writing block #" + b);

                if (writeBlockToDisk(debug))
                {
                    b++;
                    numBlocksPosting = 0;
                    vocabulary = new Vocabulary();
                    invertedIndex = new InvertedIndex();
                }
                else
                {
                    System.out.println("ERROR: Not able to write the binary file");
                    break;
                }
            }

            if (docid % 100 == 0)
            {
                System.out.println("Document progression: " + docid);
            }
        }

        // Debug test un par di palle xD
//        invertedIndex.printIndex();
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

