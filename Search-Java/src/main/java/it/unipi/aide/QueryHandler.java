package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.List;

/**
 * This class is used by the user to make queries on the InvertedIndex
 */
public class QueryHandler
{
    public static void main(String[] args)
    {
        String INPUT_PATH = "data/out/";
        QueryPreprocessing query = new QueryPreprocessing();
        query.setQuery("Sleeping cat");
        List<String> tokens = query.getTokens();

        DAAT daat = new DAAT( 5);
        List<ScoredDocument> top_k = daat.executeDAAT(tokens);

        for(ScoredDocument d : top_k)
            System.out.println(String.format("PID: %s\tScore: %f", d.getDocID(), d.getScore()));
    }
}
