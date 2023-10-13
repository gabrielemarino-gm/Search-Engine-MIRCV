package it.unipi.aide.algorithms;

import it.unipi.aide.model.InvertedIndex;
import it.unipi.aide.model.ProcessedDocument;
import it.unipi.aide.model.Vocabulary;
import it.unipi.aide.utils.Preprocesser;

import java.io.*;
import java.lang.management.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class SPIMI {
    private final int MAX_MEM;
    private final String inputPath;
    private final String outputPath;
    private Vocabulary dictionary;
    private InvertedIndex invertedIndex;
    private final Preprocesser preprocesser;
    private int b;

    /**
     * SPIMI constructor
     * @param inputPath Where the Corpus to process is located
     * @param outputPath Where to output partial results
     * @param maxMem Max memory allowed in %
     * @param stemming Enable stemming
     */
    public SPIMI(String inputPath, String outputPath, int maxMem, boolean stemming) {
        MAX_MEM = maxMem;
        this.inputPath = inputPath;
        this.outputPath = outputPath;

        dictionary = new Vocabulary();
        invertedIndex = new InvertedIndex();
        preprocesser = new Preprocesser(stemming);
        b = 0;

        System.out.println("SPIMI Parameters:");
        System.out.println("inputPath: " + inputPath);
        System.out.println("outputPath: " + outputPath);
        System.out.println("MAXMEM: " + MAX_MEM);
    }


    /**
     * Creates an iterator on the corpus to process
     * @return An iterator on the process
     */
    private Iterator<String> get_next_doc() {
        /*
        * TODO -> By allocating this 'lines' array and adding line from the file,
        *  we are moving the entire corpus in memory
        *   -
        *  Check if it's true, and if there is another way to do it
        */
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines.iterator();
    }

    /**
     * Write partial Inverted Index on the disk
     * @return true upon success, false otherwise
     */
    public boolean write_block_to_disk() {
        try {
            // Write inverted index to binary file
            ObjectOutputStream indexStream = new ObjectOutputStream(
                    Files.newOutputStream(Paths.get(outputPath + "/block/Block-" + b + ".bin"))
            );
            indexStream.writeObject(invertedIndex);
            indexStream.close();

            // Write dictionary to binary file
            ObjectOutputStream dictionaryStream = new ObjectOutputStream(
                    Files.newOutputStream(Paths.get(outputPath + "/dictionary/Dictionary-" + b + ".bin"))
            );
            dictionaryStream.writeObject(dictionary);
            dictionaryStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Debug version to write plain text
     */
    public void write_block_to_disk_debug() {
        try {
            System.out.println("qui");
            // Write inverted index to debug text file
            BufferedWriter indexWriter = new BufferedWriter(
                    new FileWriter(outputPath + "/debug/Block-" + b + ".txt")
            );
            indexWriter.write(invertedIndex.toString());
            indexWriter.close();

            // Write dictionary to debug text file
            BufferedWriter dictionaryWriter = new BufferedWriter(
                    new FileWriter(outputPath + "/debug/Dictionary-" + b + ".txt")
            );
            dictionaryWriter.write(dictionary.toString());
            dictionaryWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes the SPIMI algorithm as configured
     * @param debug Enable debug mode
     */
    public void algorithm(boolean debug) {
        System.out.println("Starting algorithm...");
        /*
        * TODO -> This should be a class variable, not a method variable
        *   -
        *  If i need to create and call SPIMI ond another corpus there will be
        *  duplicate docid
        */
        int docid = 0;

        Iterator<String> docIterator = get_next_doc();

        while (docIterator.hasNext()) {
            String docContent = docIterator.next();
            String[] docParts = docContent.split("\t");

            /**
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
            *  The preprocesser already handles empty tokens
            *  no need to iterate tokens
            */

            ProcessedDocument document = new ProcessedDocument(pid, docid, tokens);
            docid++;

            // TODO -> Create a Document Index (pid, docid, #words, ...)

            for (String t : tokens) {
                if (t.equals("")) {
                    continue;
                }

                dictionary.add(t);
                invertedIndex.add(document.getDocid(), t);
            }

            if (getPercentOfMemoryUsed() > MAX_MEM) {
                System.out.println("Writing block #" + b);

                if (debug) {
                    write_block_to_disk_debug();
                }

                if (write_block_to_disk()) {
                    b++;
                    dictionary = new Vocabulary();
                    invertedIndex = new InvertedIndex();
                } else {
                    System.out.println("ERROR: Not able to write the binary file");
                    break;
                }
            }
            if (docid % 100 == 0) {
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
    private double getPercentOfMemoryUsed() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long usedMemory = heapMemoryUsage.getUsed();
        long maxMemory = heapMemoryUsage.getMax();
        return ((double) usedMemory / maxMemory) * 100.0;
    }
}

