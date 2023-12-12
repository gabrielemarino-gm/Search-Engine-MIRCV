package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.ScoreFunction;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.*;

public class ConjunctiveRetrieval
{
    private int TOP_K;
    private boolean BM25;
    boolean COMPRESSION;
    HashMap<String, TermInfo> terms = new HashMap<>();
    private final DocumentIndex DOCUMENTINDEX;

    public ConjunctiveRetrieval()
    {
        COMPRESSION = false;
        DOCUMENTINDEX = new DocumentIndex(COMPRESSION);
    }

    public List<ScoredDocument> executeConjunctiveRankedRetrieval(List<String> queryTerms, Boolean bm25, int top_k)
    {
        BM25 = bm25;
        TOP_K = top_k;
        QueryPreprocessing qp = new QueryPreprocessing();

        List<PostingListSkippable> postingLists = qp.retrievePostingList(queryTerms, true);

        if(postingLists == null || postingLists.isEmpty())
        {
            System.err.println("No posting lists found");
            return new ArrayList<>();
        }

        terms = qp.getTerms();

        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(TOP_K, ScoredDocument.compareTo());

        orderPostingLists(postingLists);

        Posting currentPosting = postingLists.get(0).getCurrentPosting();

        int i = 1;

        while(currentPosting != null)
        {
            int current = currentPosting.getDocId();

            /****/
            //current = postingLists.get(0).getCurrentPosting().getDocId();

            ScoredDocument documentToAdd = null;

            while(i < postingLists.size())
            {
                PostingListSkippable pl = postingLists.get(i);

                // If at least one Posting List has elements, Hypothesis became false
                if (pl.getCurrentPosting() != null) {

                    // Conjunctive logic
                    pl.nextGEQ(current);

                    if (pl.getCurrentPosting().getDocId() > current)
                    {
                        postingLists.get(0).nextGEQ(pl.getCurrentPosting().getDocId());

                        if (postingLists.get(0).getCurrentPosting().getDocId() > pl.getCurrentPosting().getDocId())
                        {
                            currentPosting = postingLists.get(0).getCurrentPosting();
                            current = postingLists.get(0).getCurrentPosting().getDocId();
                            i = 1;
                        }
                        else
                        {
                            currentPosting = pl.getCurrentPosting();
                            current = currentPosting.getDocId();
                            i = 0;
                        }
                        break;
                    }
                    i += 1;
                }
            }

            if(i == postingLists.size())
            {
                documentToAdd = new ScoredDocument(current, 0);

                for(PostingListSkippable pl: postingLists)
                {
                    if (BM25)
                    {
                        documentToAdd.setScore(ScoreFunction.computeBM25(
                                pl.getCurrentPosting().getFrequency(),
                                terms.get(pl.getTerm()).getNumPosting(),
                                DOCUMENTINDEX.getLen(current)
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
                }

                scoredDocuments.add(documentToAdd);

                postingLists.get(0).next();

                currentPosting = postingLists.get(0).getCurrentPosting();

                i = 1;
            }
        }

        ArrayList<ScoredDocument> result = new ArrayList<>();
        for(int j = 0; j < TOP_K && !scoredDocuments.isEmpty(); j++)
        {
            result.add(scoredDocuments.poll());
        }
        return result;
    }

    private void orderPostingLists(List<PostingListSkippable> postingLists) {

        Comparator<PostingListSkippable> comparator = Comparator.comparingInt(postingList -> postingList.getPostingListsBlockSize());
        Collections.sort(postingLists, comparator);
    }
}