package it.unipi.aide.model;

public class BlockDescriptor {
    
    public static final long BLOCK_SIZE = 4L + 4L + 8L + 8L + 8L + 8L;
    private int maxDocID;
    private int numPostings;
    private long offsetDocID;
    private long offsetFreq;
    private long bytesDocID;
    private long bytesFreq;
    
    
    public void setMaxDocID(int maxDocID) { this.maxDocID = maxDocID; }
    public void setNumPostings(int numPostings) { this.numPostings = numPostings; }
    public void setOffsetDocID(long offsetDocID) { this.offsetDocID = offsetDocID; }
    public void setOffsetFreq(long offsetFreq) { this.offsetFreq = offsetFreq; }
    public void setBytesOccupiedDocid(long bytesDocID) { this.bytesDocID = bytesDocID; }
    public void setBytesOccupiedFreq(long bytesFreq) { this.bytesFreq = bytesFreq; }
    
    
    public int getMaxDocid() { return maxDocID; }
    public int getNumPostings() { return numPostings; }
    public long getOffsetDocid() { return offsetDocID; }
    public long getOffsetFreq() { return offsetFreq; }
    public long getBytesOccupiedDocid() { return bytesDocID; }
    public long getBytesOccupiedFreq() { return bytesFreq; }
}
