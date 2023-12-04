package it.unipi.aide.model;

import java.util.*;

public class Cache
{
    /* Cached terms for binary search: */
    private final LRUCache<Long, TermInfo> termInfos = new LRUCache<>();
    private static final int MAX_SIZE = 1200; //todo tocheck
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

    public TermInfo getTermInfo(long termPosition)
    {
        return termInfos.get(termPosition);
    }

    public void putTermIntoTermInfoCache(long termPosition, TermInfo termInfo)
    {
        termInfos.put(termPosition, termInfo);
    }

    /* Class used to implement a LRUCache with removing operation defined when the cache is full. In that case,
    * the least recently used/accessed element will be removed.  */
    public static class LRUCache<K, V> extends LinkedHashMap<K, V>
    {
        public LRUCache() {
            super(MAX_SIZE, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > MAX_SIZE;
        }
    }
}