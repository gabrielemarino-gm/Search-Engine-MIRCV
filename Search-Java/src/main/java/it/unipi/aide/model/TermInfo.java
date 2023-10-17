package it.unipi.aide.model;

public class TermInfo
{
    public final static long SIZE = 64L +4L + 8L + 4L;
    public final static int SIZE_TERM = 64;

    private String term;
    private int totalFrequency;
    private long offset;
    private int numPosting;
    private int numBlocks;

    public TermInfo(String term, int totalFrequency, long offset, int numPosting)
    {
        this.term = term;
        this.totalFrequency = totalFrequency;
        this.offset = offset;
        this.numPosting = numPosting;
        this.numBlocks = 0;
    }

    public TermInfo(String term){
        this.term = term;
        this.totalFrequency = 1;
        this.offset = 0;
        this.numPosting = 1;
        this.numBlocks = 0;
    }

    public void incrementTotalFrequency()
    {
        this.totalFrequency++;
    }

    public void incrementNumPosting()
    {
        this.numPosting++;
    }

    public void incrementNumBlock()
    {
        this.numBlocks++;
    }
    public int getTotalFrequency() {return totalFrequency;}
    public long getOffset() {return offset;}
    public int getNumPosting() {return numPosting;}
    public int getNumBlocks() {return numBlocks;}
    public String getTerm() {return term;}
    public void setTerm(String t) {this.term = t;}
    public void setNumBlocks(int n) {this.numBlocks = n;}
    public void setNumPosting(int n) {this.numPosting = n;}
    public void setTotalFrequency(int f) {this.totalFrequency = f;}
    public void setOffset(long o) {this.offset = o;}
    @Override
    public String toString()
    {
        return "["+term+"]"+"(" + totalFrequency + ", " + offset + ", " + numPosting +")";
    }


}
