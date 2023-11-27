package it.unipi.aide.model;

public class Posting
{
    private final int docId;
    private int frequency;

    /**
     * Create a new Posting with term frequency = 1 (USE IN SPIMI ONLY)
     * @param docId DocID of the document to add
     */
    public Posting(int docId){
        this.docId = docId;
        this.frequency = 1;
    }

    /**
     * Create a new Posting (USE IN MERGE AND QUERY ONLY)
     * @param docId DocID of the document to add
     * @param frequency Frequency of term in that document
     */
    public Posting(int docId, int frequency)
    {
        this.docId = docId;
        this.frequency = frequency;
    }

    public int getDocId() {
        return docId;
    }
    public int getFrequency() {
        return frequency;
    }

    /**
     * Increment frequency by one (USE IN SPIMI ONLY)
     */
    public void incrementFrequency() {
        frequency++;
    }

    @Override
    public String toString() {
        return "(" + docId + ":" + frequency + ")";
    }
}