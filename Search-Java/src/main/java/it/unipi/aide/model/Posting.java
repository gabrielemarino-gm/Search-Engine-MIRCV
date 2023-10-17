package it.unipi.aide.model;

public class Posting
{
    private final int docId;
    private int frequency;

    public Posting(int docId){
        this.docId = docId;
        this.frequency = 1;
    }

    public Posting(int docId, int frequency)
    {
        this.docId = docId;
        this.frequency = frequency;
    }

    public int getDocId() {
        return docId;
    }
    public int getFrequency() {return frequency;}

    public void increment() {
        frequency++;
    }

    @Override
    public String toString() {
        return "(" + docId + ":" + frequency + ")";
    }
}