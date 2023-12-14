package it.unipi.aide.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache
{
    /* Cache instance singleton: */
    private static final Cache SearchEngineCache = new Cache();

    /* Cached terms for binary search: */ /* L3 */
    private static final int MAX_TERM_POSITION_CACHE_SIZE = 420;
    private final LRUCache<Long, String> termPositions = new LRUCache<>(MAX_TERM_POSITION_CACHE_SIZE);


    /* Cached termInfo to avoid binary search */ /* L2 */



    /* Cached postingListSkippable to avoid blocks retrieval */ /* L1 */
    private static final int MAX_POSTING_LIST_CACHE_SIZE = 1400;
    private final LRUCache<String, PostingListSkippable> postingLists = new LRUCache<>(MAX_POSTING_LIST_CACHE_SIZE);

    /* Cached compressed docids */ /* Test */


    /* Cached compressed freqs */ /* Test */


    /* Returns the cache instance. Used to make all classes refer to the same cache instance. */
    public static Cache getCacheInstance() { return SearchEngineCache; }


    /* TermInfo handling methods: */
    public boolean containsTermPosition(long termPosition) { return termPositions.containsKey(termPosition); }
    public String getTermPosition(long termPosition) { return termPositions.get(termPosition); }
    public void putTermPosition(long termPosition, String termInfo) { termPositions.put(termPosition, termInfo); }


    /* SkippableLists handling methods: */
    public boolean containsSkippable(String term) { return postingLists.containsKey(term); }
    public PostingListSkippable getSkippable(String term) { return postingLists.get(term); }
    public void putSkippable(String term, PostingListSkippable postingList) { postingLists.put(term, postingList); }

    /* Class used to implement a LRUCache with removing operation defined when the cache is full. In that case,
    * the least recently used/accessed element will be removed.  */
    public static class LRUCache<K, V> extends LinkedHashMap<K, V>
    {
        int MAX_SIZE;

        public LRUCache(int maxSize) {
            super(maxSize, 0.75f, true);
            MAX_SIZE = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) { return size() > MAX_SIZE; }
    }
}