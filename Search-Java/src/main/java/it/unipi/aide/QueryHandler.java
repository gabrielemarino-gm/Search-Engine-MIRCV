package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.DocumentIndex;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;

import java.util.List;
import java.util.Scanner;

import static it.unipi.aide.utils.ColorText.*;

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
    static DocumentIndex documentIndex = new DocumentIndex();

    public static void main(String[] args)
    {
        while (true)
        {
            if (!SETUP)
            {
                setupSystem();
                continue;
            }

            System.out.printf("%sQuery Handler: Type query (%s, %s) >%s ", BLUE, ALGORITHM, BM25? "BM25" : "TF-IDF", ANSI_RESET);
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("q"))
            {
                System.out.print(BLUE + "Query Handler > " + ANSI_RESET + "Are you sure? (Y/N) ");
                input = scanner.nextLine();

                while(!(input.equalsIgnoreCase("y")) && !(input.equalsIgnoreCase("n")))
                {
                    System.out.println(RED + "Query Handler ERR > Invalid input. Try again." + ANSI_RESET);
                    System.out.print(BLUE + "Query Handler > " + ANSI_RESET + "Are you sure? (Y/N) ");
                    input = scanner.nextLine();
                }

                if(input.equalsIgnoreCase("y"))
                {
                    System.out.println(BLUE + "Query Handler > " + ANSI_RESET + "Exiting...");
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
        System.out.println(BLUE + "Query Handler > "+ ANSI_RESET + "Setup system");
        System.out.println(BLUE + "Query Handler > "+ ANSI_RESET + "Choose the algorithm to use for the query, type 1 for DAAT, 2 for MaxScore");
        System.out.print(BLUE + "Query Handler > "+ ANSI_RESET);

        String input = scanner.nextLine();

        while(!(input.equals("1") || input.equals("2")))
        {
            System.out.println(RED + "Query Handler ERR > Invalid input. Try again." + ANSI_RESET);
            System.out.print(BLUE + "Query Handler > "+ ANSI_RESET);
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
            System.out.println(RED + "Query Handler ERR > Invalid input. Try again." + ANSI_RESET);
            return;
        }

        System.out.println(BLUE + "Query Handler > "+ ANSI_RESET + "What kind of score function do you want to use? Type 1 for TF-IDF, 2 for BM25 ");
        System.out.print(BLUE + "Query Handler > "+ ANSI_RESET);

        while(!(input.equals("1") || input.equals("2")))
        {
            System.out.println(RED + "Query Handler ERR > Invalid input. Try again." + ANSI_RESET);
            System.out.print(BLUE + "Query Handler > "+ ANSI_RESET);
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
            System.out.println(RED + "Query Handler ERR > Invalid input. Try again." + ANSI_RESET);
            return;
        }

        System.out.println(BLUE + "Query Handler > "+ ANSI_RESET + "Set the value of k for the top-k documents ");
        System.out.print(BLUE + "Query Handler > "+ ANSI_RESET);
        input = scanner.nextLine();

        try
        {
            TOP_K = Integer.parseInt(input);
        }
        catch (NumberFormatException e)
        {
            System.out.println(RED + "Query Handler ERR > Invalid input. Try again." + ANSI_RESET);
            return;
        }

        System.out.println(BLUE + "Query Handler > "+ ANSI_RESET + "System setup completed.");
        SETUP = true;
    }

    private static void processQueryDAAT(String query)
    {
        List<String> tokens = preprocesser.process(query);
        long startTime = System.currentTimeMillis();

        System.out.printf("%sQuery Handler: Results (%s, %s) >%s \n", BLUE, ALGORITHM, BM25? "BM25" : "TF-IDF", ANSI_RESET);

        System.out.println("PID\t\t\t|\tScore");
        for (ScoredDocument sd : daat.executeDAAT(tokens, BM25, TOP_K)) {
            float score = sd.getScore();
            String pid = documentIndex.get(sd.getDocID()).getPid();

            if (Integer.parseInt(pid) < 10)
                System.out.println(String.format("%s\t\t\t|\t%.4f", pid, score));
            else
                System.out.println(String.format("%s\t\t|\t%.4f", pid, score));
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("(" + elapsedTime + " ms)");
    }

    private static void processQueryMaxScore(String query)
    {
        List<String> tokens = preprocesser.process(query);
        long startTime = System.currentTimeMillis();

        System.out.printf("%sQuery Handler: Results (%s, %s) >%s \n", BLUE, ALGORITHM, BM25? "BM25" : "TF-IDF", ANSI_RESET);

        System.out.println("PID\t\t\t|\tScore");
        // Print the list of top-k scored documents, in reverse order
        for (ScoredDocument sd : maxScore.executeMaxScore(tokens, BM25, TOP_K)) {
            float score = sd.getScore();
            String pid = documentIndex.get(sd.getDocID()).getPid();

            if (Integer.parseInt(pid) < 10)
                System.out.println(String.format("%s\t\t\t|\t%.4f", pid, score));
            else
                System.out.println(String.format("%s\t\t|\t%.4f", pid, score));
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("(" + elapsedTime + " ms)");
    }
}
