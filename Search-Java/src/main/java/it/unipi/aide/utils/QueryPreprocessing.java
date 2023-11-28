package it.unipi.aide.utils;

import it.unipi.aide.model.*;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryPreprocessing
{
    private final HashMap<String, TermInfo> terms = new HashMap<>();
    private List<String> tokens = new ArrayList<>();

    Cache cache = Cache.getCacheInstance();

    public QueryPreprocessing() {}

    /* TODO -> Instantiating a new Preprocesser for every new query is time consuming */
    public void setQuery(String query) { tokens = new Preprocesser(true).process(query); }

    /**
     * Retrieve the posting lists of the query terms
     * @param queryTerms List of query terms
     * @return List of PostingListSkippable, one for each query term
     */
    public List<PostingListSkippable> retrievePostingList(List<String> queryTerms)
    {
        List<PostingListSkippable> postingLists = new ArrayList<>();

        for(String t: queryTerms)
        {
            TermInfo toRetrieve = binarySearch(t);
            if(toRetrieve != null)
            {
                terms.put(t, toRetrieve);
                postingLists.add(new PostingListSkippable(toRetrieve));

            }
        }

        return postingLists;
    }

    /**
    *   Get the posting list of a term
    *
    *   @param term String of the term
    *   @return PostingList of the term
    */
    public TermInfo binarySearch(String term)
    {
        try(
                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(ConfigReader.getVocabularyPath()),
                        StandardOpenOption.READ)
        )
        {
            long WIN_DOWN = 0;
            long WIN_UP = CollectionInformation.getTotalTerms();

            // Binary search on the vocabulary file
            while (true)
            {
                long MID_POINT = (WIN_UP - WIN_DOWN)/ 2 + WIN_DOWN;
                if(WIN_UP == WIN_DOWN || MID_POINT == WIN_DOWN) return null;

                TermInfo middleTerm;
                if(cache.containsTermInfo(MID_POINT)) {
                    middleTerm = cache.getTermInfo(MID_POINT);
                }
                else {
                    middleTerm = getTermFromDisk(channel, MID_POINT);
                    cache.putTermIntoTermInfoCache(MID_POINT, middleTerm);
                }

                int comp = middleTerm.getTerm().compareTo(term);
                if (comp == 0)
                {
                    // Found
                    return middleTerm;
                }
                else if (comp > 0)
                {
                    // Right half
                    WIN_UP = MID_POINT;
                }
                else
                {
                    // Second half
                    WIN_DOWN = MID_POINT;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the term from the disk
     * @param channel FileChannel of the vocabulary file
     * @param from Offset of the term
     * @return TermInfo of the term
     * @throws IOException If the file is not found
     */
    private TermInfo getTermFromDisk(FileChannel channel, long from) throws IOException
    {
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,
                from * TermInfo.SIZE_POST_MERGING, TermInfo.SIZE_POST_MERGING);

        byte[] termBytes = new byte[TermInfo.SIZE_TERM];
        buffer.get(termBytes);

        String term = new String(termBytes).trim();
        int totFreq = buffer.getInt();
        int nPost = buffer.getInt();
        int nBlocks = buffer.getInt();
        long offset = buffer.getLong();
        float tfidf = buffer.getFloat();
        float bm25 = buffer.getFloat();

        return new TermInfo(term, totFreq, nPost, offset, nBlocks, tfidf, bm25);
    }

    public List<String> getTokens()
    {
        return tokens;
    }

    public HashMap<String, TermInfo> getTerms()
    {
        return terms;
    }
}
