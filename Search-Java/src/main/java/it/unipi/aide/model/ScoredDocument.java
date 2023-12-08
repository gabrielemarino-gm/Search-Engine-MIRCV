package it.unipi.aide.model;

import java.util.Comparator;

public class ScoredDocument
{
    int docId;
    float score;

    public ScoredDocument(int docId, float score)
    {
        this.docId = docId;
        this.score = score;
    }

    /**
     * Comparator for ScoredDocument objects.
     * This method orders the documents by score, from lowest to highest.
     * @return Comparator object.
     */
    public static Comparator<? super ScoredDocument> compareTo()
    {
        return new Comparator<ScoredDocument>()
        {
            @Override
            public int compare(ScoredDocument o1, ScoredDocument o2)
            {
                return -Float.compare(o1.getScore(), o2.getScore());
            }
        };
    }

    public int getDocID() { return this.docId; }
    public float getScore() { return this.score; }
    public void setScore(double newScore) { this.score += newScore; }
    public String toString()
    {
        return "docid: " + docId + " | score: " + score + "\n";
    }
}