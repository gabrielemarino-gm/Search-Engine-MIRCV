package it.unipi.aide;

import it.unipi.aide.algorithms.Merging;
import it.unipi.aide.algorithms.SPIMI;
import it.unipi.aide.utils.ConfigReader;

public class CreateIndex
{
    public static void main(String[] args)
    {
        boolean DEBUG = false;
        boolean COMPRESSION = ConfigReader.compressionEnabled();
        boolean STOPSTEM = false;
        String INPUT_PATH = null;

        int i = 0;
        int maxArgs = args.length;
        while(i < maxArgs)
        {
            if (args[i].equals("-in")) {INPUT_PATH = args[i+1]; i += 2; continue;}
            if (args[i].equals("-ss")) {STOPSTEM = true; i += 1; continue;}
            if (args[i].equals("-d")) {DEBUG = true; i += 1; continue;}
            if (args[i].equals("-c")) {COMPRESSION = true; i += 1; continue;}

            else {i++; System.err.println("Unknown command. Continuing.");}
        }

        if(INPUT_PATH == null)
        {
            System.err.println("Input path not specified. Exiting.");
            System.exit(1);
        }

        // Index building
        SPIMI spimi = new SPIMI(INPUT_PATH, STOPSTEM);
        int numBlocks = spimi.algorithm(DEBUG);

        System.out.println("LOG:\t\tIndex created. Merging...");

        // Index merging
        Merging merge = new Merging(COMPRESSION, numBlocks, DEBUG);
        merge.mergeBlocks();
    }
}