package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.Document;
import it.unipi.aide.model.DocumentIndex;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * This class is used by the user to make queries on the InvertedIndex
 */
public class QueryHandler
{
    static boolean SETUP = false;
    static boolean BM25 = false;
    static String ALGORITHM = "DAAT";
    static int TOP_K = 10;
    static Scanner scanner = new Scanner(System.in);
    static Preprocesser preprocesser = new Preprocesser(true);
    static DAAT daat = new DAAT();
    static MaxScore maxScore = new MaxScore();

    public static void main(String[] args)
    {
        while (true)
        {
            if (!SETUP)
            {
                setupSystem();
                continue;
            }

            System.out.print(String.format("Query Handler: Type query (%s, %s) > ", ALGORITHM, BM25? "BM25" : "TF-IDF"));
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("q"))
            {
                System.out.print("Query Handler > Are you sure? (Y/N) ");
                input = scanner.nextLine();

                while(!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n")))
                    input = scanner.nextLine();

                if(input.equalsIgnoreCase("y"))
                {
                    System.out.println("Query Handler > Exiting...");
                    break;
                }
                else
                {
                    continue;
                }
            }
            else if (input.equalsIgnoreCase("s"))
            {
                SETUP = false;
                continue;
            }

            if (ALGORITHM.equals("DAAT"))
                processQueryDAAT(input);
            else if (ALGORITHM.equals("MAX-SCORE"))
                processQueryMaxScore(input);
        }
    }

    /**
     * User interface for the query handler.
     * Setup the system for the query
     */
    private static void setupSystem()
    {
        System.out.println("Query Handler > Setup system");
        System.out.println("Query Handler > Choose the algorithm to use for the query, type 1 for DAAT, 2 for MaxScore");
        System.out.print("Query Handler > ");

        String input = scanner.nextLine();

        while(!(input.equals("1") || input.equals("2")))
        {
            System.err.println("Query Handler ERR > Invalid input. Try again.");
            System.out.println();
            System.out.print("Query Handler > ");
            input = scanner.nextLine();
        }

        if (input.equals("1"))
        {
            ALGORITHM = "DAAT";
        }
        else if (input.equals("2"))
        {
            ALGORITHM = "MAX-SCORE";
        }
        else
        {
            System.err.println("Query Handler ERR > Invalid input. Try again.");
            return;
        }

        System.out.println("Query Handler > What kind of score function do you want to use? Type 1 for TF-IDF, 2 for BM25 ");
        System.out.print("Query Handler > ");

        while(!(input.equals("1") || input.equals("2")))
        {
            System.err.println("Query Handler ERR > Invalid input. Try again.");
            System.out.println();
            System.out.print("Query Handler > ");
            input = scanner.nextLine();
        }

        input = scanner.nextLine();
        if (input.equals("1"))
        {
            BM25 = false;
        }
        else if (input.equals("2"))
        {
            BM25 = true;
        }
        else
        {
            System.err.println("Query Handler ERR > Invalid input. Try again.");
            return;
        }

        System.out.println("Query Handler > Set the value of k for the top-k documents ");
        System.out.print("Query Handler > ");
        input = scanner.nextLine();

        try
        {
            TOP_K = Integer.parseInt(input);
        }
        catch (NumberFormatException e)
        {
            System.err.println("Query Handler ERR > Invalid input. Try again.");
            return;
        }

        System.out.println("Query Handler > System setup completed.");
        SETUP = true;
    }

    private static void processQueryDAAT(String query)
    {
        List<String> tokens = preprocesser.process(query);
        long startTime = System.currentTimeMillis();

        System.out.println(String.format("Query Handler Results (%s, %s) > ", ALGORITHM, BM25? "BM25" : "TF-IDF"));

        for (ScoredDocument sd : daat.executeDAAT(tokens, BM25, TOP_K)) {
            System.out.print("\t\t\t\t\t\t" + sd);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("\t\t\t\t\t\t(" + elapsedTime + " ms)");
    }

    private static void processQueryMaxScore(String query)
    {
        List<String> tokens = preprocesser.process(query);
        long startTime = System.currentTimeMillis();

        System.out.println(String.format("Query Handler Results (%s, %s) > ", ALGORITHM, BM25? "BM25" : "TF-IDF"));
        // Print the list of top-k scored documents, in reverse order
        for (ScoredDocument sd : maxScore.executeMaxScore(tokens, BM25, TOP_K)) {
            System.out.print("\t\t\t\t\t\t" + sd);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("\t\t\t\t\t\t(" + elapsedTime + " ms)");
    }
}
