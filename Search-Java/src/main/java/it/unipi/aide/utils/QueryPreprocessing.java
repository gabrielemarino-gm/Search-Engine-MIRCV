package it.unipi.aide.utils;

import it.unipi.aide.model.Cache;
import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.PostingListSkippable;
import it.unipi.aide.model.TermInfo;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class QueryPreprocessing
{
    private final HashMap<String, TermInfo> terms = new HashMap<>();
    private final List<String> tokens = new ArrayList<>();

    Cache cache = Cache.getCacheInstance();

    public QueryPreprocessing() {}

    /**
     * Retrieve the posting lists of the query terms
     *
     * @param queryTerms      List of query terms
     * @param conjunctiveMode
     * @return List of PostingListSkippable, one for each query term
     */
    public List<PostingListSkippable> retrievePostingList(List<String> queryTerms, boolean conjunctiveMode)
    {
        List<PostingListSkippable> postingLists = new ArrayList<>();

        Collections.sort(queryTerms);

        for(String t: queryTerms)
        {

            if(! cache.containsSkippable(t)){
                // Binary search on the vocabulary file for the term of the query
                TermInfo termToRetrieve = binarySearch(t);

                if (termToRetrieve != null) {
                    terms.put(t, termToRetrieve);

                    PostingListSkippable temp = new PostingListSkippable(termToRetrieve);
                    cache.putSkippable(t, temp);

                    postingLists.add(temp);
                } else if (conjunctiveMode)
                    return null;
            }
            else
            {
                PostingListSkippable temp = cache.getSkippable(t);
                terms.put(t, temp.getTermInfo());
                postingLists.add(temp);
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
        // TODO: Use cache to memorize only jumps at first 10 levels
        try(FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(ConfigReader.getVocabularyPath()),
                        StandardOpenOption.READ))
        {
            // Window of the binary search, start from the whole vocabulary file
            long WIN_LOWER_BOUND = 0;
            long WIN_UPPER_BOUND = CollectionInformation.getTotalTerms();
            long prev = -1;
            int level = 0; // TODO: Depth level search

            // Binary search on the vocabulary file
            while (true)
            {
                // Middle point of the window
                long WIN_MIDDLE_POINT = (WIN_UPPER_BOUND - WIN_LOWER_BOUND)/ 2 + WIN_LOWER_BOUND;
                if(prev == WIN_MIDDLE_POINT)
                    break;
                prev = WIN_MIDDLE_POINT;


                // Check if the window is empty
                if(WIN_UPPER_BOUND == WIN_LOWER_BOUND)
                    return null;

                // Check if the term is in the cache
                String middleTermString;
                TermInfo middleTerm = null;
                // TODO: If depth level is less than 10, use cache
                // TODO: Otherwise always read from disk
                /*
                * TODO: Cache size
                *  Memorizing 1 levels: size -> 1
                *  Memorizing 2 levels: size -> 3
                *  Memorizing 3 levels: size -> 7
                *  Memorizing 4 levels: size -> 15
                *  Memorizing 5 levels: size -> 31
                *  Memorizing 6 levels: size -> 63
                *  Memorizing 7 levels: size -> 127
                *  Memorizing 8 levels: size -> 255
                *  Memorizing 9 levels: size -> 511
                *  Memorizing 10 levels: size -> 1023
                *  Memorizing 11 levels: size -> 2047
                *  Memorizing 12 levels: size -> 4095
                */

                if (level < 10)
                {
                    if(cache.containsTermPosition(WIN_MIDDLE_POINT))
                    {
                        middleTermString = cache.getTermPosition(WIN_MIDDLE_POINT);
                    }
                    // If not, get the term from the disk
                    else
                    {
                        middleTerm = getTermFromDisk(channel, WIN_MIDDLE_POINT);
                        middleTermString = middleTerm.getTerm();
                        cache.putTermPosition(WIN_MIDDLE_POINT, middleTermString);
                    }
                }
                else
                {
                    middleTerm = getTermFromDisk(channel, WIN_MIDDLE_POINT);
                    middleTermString = middleTerm.getTerm();
                }

                level ++;

                // Compare the term with the middle term
                int comp = middleTermString.compareTo(term);

                if (comp == 0)
                {
                    // Found
                    if(middleTerm == null)
                        middleTerm = getTermFromDisk(channel, WIN_MIDDLE_POINT);

                    return middleTerm;
                }
                else if (comp > 0)
                {
                    // Right half
                    WIN_UPPER_BOUND = WIN_MIDDLE_POINT;
                }
                else
                {
                    // Second half
                    WIN_LOWER_BOUND = WIN_MIDDLE_POINT;
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

    public HashMap<String, TermInfo> getTerms()
    {
        return terms;
    }
}
