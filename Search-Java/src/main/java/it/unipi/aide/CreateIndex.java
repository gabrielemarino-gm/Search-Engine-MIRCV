package it.unipi.aide;

import it.unipi.aide.algorithms.Merging;
import it.unipi.aide.algorithms.SPIMI;
import it.unipi.aide.utils.ConfigReader;

import static it.unipi.aide.utils.beautify.ColorText.*;

public class CreateIndex
{
    static final String HELP_STRING = RED +
        "Usage: createIndex -in <corpus_file> [options]\n\n" +
        "Options:\n" +
        "\t-c\tEnable index compression\n" +
        "\t-ss\tEnable stemming and stopword removal\n" +
        "\t-d\tEnable debug files creation\n" +
        ANSI_RESET;

    public static void main(String[] args)
    {
        int maxArgs = args.length;
        if (maxArgs == 1)
        {
            System.out.println(HELP_STRING);
            return;
        }

        for(String s : args)
        {
            if (s.equals("-h"))
            {
                System.out.println(HELP_STRING);
                return;
            }
        }

        boolean DEBUG = false;
        boolean COMPRESSION = false;
        boolean STOPSTEM = false;
        String INPUT_PATH = null;

        int i = 1;
        while(i < maxArgs)
        {
            switch (args[i]) {
                case "-in":
                    INPUT_PATH = args[i + 1];
                    i += 2;
                    break;
                case "-ss":
                    STOPSTEM = true;
                    i += 1;
                    break;
                case "-d":
                    DEBUG = true;
                    i += 1;
                    break;
                case "-c":
                    COMPRESSION = true;
                    i += 1;
                    break;
                default:
                    System.out.println(RED + "Search Engine ERR > Unknown command for createIndex. Use createIndex -h." + ANSI_RESET);
                    return;
            }
        }

        if (INPUT_PATH == null)
        {
            System.out.println(RED +"Search Engine ERR > Input path not specified. Exiting." + ANSI_RESET);
            System.out.println();
        }

        ConfigReader.setCompression(COMPRESSION);
        ConfigReader.setStemming(STOPSTEM);

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

        System.exit(0);
    }
}
