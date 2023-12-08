package it.unipi.aide.algorithms;

import it.unipi.aide.model.DocumentIndex;
import it.unipi.aide.model.PostingListSkippable;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.ScoreFunction;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class DAAT
{
    private int TOP_K;
    private boolean BM25;
    boolean COMPRESSION;
    HashMap<String, TermInfo> terms = new HashMap<>();
    private final DocumentIndex DOCUMENTINDEX;

    public DAAT()
    {
        COMPRESSION = false;
        DOCUMENTINDEX = new DocumentIndex(COMPRESSION);
    }

    public List<ScoredDocument> executeDAAT(List<String> queryTerms, Boolean bm25, int top_k)
    {
        BM25 = bm25;
        TOP_K = top_k;
        QueryPreprocessing qp = new QueryPreprocessing();

        List<PostingListSkippable> postingLists = qp.retrievePostingList(queryTerms);

        if(postingLists.isEmpty())
        {
            System.err.println("No posting lists found");
            return new ArrayList<>();
        }

        terms = qp.getTerms();

        // If searched terms are not in the vocabulary, return null
        if(postingLists.isEmpty())
            return null;

        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(TOP_K, ScoredDocument.compareTo());

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
                if (pl.getCurrentPosting() != null)
                {
                    stop = false;
                    // If Posting List of current term has docId equals to the smallest under consideration, calculate its score
                    if (pl.getCurrentPosting().getDocId() == firstDoc)
                    {
                        if (BM25)
                        {
                            documentToAdd.setScore(ScoreFunction.computeBM25(
                                    pl.getCurrentPosting().getFrequency(),
                                    terms.get(pl.getTerm()).getNumPosting(),
                                    DOCUMENTINDEX.getLen(firstDoc)
                            ));
                        }
                        else
                        {
                            documentToAdd.setScore(ScoreFunction.computeTFIDF(
                                            pl.getCurrentPosting().getFrequency(),
                                            terms.get(pl.getTerm()).getNumPosting()
                                    )
                            );
                        }

                        pl.next();
                    }
                }
            }
            if(!stop)
                scoredDocuments.add(documentToAdd);
        }

        ArrayList<ScoredDocument> result = new ArrayList<>();
        for(int i = 0; i < TOP_K && !scoredDocuments.isEmpty(); i++)
        {
            result.add(scoredDocuments.poll());
        }
        return result;

    }

    private int getSmallestDocid(List<PostingListSkippable> postingLists)
    {
        int min = Integer.MAX_VALUE;

        for(PostingListSkippable pl : postingLists)
        {
            if(pl.getCurrentPosting() != null && pl.getCurrentPosting().getDocId() < min)
            {
                min = pl.getCurrentPosting().getDocId();
            }
        }
        return min;
    }
}
