package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.List;
import java.util.PriorityQueue;

/**
 * This class is used by the user to make queries on the InvertedIndex
 */
public class QueryHandler
{
    public static void main(String[] args)
    {
        QueryPreprocessing query = new QueryPreprocessing();
        query.setQuery("Manahattan Project");
        List<String> tokens = query.getTokens();

        // MaxScore maxScore = new MaxScore(false);
        // PriorityQueue<ScoredDocument> top_k = maxScore.executeMaxScore(tokens, 5);

        DAAT daat = new DAAT( 10);
        List<ScoredDocument> top_k = daat.executeDAAT(tokens);

        for(ScoredDocument d : top_k)
            System.out.println(String.format("PID: %s\tScore: %f", d.getDocID(), d.getScore()));
    }
}
