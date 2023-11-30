package it.unipi.aide.model;

import java.util.*;

public class Cache
{
    private final Map<Long, TermCacheInfo> termInfos;
    private final Map<Integer, DocCacheInfo> docInfos;
    private final int maxSize = 1200; //tofix

    private static final Cache SearchEngineCache = new Cache();

    public Cache()
    {
        this.termInfos = new HashMap<>();
        this.docInfos = new HashMap<>();
    }

    public static Cache getCacheInstance() {
        return SearchEngineCache;
    }

    /* Cached terms for binary search */
    public boolean containsTermInfo(long termPosition)
    {
        return termInfos.containsKey(termPosition);
    }

    public TermInfo getTermInfo(long termPosition)
    {
        return termInfos.get(termPosition).getTermInfo();
    }

    public void putTermIntoTermInfoCache(long termPosition, TermInfo termInfo)
    {
        termInfos.put(termPosition, new TermCacheInfo(termInfo));
    }

    public static class TermCacheInfo
    {
        private final TermInfo termInfo;
        private long timestamp;

        public TermCacheInfo(TermInfo ti)
        {
            termInfo = ti;
            timestamp = System.currentTimeMillis();
        }

        public TermInfo getTermInfo()
        {
            timestamp = System.currentTimeMillis();
            return termInfo;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }

    public static class DocCacheInfo
    {
        private final Integer docLenght;

        private long timestamp;

        public DocCacheInfo (int dl)
        {
            docLenght = dl;
            timestamp = System.currentTimeMillis();
        }

        public Integer getDocInfo()
        {
            timestamp = System.currentTimeMillis();
            return docLenght;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }
}