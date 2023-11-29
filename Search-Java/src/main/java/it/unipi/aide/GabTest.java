package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.PostingListSkippable;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class GabTest
{
    static Preprocesser preprocesser = new Preprocesser(true);
    static DAAT daat = new DAAT(10);
    static MaxScore maxScore = new MaxScore(false, 10);
    public static void main(String[] argv)
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

    private static void queryHandler()
    {
        String query = "manhattan project scientists";
        List<String> queryTerms = preprocesser.process(query);

        // Retrieve the posting lists of the query terms
        QueryPreprocessing qp = new QueryPreprocessing();
        List<PostingListSkippable> postingLists = qp.retrievePostingList(queryTerms);
        HashMap<String, TermInfo> terms = new HashMap<String, TermInfo>();
        terms = qp.getTerms();

        // Print the posting lists
        for (PostingListSkippable p: postingLists)
        {
            System.out.println("Term: " + p.getTerm()
                    + "\nNumBlocks: " + terms.get(p.getTerm()).getNumBlocks()
                    + "\nNumPostings: " + terms.get(p.getTerm()).getNumPosting()
                    + "\nTermUpperBound: " + terms.get(p.getTerm()).getTermUpperBoundTFIDF());
            p.printPostingList();
            System.out.println();
            System.out.println();
        }

        processQueryMaxScore(queryTerms);
    }

    private static void processQueryMaxScore(List<String> tokens)
    {
        long startTime = System.currentTimeMillis();

        System.out.println("Results MAX-SCORE:");

        for (ScoredDocument sd : maxScore.executeMaxScore(tokens)) {
            System.out.print(sd);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println(elapsedTime + " ms");
    }
}
