package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.*;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class GabTest
{
    static Preprocesser preprocesser = new Preprocesser(true);
    static DAAT daat = new DAAT(10);
    static MaxScore maxScore = new MaxScore(true, 10);
    public static void main(String[] argv)
    {
        // String query = "who proposed the mathematical relationship on how gases physically mixed will generate pressure";
        String query = "the cat is on the little table where there is a red flower";

        List<String> queryTerms = preprocesser.process(query);
        System.out.println("QUERY: " + query);

        System.out.println();
        processQueryDAAT(queryTerms);
        System.out.println();
        processQueryMaxScore(queryTerms);
    }

    private static void priorityQueueTest()
    {
        // Provare la priority queue di dimensione massima 5
        PriorityQueue<ScoredDocument> priorityQueue = new PriorityQueue<>(5, ScoredDocument.compareTo());

        for (int i = 0; i < 10; i++)
        {
            ScoredDocument sd = new ScoredDocument(i, (float) ((float) i*0.1));
            priorityQueue.add(sd);

            System.out.println("Iteration " + i);
            System.out.println(priorityQueue);
            if (priorityQueue.size() > 5)
                priorityQueue.poll();
            System.out.println();
            System.out.println(priorityQueue);
            System.out.println("-------------------------");
        }

        System.out.println("END:");
        System.out.println(priorityQueue);
    }
    private static void processQueryDAAT(List<String> queryTerms)
    {
        long startTime = System.currentTimeMillis();

        System.out.println("Results DAAT:");

        for (ScoredDocument sd : daat.executeDAAT(queryTerms))
        {
            System.out.print(sd);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println(elapsedTime + " ms");
    }

    private static void processQueryMaxScore(List<String> tokens)
    {
        long startTime = System.currentTimeMillis();

        System.out.println("Results MAX-SCORE:");

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println(elapsedTime + " ms for reading docLengths");

        startTime = System.currentTimeMillis();
        for (ScoredDocument sd : maxScore.executeMaxScore(tokens)) {
            System.out.print(sd);
        }

        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;

        System.out.println(elapsedTime + " ms for executing maxScore");
    }
}
