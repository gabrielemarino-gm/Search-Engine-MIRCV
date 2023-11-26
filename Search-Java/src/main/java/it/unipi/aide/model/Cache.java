package it.unipi.aide.model;

import java.util.*;

public class Cache
{
    private final Map<List<String>, QueryResults> queriesResults;

    private final Map<String, TermsPostingLists> postingLists;

    private final int maxSize = 1200;

    public Cache()
    {
        this.queriesResults = new HashMap<>();
        this.postingLists = new HashMap<>();
    }

    /* Query Results Cache */
    public QueryResults getQueryResults(List<String> query)
    {
        return queriesResults.get(query);
    }

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

        return null; // Se non trova una corrispondenza esatta
    }

    public Map<String, List<String>> getTermsInCommonAndNot(List<String> query) {
        Map<String, List<String>> result = new HashMap<>();
        int maxCommonTerms = 0;

        for (Map.Entry<List<String>, QueryResults> entry : queriesResults.entrySet()) {
            List<String> key = entry.getKey();
            QueryResults value = entry.getValue();

            List<String> commonStrings = new ArrayList<>();
            List<String> nonCommonStrings = new ArrayList<>();

            for (String term : key) {
                if (query.contains(term)) {
                    commonStrings.add(term);
                } else {
                    nonCommonStrings.add(term);
                }
            }

            int commonTerms = commonStrings.size();

            if (commonTerms > maxCommonTerms) {
                maxCommonTerms = commonTerms;
                result.clear();  // Cancella eventuali risultati precedenti
                result.put("common", commonStrings);
                result.put("nonCommon", nonCommonStrings);
            }
        }

        return result;
    }

    public void putInQueriesResultsCache(List<String> query, List<ScoredDocument> results) {

        if (isQueriesResultsCacheFull()) {
            makeSpaceInQueriesResultsCache();
        }

        QueryResults queryResults = new QueryResults(results);
        queriesResults.put(query, queryResults);
    }

    private void makeSpaceInQueriesResultsCache() {
        long oldestTimestamp = Long.MAX_VALUE;
        List<String> queryToRemove = null;

        for (Map.Entry<List<String>, QueryResults> entry : queriesResults.entrySet()) {
            long timestamp = entry.getValue().getTimestamp();
            if (timestamp < oldestTimestamp) {
                oldestTimestamp = timestamp;
                queryToRemove = entry.getKey();
            }
        }

        if (queryToRemove != null) {
            queriesResults.remove(queryToRemove);
        }
    }

    public void removeQueryResults(List<String> query)
    {
        queriesResults.remove(query);
    }

    public boolean isQueriesResultsCacheEmpty() {
        return queriesResults.isEmpty();
    }

    /* Returns True if the QueriesResults Cache is full. */
    public boolean isQueriesResultsCacheFull() {
        return (queriesResults.size() == maxSize);
    }

    /* Posting Lists Term Cache */
    public PostingListSkippable getTermsPostingList(String term)
    {
        return postingLists.get(term).getPostingList();
    }

    public void putInPostingListsCache(String term, PostingListSkippable postingListToBeCached)
    {
        if(isTermsPostingListCacheFull())
            makeSpaceInPostingListsCache();

        TermsPostingLists newTermPostingList = new TermsPostingLists(postingListToBeCached);
        postingLists.put(term, newTermPostingList);
    }

    private void makeSpaceInPostingListsCache() {
        long oldestTimestamp = Long.MAX_VALUE;
        String termToRemove = null;

        for (Map.Entry<String, TermsPostingLists> entry : postingLists.entrySet()) {
            long timestamp = entry.getValue().getTimestamp();
            if (timestamp < oldestTimestamp) {
                oldestTimestamp = timestamp;
                termToRemove = entry.getKey();
            }
        }

        if (termToRemove != null) {
            postingLists.remove(termToRemove);
        }
    }

    private boolean isTermsPostingListCacheFull() {
        return (postingLists.size() == maxSize);
    }

    public boolean containsTermsPostingList(String term)
    {
        return postingLists.containsKey(term);
    }

    public void removeTermsPostingList(String term)
    {
        postingLists.remove(term);
    }

    public boolean isTermsPostingListCacheEmpty() {
        return postingLists.isEmpty();
    }

    public static class QueryResults {

        private final List<ScoredDocument> results;

        private final long timestamp;

        public QueryResults(List<ScoredDocument> givenResults) {
            results = givenResults;
            timestamp = System.currentTimeMillis();
        }

        public List<ScoredDocument> getResults() {
            return results;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public static class TermsPostingLists {

        private final PostingListSkippable postingList;

        private final long timestamp;

        public TermsPostingLists (PostingListSkippable givenPostingList) {
            postingList = givenPostingList;
            timestamp = System.currentTimeMillis();
        }

        public PostingListSkippable getPostingList() {
            return postingList;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}