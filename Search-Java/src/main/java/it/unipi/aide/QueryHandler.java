package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * This class is used by the user to make queries on the InvertedIndex
 */
public class QueryHandler
{
    static Scanner scanner = new Scanner(System.in);
    static Preprocesser preprocesser = new Preprocesser(true);
    static DAAT daat = new DAAT(10);
    static MaxScore maxScore = new MaxScore(false);

    public static void main(String[] args)
    {
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

                if(input.equalsIgnoreCase("y"))
                {
                    System.out.println("Exiting...");
                    break;
                }
                else
                {
                    continue;
                }
            }

            processQueryDAAT(input);
        }

        // PriorityQueue<ScoredDocument> top_k = maxScore.executeMaxScore(tokens, 5);
    }

    private static void processQueryDAAT(String query)
    {
        List<String> tokens = preprocesser.process(query);
        long startTime = System.currentTimeMillis();

        System.out.println("Results:");

        for (ScoredDocument sd : daat.executeDAAT(tokens)) {
            System.out.print(sd);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println(elapsedTime + " ms");
    }
}
