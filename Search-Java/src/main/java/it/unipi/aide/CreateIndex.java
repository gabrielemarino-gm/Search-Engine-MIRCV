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

        int i = 1;
        int maxArgs = args.length;
        while(i < maxArgs)
        {
            if (args[i].equals("-in")) {INPUT_PATH = args[i+1]; i += 2; continue;}
            if (args[i].equals("-ss")) {STOPSTEM = true; i += 1; continue;}
            if (args[i].equals("-d")) {DEBUG = true; i += 1; continue;}
            if (args[i].equals("-c")) {COMPRESSION = true; i += 1; continue;}

            else {i++; System.err.println("Search Engine ERR > Unknown key for the command createIndex. Try again.");}
        }

        if(INPUT_PATH == null)
        {
            System.err.println("Search Engine ERR > Input path not specified. Exiting.");
            System.out.println();
        }

        // Index building
        SPIMI spimi = new SPIMI(INPUT_PATH, STOPSTEM);
        int nBlocks  = spimi.algorithm(DEBUG);

        if (nBlocks == 0)
        {
            return;
        }

        // Index merging
        Merging merge = new Merging(COMPRESSION, nBlocks, DEBUG);
        merge.mergeBlocks();
    }
}