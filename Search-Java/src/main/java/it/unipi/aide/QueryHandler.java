package it.unipi.aide;

import it.unipi.aide.algorithms.QueryManager;

/**
 * This class is used by the user to make queries on the InvertedIndex
 */
public class QueryHandler
{
    public static void main(String[] args)
    {
        String INPUT_PATH = "data/out/";

        QueryManager queryManager = new QueryManager(INPUT_PATH);
        queryManager.makeQuery();
    }
}
