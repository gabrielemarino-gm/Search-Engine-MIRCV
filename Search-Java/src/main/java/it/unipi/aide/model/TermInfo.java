package it.unipi.aide.model;

public class TermInfo
{
    private String term;
    private int totalFrequency;
    private int offset;
    private int numPosting;
    private int numBlocks;
    public final long SIZE_TERM = 64;
    public TermInfo(int totalFrequency, int offset, int numPosting)
    {
        this.totalFrequency = totalFrequency;
        this.offset = offset;
        this.numPosting = numPosting;
        this.numBlocks = 0;
    }

    public void incrementTotalFrequency()
    {
        this.numPosting++;
        this.totalFrequency++;
    }
    public void incrementNumBlock()
    {
        this.numBlocks++;
    }
    public int getTotalFrequency() {return totalFrequency;}
    public int getOffset() {return offset;}
    public int getNumPosting() {return numPosting;}
    public int getNumBlocks() {return numBlocks;}
    public String getTerm() {return term;}
    public void setTerm(String t) {this.term = t;}
    public void setNumBlocks(int n) {this.numBlocks = n;}
    public void setNumPosting(int n) {this.numPosting = n;}
    public void setTotalFrequency(int f) {this.totalFrequency = f;}
    public void setOffset(int o) {this.offset = o;}
    @Override
    public String toString()
    {
        return "(" + totalFrequency + ", " + offset + ", " + numPosting +")";
    }


}
