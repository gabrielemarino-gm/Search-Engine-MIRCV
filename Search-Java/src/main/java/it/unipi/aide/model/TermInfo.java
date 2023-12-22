package it.unipi.aide.model;

import it.unipi.aide.utils.ConfigReader;

public class TermInfo {
    public final static int SIZE_TERM = 46;

    public final static long SIZE_PRE_MERGING = SIZE_TERM +
                                                4L +   // totalFrequency
                                                4L +   // numPosting
                                                8L +   // offset
                                                4L +   // maxTF
                                                4L +   // BM25TF
                                                4L;    // BM25DL

    public final static long SIZE_POST_MERGING = SIZE_TERM +
                                                 4L +  // totalFrequency
                                                 4L +  // numPosting
                                                 4L +  // numBlocks
                                                 8L +  // offset
                                                 4L +  // TFIDF
                                                 4L;   // BM25


    private final String term;
    private int totalFrequency;
    private int numPosting;
    private int numBlocks; // Only post-merging
    private long offset;
    private int maxTF; // Only pre-merging
    private int BM25TF; // Only pre-merging
    private int BM25DL; // Only pre-merging

    private float termUpperboundTFIDF;
    private float getTermUpperboundBM25;

    // Before merging is where docids and frequencies stats
    // After merging is the position of first block descriptor

    /**
     * Create a new TermInfo (USE IN SPIMI TO CREATE NEW TERMS)
     * As we are adding this term for the first time
     *
     * @param term Term name
     */
    public TermInfo(String term)
    {
        this.term = term;
        this.totalFrequency = 1;
        this.numPosting = 1;
        this.maxTF = 1;
        this.BM25TF = 1;
        this.BM25DL = 0;
        this.offset = 0;
    }


    /**
     * Create new TermInfo (TEMPLATE)
     *
     * @param term           Term name
     * @param totalFrequency How many times it appears in the Corpus
     * @param offset         Offset in the file where the posting list starts
     * @param numPosting     How many Postings for that term
     */
    private TermInfo(String term, int totalFrequency,
                    int numPosting, long offset) {
        this.term = term;
        this.totalFrequency = totalFrequency;
        this.numPosting = numPosting;
        this.offset = offset;
    }

    /**
     * Create new TermInfo (USE IN MERGE ONLY TO READ TERMS PRODUCED BY SPIMI)
     *
     * @param term      Term name
     * @param frequency How many times it appears in the Corpus
     * @param nPosting  How many Postings for that term
     * @param offset    Offset in the file where the posting list starts
     * @param maxTF     Max TF for that term
     * @param BMTF      Max TF for BM25 for that term
     * @param BMDL      Max DL for BM25 for that term
     */
    public TermInfo(String term, int frequency, int nPosting, long offset, int maxTF, int BMTF, int BMDL)
    {
        this(term, frequency, nPosting, offset);
        this.maxTF = maxTF;
        this.BM25TF = BMTF;
        this.BM25DL = BMDL;
    }

    /**
     * Create new TermInfo (USE IN QUERY PROCESSING TO READ TERMS PRODUCD BY MERGING)
     * @param term Term name
     * @param totalFrequency How many times it appears in the Corpus
     * @param numPosting How many Postings for that term
     * @param nBlocks How many blocks for that term
     * @param offset Offset in the file where the posting list starts
     * @param tfidf TFIDF upperbound for that term
     * @param bm25 BM25 upperbound for that term
     */
    public TermInfo(String term, int totalFrequency, int numPosting, long offset, int nBlocks, float tfidf, float bm25)
    {
        this(term, totalFrequency, numPosting, offset);
        this.numBlocks = nBlocks;
        this.termUpperboundTFIDF = tfidf;
        this.getTermUpperboundBM25 = bm25;
    }

    /**
     *
     * @param tf
     */
    public void setMaxTF(int tf)
    {
        if (this.maxTF < tf)
            this.maxTF = tf;
    }

    /**
     * Set the max BM25
     * @param tf
     * @param dl
     */
    public void setMaxBM25(int tf, int dl)
    {
        if (this.BM25DL == 0)
        {
            this.BM25DL = dl;
            this.BM25TF = tf;
        }
        else if (BMApproximation(tf, dl) > BMApproximation(this.BM25TF, this.BM25DL))
        {
            this.BM25DL = dl;
            this.BM25TF = tf;
        }
    }
    private float BMApproximation(int tf, int dl){
        float k = ConfigReader.getK();
        float b = ConfigReader.getB();

        return (float)tf / (tf + k*(1 - b) + k*b*dl);
    }

    public float getTermUpperBoundTFIDF() { return this.termUpperboundTFIDF; }
    public float getTermUpperBoundBM25() { return this.getTermUpperboundBM25; }

    public void incrementTotalFrequency() {this.totalFrequency++;}
    public void incrementNumPosting() {this.numPosting++;}
    public int getTotalFrequency() {return totalFrequency;}
    public int getNumBlocks() {return numBlocks;}
    public long getOffset() {return offset;}
    public int getNumPosting() {return numPosting;}
    public String getTerm() {return term;}
    public int getMaxTF() {return maxTF;}
    public int getBM25TF() {return BM25TF;}
    public int getBM25DL() {return BM25DL;}
    public void setNumPosting(int n) {this.numPosting = n;}
    public void setTotalFrequency(int f) {this.totalFrequency = f;}
    public void setNumBlocks(int n) {this.numBlocks = n;}
    public void setOffset(long o) {this.offset = o;}

    @Override
    public String toString()
    {
        return String.format("[%s](TF: %d, NP: %d, NB: %d, OS: %d, TUB-TFIDF: %f)", term, totalFrequency, numPosting, numBlocks, offset, termUpperboundTFIDF);
    }
}
