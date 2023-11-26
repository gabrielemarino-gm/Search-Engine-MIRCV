package it.unipi.aide.model;

import it.unipi.aide.utils.ScoreFunction;

public class TermInfo
{
    public final static int SIZE_TERM = 46;
    // Term, totalFrequency, numPosting, offset
    public final static long SIZE_PRE_MERGING = SIZE_TERM  + 4L + 4L + 8L + 4L + 4L;
    //
    public final static long SIZE_POST_MERGING = SIZE_TERM + 4L + 4L + 4L + 8L + 4L + 4L;


    private String term;
    private int totalFrequency;
    private int numPosting;
    private int numBlocks; // Only post-merging
    private long offset;
    private int termFrequency;
    private float termUpperBoundTDIDF;
    private float termUpperBoundBM25;

    // Before merging is where docids and frequencies stats
    // After merging is the position of first block descriptor

    /**
     * Create a new TermInfo (USE IN SPIMI ONLY)
     * As we are adding this term for the first time, it will have
     *  TF = 1 and nPostings = 1
     * @param term Term name
     */
    public TermInfo(String term){
        this.term = term;
        this.totalFrequency = 1;
        this.numPosting = 1;
        this.offset = 0;
    }

    /**
     * Create new TermInfo (USE IN MERGE ONLY FOR READ)
     * @param term Term name
     * @param totalFrequency How many times it appears in the Corpus
     * @param offset Offset in the file where the posting list starts
     * @param numPosting How many Postings for that term
     */
    public TermInfo(String term, int totalFrequency,
                    int numPosting, long offset)
    {
        this.term = term;
        this.totalFrequency = totalFrequency;
        this.numPosting = numPosting;
        this.offset = offset;
    }
    public TermInfo(String term, int totalFrequency,
                    int numPosting, int nBlocks, long offset)
    {
        this(term, totalFrequency, numPosting, offset);
        this.numBlocks = nBlocks;
    }

    /**
     * Create new TermInfo (USE IN MERGE ONLY FOR WRITE)
     * @param term Term name
     * @param frequency How many times it appears in the Corpus
     * @param nPosting How many Postings for that term
     * @param i How many blocks for that term
     * @param offset Offset in the file where the posting list starts
     * @param upperBoundTDIDF Partial term upper bound for the score of the term
     * @param upperBoundBM25 Partial term upper bound for the score of the term
     */
    public TermInfo(String term, int frequency, int nPosting, int i, long offset, float upperBoundTDIDF, float upperBoundBM25)
    {
        this.term = term;
        this.totalFrequency = frequency;
        this.numPosting = nPosting;
        this.numBlocks = i;
        this.offset = offset;
        this.termUpperBoundTDIDF = upperBoundTDIDF;
        this.termUpperBoundBM25 = upperBoundBM25;
    }

    public void setTermUpperBoundWithIntermediateResultsTDIDF(double maxScoreTFIDF, int documentFrequency)
    {
        this.termUpperBoundTDIDF = (float) (maxScoreTFIDF * Math.log((double) CollectionInformation.getTotalDocuments() / documentFrequency));
    }
    public void evaluateTermUpperBoundTDIDF(int tf, int df)
    {
        float score = ScoreFunction.computeTFIDF(tf, df, CollectionInformation.getTotalDocuments());

        if (this.termUpperBoundTDIDF < score)
            this.termUpperBoundTDIDF = score;
    }
    public void evaluateTermUpperBoundBM25(int tf, int df, int docLength)
    {
        float score = ScoreFunction.computeBM25(tf, df, CollectionInformation.getTotalDocuments(), docLength,
                CollectionInformation.getAverageDocumentLength());

        if (this.termUpperBoundBM25 < score)
            this.termUpperBoundBM25 = score;
    }

    public void incrementTotalFrequency() {this.totalFrequency++;}
    public void incrementNumPosting() {this.numPosting++;}


    public int getTotalFrequency() {return totalFrequency;}
    public int getNumBlocks() {return numBlocks;}
    public long getOffset() {return offset;}
    public int getNumPosting() {return numPosting;}
    public String getTerm() {return term;}
    public int getTermFrequency() {return termFrequency;}
    public float getTermUpperBoundTDIDF() {return termUpperBoundTDIDF;}
    public float getTermUpperBoundBM25() {return termUpperBoundBM25;}

    public void setNumPosting(int n) {this.numPosting = n;}
    public void setTotalFrequency(int f) {this.totalFrequency = f;}
    public void setNumBlocks(int n) {this.numBlocks = n;}
    public void setOffset(long o) {this.offset = o;}
    public void setTermFrequency(int f) {this.termFrequency = f;}
    public void setTermUpperBoundTDIDF(float f) {this.termUpperBoundTDIDF = f;}
    public void setTermUpperBoundBM25(float f) {this.termUpperBoundBM25 = f;}

    @Override
    public String toString()
    {
        return String.format("[%s](TF: %d, NP: %d, NB: %d, OS: %d)", term, totalFrequency, numPosting, numBlocks, offset);
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
