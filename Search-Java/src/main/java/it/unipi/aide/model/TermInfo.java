package it.unipi.aide.model;

public class TermInfo
{
    private int totalFrequency;
    private int offset;
    private int numPosting;
    public final long SIZE_TERM = 64;
    public TermInfo(int totalFrequency, int offset, int numPosting)
    {
        this.totalFrequency = totalFrequency;
        this.offset = offset;
        this.numPosting = numPosting;
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
    @Override
    public String toString()
    {
        return "(" + totalFrequency + ", " + offset + ", " + numPosting +")";
    }
}
