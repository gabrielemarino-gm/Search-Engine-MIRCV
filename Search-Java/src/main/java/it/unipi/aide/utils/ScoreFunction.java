package it.unipi.aide.utils;

import it.unipi.aide.model.CollectionInformation;

public class ScoreFunction
{
    public static float computeTDIDF(int tf, int df, long N)
    {
        float score = 0;

        if (tf > 0)
            score = (float) ((1 + Math.log(tf)) * Math.log((double) N / df));

        return score;
    }

    public static float computeBM25(int tf, int df, int N, int docLength, int avgDocLength)
    {
        // TODO -> BM25 formula
        return 0;
    }
}
