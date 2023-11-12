package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.Commons;
import it.unipi.aide.utils.Compressor;
import it.unipi.aide.utils.ConfigReader;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DAAT {

    private int K;
    boolean COMPRESSION;

    HashMap<String, TermInfo> terms = new HashMap<>();

    public DAAT(int k){
        this.K = k;
        COMPRESSION = false;
    }

    public List<ScoredDocument> executeDAAT(List<String> queryTerms){
        // Retrieve the posting lists of the query terms
        List<PostingListSkippable> postingLists= new ArrayList<>();
        for(String t : queryTerms){
            /* TODO -> Search inside a cache before performing binary search */
            TermInfo toRetrieve = binarySearch(t);
            terms.put(t, toRetrieve);

            System.out.println(toRetrieve);

            if (toRetrieve == null)
            {
                // TODO -> What if the term is not in the vocabulary?

            }
            else {
                postingLists.add(new PostingListSkippable(toRetrieve));
            }
        }

        // If searched terms are not in the vocabulary, return null
        if(postingLists.isEmpty()) return null;

        List<ScoredDocument> scoredDocuments = new ArrayList<>();
        boolean stop = false;

        while(!stop){
            // Hypothesis that all the Posting Lists are empty
            stop = true;
            int firstDoc = getSmallestDocid(postingLists);
            ScoredDocument toAdd = new ScoredDocument(firstDoc, 0);
            for(PostingListSkippable pl : postingLists){
                // If at least one Posting List has elements, Hypothesis became false
                if (pl.getCurrent() != null) {
                    stop = false;
                    // If Posting List of current term has docId equals to the smallest under consideration, calculate its score
                    if (pl.getCurrent().getDocId() == firstDoc) {
                        // TODO -> Scoring functions apart and well distinct
                        toAdd.setScore(
                                (1 + Math.log(pl.getCurrent().getFrequency())) * Math.log((double) CollectionInformation.getTotalDocuments() / terms.get(pl.getTerm()).getNumPosting())
                        );
                        pl.next();
                    }
                }
            }
            if(!stop)
                scoredDocuments.add(toAdd);
        }

        // Sort the documents by score
        scoredDocuments.sort((o1, o2) -> {
            if(o1.getScore() > o2.getScore()) return -1;
            else if(o1.getScore() < o2.getScore()) return 1;
            else return 0;
        });

        if (scoredDocuments.size() > K)
            return scoredDocuments.subList(0, K);
        // Return top-k documents
        else
            return scoredDocuments;
    }

    private int getSmallestDocid(List<PostingListSkippable> postingLists){
        int min = Integer.MAX_VALUE;

        for(PostingListSkippable pl : postingLists){
            if(pl.getCurrent() != null && pl.getCurrent().getDocId() < min){
                min = pl.getCurrent().getDocId();
            }
        }
        return min;
    }

    private TermInfo binarySearch(String term)
    {
        try(
                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(ConfigReader.getVocabularyPath()),
                        StandardOpenOption.READ);
        )
        {
            long WIN_DOWN = 0;
            long WIN_UP = CollectionInformation.getTotalTerms();
            while (true)
            {
                long MID_POINT = (WIN_UP - WIN_DOWN)/ 2 + WIN_DOWN;
                if(WIN_UP == WIN_DOWN) return null;
                TermInfo middleTerm = getTermFromDisk(channel, MID_POINT);

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
        long off = buffer.getLong();

        return new TermInfo(term, totFreq, nPost, nBlocks, off);
    }
}
