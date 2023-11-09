package it.unipi.aide;

import it.unipi.aide.algorithms.MergingM;
import it.unipi.aide.algorithms.SPIMI;

public class CreateIndex
{
    public static void main(String[] args)
    {
        boolean DEBUG = false;
        boolean COMPRESSION = false;
        boolean STOPSTEM = false;
        String INPUT_PATH = null;
        String OUTPUT_PATH = null;
        String MODE = null;
        boolean MAXMEM = false;

        int i = 0;
        int maxArgs = args.length;
        while(i < maxArgs)
        {
            if (args[i].equals("-c")) {COMPRESSION = true; i += 1; continue;}
            if (args[i].equals("-d")) {DEBUG = true; i += 1; continue;}
            if (args[i].equals("-ss")) {STOPSTEM = true; i += 1; continue;}
            if (args[i].equals("-in")) {INPUT_PATH = args[i+1]; i += 2; continue;}
            if (args[i].equals("-out")) {OUTPUT_PATH = args[i+1]; i += 2; continue;}
            if (args[i].equals("-m")) {MODE = args[i+1]; i += 2; continue;} // TODO -> To remove, useless
            if (args[i].equals("-mm")) {MAXMEM = true; i += 1; continue;}

            else {i++; System.err.println("Unknown command. Continuing.");}
        }

        if(INPUT_PATH == null)
        {
            System.err.println("Input path not specified. Exiting.");
            System.exit(1);
        }

        if(OUTPUT_PATH == null)
        {
            System.err.println("Output path not specified. Exiting.");
            System.exit(1);
        }

        if(MODE == null)
        {
            System.err.println("Mode not specified. Assuming TFIDF.");
            MODE = "TFIDF";
        }
        // Index building
        SPIMI spimi = new SPIMI(INPUT_PATH, OUTPUT_PATH, MAXMEM, STOPSTEM);
        int numBlocks = spimi.algorithm(DEBUG);

        System.out.println("LOG:\t\tIndex created. Merging...");

        // Index merging
//        Merging merge = new Merging(OUTPUT_PATH, COMPRESSION, numBlocks);
//        merge.mergeBlocks(DEBUG);

        MergingM mergem = new MergingM(OUTPUT_PATH, COMPRESSION, numBlocks);
        mergem.mergeBlocks(DEBUG);
    }
}