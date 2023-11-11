package it.unipi.aide.model;

public class TermInfo
{
    public final static int SIZE_TERM = 46;
    public final static long SIZE_PRE_MERGING = SIZE_TERM +4L + 8L + 4L;
    public final static long SIZE_POST_MERGING = SIZE_TERM + 4L + 8L + 8L + 4L + 8L + 8L;


    private String term;
    private int totalFrequency;
    private long docidOffset;
    private long freqOffset;
    private long bytesOccupiedDocid;
    private long bytesOccupiedFreq;
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
        this.docidOffset = 0;
        this.freqOffset = 0;
        this.numPosting = 1;
    }

    /**
     * Create new TermInfo (USE IN MERGE ONLY FOR READ)
     * @param term Term name
     * @param totalFrequency How many times it appears in the Corpus
     * @param docOffset At which offset its PostingList begins (DocIDs)
     * @param freqOffset At which offset its PostingList begins (Frequencies)
     * @param numPosting How many Postings for that term
     */
    public TermInfo(String term, int totalFrequency,
                    long docOffset, long freqOffset,
                    int numPosting)
    {
        this.term = term;
        this.totalFrequency = totalFrequency;
        this.docidOffset = docOffset;
        this.freqOffset = freqOffset;
        this.numPosting = numPosting;
    }

    public TermInfo(String term, int totalFrequency,
                    long docidOffset, long freqOffset,
                    long docidBytes, long freqBytes,
                    int numPosting)
    {
        this(term, totalFrequency, docidOffset, freqOffset, numPosting);
        bytesOccupiedDocid = docidBytes;
        bytesOccupiedFreq = freqBytes;
    }

    public void incrementTotalFrequency() {this.totalFrequency++;}
    public void incrementNumPosting() {this.numPosting++;}

    public int getTotalFrequency() {return totalFrequency;}
    public long getDocidOffset() {return docidOffset;}
    public long getFreqOffset() {return freqOffset;}
    public int getNumPosting() {return numPosting;}
    public String getTerm() {return term;}
    public void setTerm(String t) {this.term = t;}
    public void setNumPosting(int n) {this.numPosting = n;}
    public void setTotalFrequency(int f) {this.totalFrequency = f;}
    public void setDocidOffset(long o) {this.docidOffset = o;}
    public void setFreqOffset(long o) {this.freqOffset = o;}
    public void setBytesOccupiedDocid(long bytesOccupiedDocid) {this.bytesOccupiedDocid = bytesOccupiedDocid;}
    public void setBytesOccupiedFreq(long bytesOccupiedFreq) {this.bytesOccupiedFreq = bytesOccupiedFreq;}
    public long getBytesOccupiedDocid() {return bytesOccupiedDocid;}
    public long getBytesOccupiedFreq() {return bytesOccupiedFreq;}

    @Override
    public String toString()
    {
        return String.format("[%s](%d, %d, %d, %d, %d, %d)", term, totalFrequency, numPosting, docidOffset, freqOffset, bytesOccupiedDocid, bytesOccupiedFreq);
    }
}

/*
 * Tale classe rappresenta un termine del vocabolario.
 *  Essa include tutte le informazioni utili su tale termine da utilizzare in fase di calcolo dello score, nonche
 *  altre informazioni per recuperare le posting list di tale termine dal file principale
 *
 * Si noti come spiegato in Merge.java che per via dello spazio occupato da ogni posting list con o senza compressione,
 *  sono specificati due campi che aiutano a recuperare le liste nel caso in cui fosse stata utilizzata la compressione
 *
 */
