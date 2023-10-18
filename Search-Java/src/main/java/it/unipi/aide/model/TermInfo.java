package it.unipi.aide.model;

public class TermInfo
{
    public final static long SIZE = 64L +4L + 8L + 4L;
    public final static int SIZE_TERM = 64;

    private String term;
    private int totalFrequency;
    private long offset;    // Used only later in storing/retrieval phase
    private int numPosting;

    /**
     * Create a new TermInfo (USE IN SPIMI ONLY)
     * As we are adding this term for the first time, it will have
     *  TF = 1 and nPostings = 1
     * @param term Term name
     */
    public TermInfo(String term){
        this.term = term;
        this.totalFrequency = 1;
        this.offset = 0;
        this.numPosting = 1;
    }

    /**
     * Create new TermInfo (USE IN MERGE ONLY)
     * @param term Term name
     * @param totalFrequency How many times it appears in the Corpus
     * @param offset At which offset its PostingList begins
     * @param numPosting How many Postings for that term
     */
    public TermInfo(String term, int totalFrequency, long offset, int numPosting)
    {
        this.term = term;
        this.totalFrequency = totalFrequency;
        this.offset = offset;
        this.numPosting = numPosting;
    }

    public void incrementTotalFrequency() {this.totalFrequency++;}
    public void incrementNumPosting() {this.numPosting++;}

    public int getTotalFrequency() {return totalFrequency;}
    public long getOffset() {return offset;}
    public int getNumPosting() {return numPosting;}
    public String getTerm() {return term;}
    public void setTerm(String t) {this.term = t;}
    public void setNumPosting(int n) {this.numPosting = n;}
    public void setTotalFrequency(int f) {this.totalFrequency = f;}
    public void setOffset(long o) {this.offset = o;}

    @Override
    public String toString()
    {
        return "["+term+"]"+"(" + totalFrequency + ", " + offset + ", " + numPosting +")";
    }


}
