package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.Merging;
import it.unipi.aide.algorithms.SPIMI;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.Preprocesser;

import java.util.List;
import java.util.Scanner;

public class Main
{

    static Scanner scanner = new Scanner(System.in);
    static Preprocesser preprocesser = new Preprocesser(true);
    static DAAT daat = new DAAT(10);

    public static void main(String[] args)
    {
        init(args); //to change init to specify preprocessing options

        System.out.println("Ready.");

        while (true)
        {
            System.out.print("Search for: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit"))
            {
                System.out.print("Are you sure? (Y/N) ");
                input = scanner.nextLine();

                while(!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n")))
                    input = scanner.nextLine();

                if(input.equalsIgnoreCase("y")) {
                    System.out.println("Exiting...");
                    break;
                }
                else
                    continue;
            }

            processQuery(input);
        }

        scanner.close();
    }

    private static void processQuery(String input) {

        List<String> tokens = preprocesser.process(input);

        long startTime = System.currentTimeMillis();

        System.out.println("Results:");
        for (ScoredDocument sd : daat.executeDAAT(tokens)) {
            System.out.print(sd);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println(elapsedTime + " ms");
    }

    public static void init(String[] args)
    {
        long startTime = System.currentTimeMillis();

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
            if (args[i].equals("-c")) {COMPRESSION = true; i += 1;}

            else {i++; System.err.println("Unknown command. Continuing.");}
        }

        if(INPUT_PATH == null)
        {
            System.err.println("Input path not specified. Exiting.");
            System.exit(1);
        }

        // Index building
        SPIMI spimi = new SPIMI(INPUT_PATH, STOPSTEM);
        int nBlocks  = spimi.algorithm(DEBUG);

        System.out.println("LOG:\t\tIndex created. Merging...");

        // Index merging
        Merging merge = new Merging(COMPRESSION, nBlocks, DEBUG);
        merge.mergeBlocks();

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("Init time: " + elapsedTime + " ms");
    }
}