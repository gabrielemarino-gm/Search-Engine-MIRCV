package it.unipi.aide.model;

class Posting {
    private final int docId;
    private int count;

    public Posting(int docId) {
        this.docId = docId;
        this.count = 1;
    }

    public int getDocId() {
        return docId;
    }

    public void increment() {
        count++;
    }

    @Override
    public String toString() {
        return "(" + docId + ":" + count + ")";
    }
}