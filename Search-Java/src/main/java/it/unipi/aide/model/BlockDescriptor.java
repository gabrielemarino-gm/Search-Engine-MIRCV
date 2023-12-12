package it.unipi.aide.model;

public class BlockDescriptor
{
    public static final long BLOCK_SIZE = 4L + 4L + 8L + 8L + 8L + 8L;
    private int maxDocID;
    private int numPostings;
    private long offsetDocID;
    private long offsetFreq;
    private long bytesDocID;
    private long bytesFreq;

public BlockDescriptor() {}

public BlockDescriptor(int maxDocID, int numPostings, long offsetDocID, long offsetFreq, long bytesDocID, long bytesFreq) {
    this.maxDocID = maxDocID;
    this.numPostings = numPostings;
    this.offsetDocID = offsetDocID;
    this.offsetFreq = offsetFreq;
    this.bytesDocID = bytesDocID;
    this.bytesFreq = bytesFreq;
}
    
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

    @Override
    public String toString() {
        return "BlockDescriptor{" +
                "maxDocID=" + maxDocID +
                ", numPostings=" + numPostings +
                ", offsetDocID=" + offsetDocID +
                ", offsetFreq=" + offsetFreq +
                ", bytesDocID=" + bytesDocID +
                ", bytesFreq=" + bytesFreq +
                "}\n";
    }
}
