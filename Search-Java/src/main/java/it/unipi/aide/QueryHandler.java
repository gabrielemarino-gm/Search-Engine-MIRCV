package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;

import java.util.List;

/**
 * This class is used by the user to make queries on the InvertedIndex
 */
public class QueryHandler
{


    public static void main(String[] args)
    {
        String INPUT_PATH = "data/out/";

        DAAT daat = new DAAT("data/out/", 5);

        List<String> tokens = new Preprocesser(true).process("Sleeping cat");
        
        List<ScoredDocument> top_k = daat.executeDAAT(tokens);

        for(ScoredDocument d : top_k) System.out.println(String.format("PID: %s\tScore: %f", d.getDocID(), d.getScore()));
    }
}
