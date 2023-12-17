package it.unipi.aide.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache
{
    /* Cache instance singleton: */
    private static final Cache SearchEngineCache = new Cache();


    /* Cached terms for binary search: */ /* L3 */
    private static final int MAX_TERM_POSITION_CACHE_SIZE = 1100;
    private final LRUCache<Long, String> termPositions = new LRUCache<>(MAX_TERM_POSITION_CACHE_SIZE);


    /* Cached termInfo to avoid binary search */ /* L2 */
    private static final int MAX_TERM_INFO_CACHE_SIZE = 2000;
    private final LRUCache<String, TermInfo> termInfos = new LRUCache<>(MAX_TERM_INFO_CACHE_SIZE);


    /* Cached postingListSkippable to avoid blocks retrieval */ /* L1 */
    private static final int MAX_SKIPPABLE_LIST_CACHE_SIZE = 1000;
    private final LRUCache<String, PostingListSkippable> skippables = new LRUCache<>(MAX_SKIPPABLE_LIST_CACHE_SIZE);

    /* Cached compressed docids */ /* Test */


    /* Cached compressed freqs */ /* Test */


    /* Returns the cache instance. Used to make all classes refer to the same cache instance. */
    public static Cache getCacheInstance() { return SearchEngineCache; }

    /**
     * Clear the cache
     */
    public static void clearCache()
    {
        SearchEngineCache.termPositions.clear();
        SearchEngineCache.termInfos.clear();
        SearchEngineCache.skippables.clear();
    }


    /* TermPositions handling methods: */
    public boolean containsTermPosition(long termPosition) { return termPositions.containsKey(termPosition); }
    public String getTermPosition(long termPosition) { return termPositions.get(termPosition); }
    public void putTermPosition(long termPosition, String termInfo) { termPositions.put(termPosition, termInfo); }


    /* TermInfos handling methods: */
    public boolean containsTermInfo(String term) { return termInfos.containsKey(term); }
    public TermInfo getTermInfo(String term) { return termInfos.remove(term); }
    public void putTermInfo(String term, TermInfo termInfo) { termInfos.put(term, termInfo); }


    /* SkippableLists handling methods: */
    public boolean containsSkippable(String term) { return skippables.containsKey(term); }
    public PostingListSkippable getSkippable(String term) { return skippables.get(term).reset(); }
    public void putSkippable(String term, PostingListSkippable postingList) { skippables.put(term, postingList); }


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
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            if (size() > MAX_SIZE && eldest.getValue() instanceof PostingListSkippable)
                Cache.getCacheInstance().putTermInfo
                        (
                        (String)eldest.getKey(),
                        ((PostingListSkippable)(eldest.getValue())).getTermInfo()
                );
            return size() > MAX_SIZE;
        }
    }

    public int getL1Used(){
        return skippables.size();
    }
    public int getL1Max()
    {
        return MAX_SKIPPABLE_LIST_CACHE_SIZE;
    }

    //
    public int getL2Used()
    {
        return termInfos.size();
    }
    public int getL2Max()
    {
        return MAX_TERM_INFO_CACHE_SIZE;
    }

    // Term Positions
    public int getL3Used()
    {
        return termPositions.size();
    }

    public int getL3Max()
    {
        return MAX_TERM_POSITION_CACHE_SIZE;
    }

}