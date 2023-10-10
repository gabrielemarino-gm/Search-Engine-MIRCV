package it.unipi.aide.utils;

import it.unipi.aide.model.InvertedIndex;
import it.unipi.aide.model.Vocabulary;

import java.io.*;
import java.lang.management.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class SPIMI {
    private final int MAX_MEM;
    private final String inputPath;
    private final String outputPath;
    private Vocabulary dictionary;
    private InvertedIndex invertedIndex;
    private final Preprocesser preprocesser;
    private int b;

    public SPIMI(String inputPath, String outputPath, int maxmem, boolean stemmstop) {
        MAX_MEM = maxmem;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        dictionary = new Vocabulary();
        invertedIndex = new InvertedIndex();
        preprocesser = new Preprocesser(stemmstop);
        b = 0;

        System.out.println("SPIMI Parameters:");
        System.out.println("inputPath: " + inputPath);
        System.out.println("outputPath: " + outputPath);
        System.out.println("MAXMEM: " + MAX_MEM);
    }

    public Iterator<String> get_next_doc() {
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

    public void write_block_to_disk_debug() {
        try {
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

    public void algorithm(boolean debug) {
        System.out.println("Start algorithm");
        int id = 0;

        Iterator<String> docIterator = get_next_doc();

        while (docIterator.hasNext()) {
            String docContent = docIterator.next();
            String[] docParts = docContent.split("\t");
            String docId = docParts[0];
            String text = docParts[1];

            List<String> terms = preprocesser.process(text);
            id++;

            for (String t : terms) {
                if (t.equals("")) {
                    continue;
                }

                dictionary.add(t);
                invertedIndex.add(Integer.parseInt(docId), t);
            }

            double memoryUsed = getPercentOfMemoryUsed();

            if (memoryUsed > MAX_MEM) {
                System.out.println("Write block " + b);

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

                if (id % 100000 == 0) {
                    System.out.println("Document progression: " + id);
                }
            }
        }
    }

    private double getPercentOfMemoryUsed() {
        // Ottieni l'istanza di MemoryMXBean
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        // Ottieni l'utilizzo della memoria
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long usedMemory = heapMemoryUsage.getUsed();
        long maxMemory = heapMemoryUsage.getMax();

        // Calcola l'utilizzo della memoria in percentuale
        return ((double) usedMemory / maxMemory) * 100.0;
    }
}

