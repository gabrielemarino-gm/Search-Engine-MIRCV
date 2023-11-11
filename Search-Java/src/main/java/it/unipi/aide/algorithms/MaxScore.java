package it.unipi.aide.algorithms;

import it.unipi.aide.model.PostingList;
import it.unipi.aide.model.ScoredDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to implement the MaxScore algorithm
 */

public class MaxScore {

    /**
     * Initialization method if needed
     */
    public MaxScore() {

    }

    /*
        TODO -> Remember: Non-essential lists are those with an upper bound lower than sigma

        TODO -> Remember: If a document is not in an essential list, it cannot be in the top-k,
                    which means that we can skip it if
     */

    /**
     * Execute the MaxScore algorithm
     * @param d List of PostingList, one for each query term, including term upper bound
     * @param k Top-K documents to retrieve
     * @return List of top-k scored documents
     */
    // TODO -> Consider using a PriorityQueue

    /* TODO -> Use another Class instead of PostingList, which includes the upper bound
     *          of the term in the posting list
     */
    public List<ScoredDocument> execute (List<PostingList> d, int k) {
        // List of top-k scored documents initially empty
        List<ScoredDocument> topK = new ArrayList<>();

        // Initial threshold sigma is equal to 0
        float sigma = 0;
        // Initial pivot for non-essential lists is the first one
        int pivot = 0;


        // Make sure that the list of PostingList is ordered by increasing upper bound
        // TODO -> Order the list of PostingList by increasing upper bound

        // TODO -> Repeat the followings util every document is examined

        // Get minimum docID from the first posting list
        int minDocID = getMinimumDocID();

        float score = 0;
        // For current DocID, compute the score of essential lists only
        score += computeEssentialScore(d, pivot);

        // Also compute the score for each non-essential list
        score += computeNonEssentialScore(d, pivot);

        // TODO -> Repeat the followings for every non-essential list (from pivot-1 to 0)

        /* TODO -> If score + upper bound of the first non-essential list is lower than the current sigma,
         *          skip the document
         */


        /* TODO -> Every time we calculate a score, we also have to increase the posting list pointer
         *          of the current non-essential list using nextGEQ() method
         */

        // Update the current sigma
        // Update the pivot

        return topK.subList(0, k - 1);
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
     * @param d List of PostingList, one for each query term, including term upper bound
     * @param pivot Current pivot
     * @return Score of essential lists only
     */
    private float computeEssentialScore(List<PostingList> d, int pivot) {
        return 0;
    }

    /**
     * Compute the score of non-essential lists only
     * @param d List of PostingList, one for each query term, including term upper bound
     * @param pivot Current pivot
     * @return Score of non-essential lists only
     */
    private float computeNonEssentialScore(List<PostingList> d, int pivot) {
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