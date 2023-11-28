package it.unipi.aide.model;

import java.util.*;

public class Cache
{
    private final Map<List<String>, QueryResults> queriesResults;
    private final Map<String, TermsPostingLists> postingLists;
    private final int maxSize = 1200; //tofix
    public Cache()
    {
        this.queriesResults = new HashMap<>();
        this.postingLists = new HashMap<>();
    }

    /**
     * Returns the list of results of the query if it is contained in the cache, null otherwise.
     * @param query The query to be searched in the cache.
     * @return The list of results of the query if it is contained in the cache, null otherwise.
     */
    public List<ScoredDocument> containsQueryResults(List<String> query)
    {
        List<ScoredDocument> results;

        for (Map.Entry<List<String>, QueryResults> entry : queriesResults.entrySet())
        {
            List<String> key = entry.getKey();
            if (key.equals(query))
            {
                results = entry.getValue().getResults();
                return results;
            }
        }

        return null;
    }

    /**
     * Returns the list of terms in common and not between the query and the cached queries.
     * @param query The query to be compared with the cached queries.
     * @return A map containing the list of terms in common and not between the query and the cached queries.
     */
    public Map<String, List<String>> getTermsInCommonAndNot(List<String> query)
    {
        Map<String, List<String>> result = new HashMap<>();
        int maxCommonTerms = 0;

        for (Map.Entry<List<String>, QueryResults> entry : queriesResults.entrySet())
        {
            List<String> key = entry.getKey();

            List<String> commonStrings = new ArrayList<>();
            List<String> nonCommonStrings = new ArrayList<>();

            for (String term : key)
            {
                if (query.contains(term))
                    commonStrings.add(term);
                else
                    nonCommonStrings.add(term);
                query.remove(term);
            }

            int commonTerms = commonStrings.size();

            if (commonTerms > maxCommonTerms)
            {
                maxCommonTerms = commonTerms;
                result.clear();
                result.put("common", commonStrings);
                result.put("nonCommon", nonCommonStrings);
            }
        }

        result.put("nonCommon", query);

        return result;
    }

    /**
     * Returns the list of results of the query if it is contained in the cache, null otherwise.
     * @param query The query to be searched in the cache.
     * @return The list of results of the query if it is contained in the cache, null otherwise.
     */
    public void putInQueriesResultsCache(List<String> query, List<ScoredDocument> results)
    {
        if (isQueriesResultsCacheFull())
            makeSpaceInQueriesResultsCache();

        QueryResults queryResults = new QueryResults(results);
        queriesResults.put(query, queryResults);
    }

    /**
     * This method removes the oldest query from the cache.
     * */
    private void makeSpaceInQueriesResultsCache()
    {
        long oldestTimestamp = Long.MAX_VALUE;
        List<String> queryToRemove = null;

        for (Map.Entry<List<String>, QueryResults> entry : queriesResults.entrySet())
        {
            long timestamp = entry.getValue().getTimestamp();

            if (timestamp < oldestTimestamp)
            {
                oldestTimestamp = timestamp;
                queryToRemove = entry.getKey();
            }
        }

        if (queryToRemove != null)
            queriesResults.remove(queryToRemove);
    }

    /**
     *  Returns True if the QueriesResults Cache is full.
     */
    public boolean isQueriesResultsCacheFull() {
        return (queriesResults.size() == maxSize);
    }

    /**
     *  Posting Lists Term Cache
     */
    public PostingListSkippable getTermsPostingList(String term)
    {
        if(containsTermsPostingList(term))
            return postingLists.get(term).getPostingList();
        else
            return null;
    }

    /**
     *  Posting Lists Term Cache
     *  @param term The term to be searched in the cache.
     *  @param postingListToBeCached The posting list of the term to be cached.
     *  @return The posting list of the term if it is contained in the cache, null otherwise.
     */
    public void putInPostingListsCache(String term, PostingListSkippable postingListToBeCached)
    {
        if(isTermsPostingListCacheFull())
            makeSpaceInPostingListsCache();

        TermsPostingLists newTermPostingList = new TermsPostingLists(postingListToBeCached);
        postingLists.put(term, newTermPostingList);
    }

    /**
     * This method removes the oldest term from the cache.
     * */
    private void makeSpaceInPostingListsCache()
    {
        long oldestTimestamp = Long.MAX_VALUE;
        String termToRemove = null;

        for (Map.Entry<String, TermsPostingLists> entry : postingLists.entrySet())
        {
            long timestamp = entry.getValue().getTimestamp();

            if (timestamp < oldestTimestamp)
            {
                oldestTimestamp = timestamp;
                termToRemove = entry.getKey();
            }
        }

        if (termToRemove != null)
            postingLists.remove(termToRemove);
    }

    private boolean isTermsPostingListCacheFull()
    {
        return (postingLists.size() == maxSize);
    }

    public boolean containsTermsPostingList(String term)
    {
        return postingLists.containsKey(term);
    }

    public static class QueryResults
    {
        private final List<ScoredDocument> results;
        private long timestamp;

        public QueryResults(List<ScoredDocument> givenResults)
        {
            results = givenResults;
            timestamp = System.currentTimeMillis();
        }

        public List<ScoredDocument> getResults()
        {
            timestamp = System.currentTimeMillis();
            return results;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }

    public static class TermsPostingLists
    {
        private final PostingListSkippable postingList;

        private long timestamp;

        public TermsPostingLists (PostingListSkippable givenPostingList)
        {
            postingList = givenPostingList;
            timestamp = System.currentTimeMillis();
        }

        public PostingListSkippable getPostingList()
        {
            timestamp = System.currentTimeMillis();
            return postingList;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }
}