package it.unipi.aide.testfilespartial.utils;

import it.unipi.aide.model.CollectionInformation;

public class ScoreFunction
{
    static final float k1 = 1.2f;
    static final float b = 0.75f;
    static final long N;
    static final long ADL;

    static
    {
        N = CollectionInformation.getTotalDocuments();
        ADL = CollectionInformation.getAverageDocumentLength();
    }

    public static float computeTFIDF(int tf, int df)
    {
        float score = 0;

        if (tf > 0)
            score = (float) ((1 + Math.log10(tf)) * Math.log10((double) N / df));

        return score;
    }

    public static float computeBM25(int tf, int df, int docLength)
    {
        float score = 0;

        if (tf > 0)
            score = (float) (tf / (k1*((1-b) + b*(docLength/ADL)) + tf) * Math.log10((double) N / df));

        return score;
    }
}
