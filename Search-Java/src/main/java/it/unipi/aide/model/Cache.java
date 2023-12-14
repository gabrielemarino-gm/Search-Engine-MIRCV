package it.unipi.aide.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache
{
    /* Cache instance singleton: */
    private static final Cache SearchEngineCache = new Cache();

    /* Cached terms for binary search: */ /* L3 */
    private final LRUCache<Long, String> termPositions = new LRUCache<>();
    private static final int MAX_TERM_POSITION_CACHE_SIZE = 420;


    /* Cached termInfo to avoid binary search */ /* L2 */


    /* Cached postingListSkippable to avoid blocks retrieval */ /* L1 */


    /* Cached compressed docids */ /* Test */


    /* Cached compressed freqs */ /* Test */


    /* Returns the cache instance. Used to make all classes refer to the same cache instance. */
    public static Cache getCacheInstance() { return SearchEngineCache; }


    /* TermInfo handling methods: */
    public boolean containsTermPosition(long termPosition) { return termPositions.containsKey(termPosition); }
    public String getTermPosition(long termPosition) { return termPositions.get(termPosition); }
    public void putTermPosition(long termPosition, String termInfo) { termPositions.put(termPosition, termInfo); }



    /* Class used to implement a LRUCache with removing operation defined when the cache is full. In that case,
    * the least recently used/accessed element will be removed.  */
    public static class LRUCache<K, V> extends LinkedHashMap<K, V>
    {
        public LRUCache() {
            super(MAX_TERM_POSITION_CACHE_SIZE, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > MAX_TERM_POSITION_CACHE_SIZE;
        }
    }
}