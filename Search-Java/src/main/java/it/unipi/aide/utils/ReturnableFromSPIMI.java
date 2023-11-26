package it.unipi.aide.utils;

import java.util.HashMap;

/**
 * This class is used for returning the results of the SPIMI algorithm
 * because we need to return more than one value
 */
public class ReturnableFromSPIMI
{
    private int numBlocks;
    private HashMap<String, Double> maxScoreTFIDF;

    public ReturnableFromSPIMI(int numBlocks, HashMap<String, Double> maxScoreTFIDF)
    {
        this.numBlocks = numBlocks;
        this.maxScoreTFIDF = maxScoreTFIDF;
    }
    {
        this.numBlocks = numBlocks;

    }
    public int getNumBlocks()
    {
        return numBlocks;
    }
    public HashMap<String, Double> getMaxScoreTFIDF()
    {
        return maxScoreTFIDF;
    }
    public void setNumBlocks(int numBlocks)
    {
        this.numBlocks = numBlocks;
    }
    public void setMaxScoreTFIDF(HashMap<String, Double> maxScoreTFIDF)
    {
        this.maxScoreTFIDF = maxScoreTFIDF;
    }

}
