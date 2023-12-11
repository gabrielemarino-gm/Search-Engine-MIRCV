package it.unipi.aide.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache
{
    /* Cached terms for binary search: */
    private final LRUCache<Long, String> termInfos = new LRUCache<>();
    private static final int MAX_SIZE_BINARY_CACHE = 420;
    private static final Cache SearchEngineCache = new Cache();

    /* Returns the cache instance. Used to make all classes refer to the same cache instance. */
    public static Cache getCacheInstance() {
        return SearchEngineCache;
    }

    /* TermInfo handling methods: */
    public boolean containsTermInfo(long termPosition)
    {
        return termInfos.containsKey(termPosition);
    }

    public String getTermInfo(long termPosition)
    {
        return termInfos.get(termPosition);
    }

    public void putTermIntoTermInfoCache(long termPosition, String termInfo)
    {
        if (termInfos.size() < MAX_SIZE_BINARY_CACHE)
            System.out.println(termInfo);

        termInfos.put(termPosition, termInfo);
    }

    /* Class used to implement a LRUCache with removing operation defined when the cache is full. In that case,
    * the least recently used/accessed element will be removed.  */
    public static class LRUCache<K, V> extends LinkedHashMap<K, V>
    {
        public LRUCache() {
            super(MAX_SIZE_BINARY_CACHE, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            boolean a = size() > MAX_SIZE_BINARY_CACHE;
            if(a)
                System.out.println(eldest);
            return size() > MAX_SIZE_BINARY_CACHE;
        }
    }
}