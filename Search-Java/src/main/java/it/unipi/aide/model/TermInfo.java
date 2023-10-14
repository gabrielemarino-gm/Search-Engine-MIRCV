package it.unipi.aide.model;

public class TermInfo
{
    private int totalFrequency;
    private int offset;
    private int numPosting;
    public TermInfo()
    {
        this.totalFrequency = 1;
        this.offset = 1;
        this.numPosting = 1;
    }

    public void incrementTotalFrequency()
    {
        this.totalFrequency++;
    }
    public int getTotalFrequency() {return totalFrequency;}
    public int getOffset() {return offset;}
    public int getNumPosting() {return numPosting;}
    public void setNumPosting(int n) {this.numPosting = n;}
    public void setTotalFrequency(int f) {this.totalFrequency = f;}
    public void setOffset(int o) {this.offset = o;}
}
