package it.unipi.aide.utils;

import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.utils.ConfigReader;

public class ScoreFunction
{
    static final float k1 = ConfigReader.getK();
    static final float b = ConfigReader.getB();
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

        if (tf > 0) {
            float idf = (float) Math.log10((double) N / df);
            score = (tf / (k1 * ((1 - b) + b * ((float)docLength / ADL)) + tf) * idf);
        }

        return score;
    }
}
