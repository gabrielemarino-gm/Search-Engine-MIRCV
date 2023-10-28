package it.unipi.aide.algorithms;

public class ScoredDocument {

    int docId;
    double score;

    public ScoredDocument(int docId, double score) {
        this.docId = docId;
        this.score = score;
    }

    public int getDocID() {
        return this.docId;
    }

    public double getScore() {
        return this.score;
    }

    public void setScore(double newScore) {
        this.score += newScore;
    }

    public String toString() {
        return "<docid: " + docId + ", score: " + score + ";\n";
    }
}