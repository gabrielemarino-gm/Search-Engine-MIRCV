package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DAAT
{
    private final int K;
    boolean COMPRESSION;

    HashMap<String, TermInfo> terms = new HashMap<>();

    public DAAT(int k)
    {
        this.K = k;
        COMPRESSION = false;
    }

    public List<ScoredDocument> executeDAAT(List<String> queryTerms)
    {
        // Retrieve the posting lists of the query terms
        List<PostingListSkippable> postingLists;
        QueryPreprocessing qp = new QueryPreprocessing();
        postingLists = qp.retrievePostingList(queryTerms);
        terms = qp.getTerms();

        // If searched terms are not in the vocabulary, return null
        if(postingLists.isEmpty())
            return null;

        List<ScoredDocument> scoredDocuments = new ArrayList<>();
        boolean stop = false;

        while(!stop)
        {
            // Hypothesis that all the Posting Lists are empty
            stop = true;
            int firstDoc = getSmallestDocid(postingLists);
            ScoredDocument documentToAdd = new ScoredDocument(firstDoc, 0);

            for(PostingListSkippable pl : postingLists)
            {
                // If at least one Posting List has elements, Hypothesis became false
                if (pl.getCurrent() != null)
                {
                    stop = false;
                    // If Posting List of current term has docId equals to the smallest under consideration, calculate its score
                    if (pl.getCurrent().getDocId() == firstDoc)
                    {
                        documentToAdd.setScore(ScoreFunction.computeTFIDF(
                                pl.getCurrent().getFrequency(),
                                terms.get(pl.getTerm()).getTotalFrequency(),
                                CollectionInformation.getTotalDocuments()));

                        pl.next();
                    }
                }
            }
            if(!stop)
                scoredDocuments.add(documentToAdd);
        }

        // Sort the documents by score
        scoredDocuments.sort((o1, o2) ->
        {
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

    private int getSmallestDocid(List<PostingListSkippable> postingLists)
    {
        int min = Integer.MAX_VALUE;

        for(PostingListSkippable pl : postingLists)
        {
            if(pl.getCurrent() != null && pl.getCurrent().getDocId() < min)
            {
                min = pl.getCurrent().getDocId();
            }
        }
        return min;
    }
}
