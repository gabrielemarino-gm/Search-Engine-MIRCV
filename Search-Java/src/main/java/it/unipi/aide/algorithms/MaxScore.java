package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.QueryPreprocessing;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Class to implement the MaxScore algorithm
 */

public class MaxScore
{


    HashMap<String, TermInfo> terms = new HashMap<>();
    List<PostingListSkippable> postingLists= new ArrayList<>();
    List<ScoredDocument> topKDocs = new ArrayList<>();
    boolean BM25 = false;
    /**
     * Initialization method if needed
     */
    public MaxScore(Boolean bm25)
    {
        BM25 = bm25;
    }       

    /*
        TODO -> Remember: Non-essential lists are those with an upper bound lower than sigma

        TODO -> Remember: If a document is not in an essential list, it cannot be in the top-k,
                    which means that we can skip it if
     */

    /**
     * Execute the MaxScore algorithm
     * @param queryTerms List of query terms
     * @param kDocs Top-K documents to retrieve
     * @return List of top-k scored documents
     */
    // TODO -> Consider using a PriorityQueue

    /* TODO -> Use another Class instead of PostingList, which includes the upper bound
     *          of the term in the posting list
     */
    public List<ScoredDocument> executeMaxScore(List<String> queryTerms, int kDocs)
    {
        // Retrieve the posting lists of the query terms
        List<PostingListSkippable> postingLists;
        QueryPreprocessing qp = new QueryPreprocessing();
        postingLists = qp.retrievePostingList(queryTerms);
        terms = qp.getTerms();

        // Initial threshold sigma is equal to 0
        float sigma = 0;
        // Initial pivot for non-essential lists is the first one
        int pivot = 0;

        // Make sure that the list of PostingList is ordered by increasing upper bound
        if (BM25)
            Collections.sort(postingLists, PostingListSkippable.compareToBM25());
        else
            Collections.sort(postingLists, PostingListSkippable.compareToTFIDF());


        // TODO -> Repeat the followings until the top-k documents are retrieved

        // Get minimum docID from the first posting list
        int currentDoc = getMinimumDocID();

        while (pivot < terms.size())
        {
            float score = 0;
            int next = Integer.MAX_VALUE;

            // For current DocID, compute the score of essential lists only
            for (int i = pivot; i < terms.size() - 1; i++)
            {
                if (postingLists.get(i).getCurrent().getDocId() == currentDoc)
                {
                    score = computeEssentialScore(pivot);
                    next = postingLists.get(i).next().getDocId();
                }

                if (postingLists.get(i).getCurrent().getDocId() < next)
                {
                    next = postingLists.get(i).getCurrent().getDocId();
                }
            }

            // For current DocID, compute the score of non-essential lists only
            for (int i = pivot - 1; i >= 0; i--)
            {
                // If score + upper bound of the first non-essential list is lower than the current sigma,
                // skip the document
                if (score + postingLists.get(i).getTermUpperBoundTFIDF() <= sigma)
                {
                    break;
                }

                // Every time we calculate a score, we also have to increase the posting list pointer
                postingLists.get(i).nextGEQ(currentDoc);
                if (postingLists.get(i).getCurrent().getDocId() == currentDoc)
                {
                    score = computeNonEssentialScore(pivot);
                }
            }
        }


        /* TODO -> If score + upper bound of the first non-essential list is lower than the current sigma,
         *          skip the document
         */

        /* TODO -> Every time we calculate a score, we also have to increase the posting list pointer
         *          of the current non-essential list using nextGEQ() method
         */

        // Update the current sigma
        // Update the pivot

        return topKDocs.subList(0, kDocs - 1);
    }

    /**
     * Return the minimum DocID from each posting list
     * @return Minimum DocID
     */
    private int getMinimumDocID() {
        return 0;
    }

    /**
     * Compute the score of essential lists only
     * @param pivot Current pivot
     * @return Score of essential lists only
     */
    private float computeEssentialScore(int pivot)
    {
        for (int i = pivot; i <= terms.size() - 1; i++)
        {
            if (BM25)
            {
                // TODO -> Compute BM25 score
            }
            else
            {
                // TODO -> Compute TF-IDF score

            }
        }
        return 0;
    }

    /**
     * Compute the score of non-essential lists only
     * @param pivot Current pivot
     * @return Score of non-essential lists only
     */
    private float computeNonEssentialScore(int pivot)
    {
        return 0;
    }
}

/*
Input:
An array p of n posting lists, one per query term,
sorted in increasing order of max score contribution
An array σ of n max score contributions, one per query term,
sorted in increasing order

Output:
A priority queue q of (at most) the top K <docid, score> pairs,
in decreasing order of score

MaxScore(p,σ):
    q ← a priority queue of (at most) K <docid, score> pairs,
sorted in decreasing order of score
    ub ← an array of n document upper bounds, one per posting list,
all entries initialised to 0

    ub[0] ← σ[0]
    for i ← 1 to n − 1 do
    ub[i] ← ub[i − 1] + σ[i]

    θ ← 0
    pivot ← 0
    current ← MinimumDocid(p)
    while pivot < n and current != null do
        score ← 0
        next ← +∞
        for i ← pivot to n − 1 do
            if p[i].docid() = current then
                score ← score + p[i].score()
                p[i].next()

            if p[i].docid() < next then
                next ← p[i].docid()

        for i ← pivot − 1 to 0 do
            if score + ub[i] ≤ θ then
                break

            p[i].next(current)
            if p[i].docid() = current then
                score ← score + p[i].score()

        if q.push(<current, score>) then
            θ ← q.min()
            while pivot < n and ub[pivot] ≤ θ do
                pivot ← pivot + 1

        current ← next

return q
 */