package it.unipi.aide.utils;

public class ScoreFunction
{
    static final float k1 = 1.2f;
    static final float b = 0.75f;
    public static float computeTFIDF(int tf, int df, long N)
    {
        float score = 0;

        if (tf > 0)
            score = (float) ((1 + Math.log(tf)) * Math.log((double) N / df));

        return score;
    }

    public static float computeBM25(int tf, int df, long N, int docLength, long avgDocLength)
    {
        float score = 0;

        if (tf > 0)
            score = (float) (tf / (((1-b) + b*(docLength/avgDocLength)) + tf) * Math.log((double) N / df));

        return score;
    }
}
