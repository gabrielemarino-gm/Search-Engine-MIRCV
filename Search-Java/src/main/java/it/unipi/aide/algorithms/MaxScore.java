package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.QueryPreprocessing;
import it.unipi.aide.utils.ScoreFunction;

import java.util.*;

/**
 * Class to implement the MaxScore algorithm
 */

public class MaxScore
{
    HashMap<String, TermInfo> terms = new HashMap<>();
    List<PostingListSkippable> postingLists= new ArrayList<>();
    boolean BM25;
    /**
     * Initialization method if needed
     */
    public MaxScore(Boolean bm25)
    {
        BM25 = bm25;
    }       

    /**
     * Execute the MaxScore algorithm
     * @param queryTerms List of query terms
     * @param kDocs Top-K documents to retrieve
     * @return List of top-k scored documents
     */

    public PriorityQueue<ScoredDocument> executeMaxScore(List<String> queryTerms, int kDocs)
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
        // Initial priority queue
        PriorityQueue<ScoredDocument> topKDocs = new PriorityQueue<>(kDocs, ScoredDocument.compareTo());

        // TODO -> Forse è meglio usare una PriorityQueue invece di ordinare ogni volta
        // Make sure that the list of PostingList is ordered by increasing upper bound
        if (BM25)
            Collections.sort(postingLists, PostingListSkippable.compareToBM25());
        else
            Collections.sort(postingLists, PostingListSkippable.compareToTFIDF());


        // Repeat the followings until the top-k documents are retrieved
        // Get minimum docID from the first posting list
        int currentDoc = getMinimumDocID();
        while (pivot < terms.size())
        {
            float score = 0;
            int nextDoc = Integer.MAX_VALUE;

// ( ESSENTIAL LISTS
            // For current DocID, compute the score of essential lists only
            for (int i = pivot; i < terms.size() - 1; i++)
            {
                if (postingLists.get(i).getCurrent().getDocId() == currentDoc)
                {
                    score += computeScore(i);
                    nextDoc = postingLists.get(i).next().getDocId();
                }

                if (postingLists.get(i).getCurrent().getDocId() < nextDoc)
                {
                    nextDoc = postingLists.get(i).getCurrent().getDocId();
                }
            }
// )

// ( NON-ESSENTIAL LISTS
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
                // to the posting with the DocID equal to the first docid equal to the next in the essential lists
                postingLists.get(i).nextGEQ(currentDoc);

                // Compute the score if the current DocID is in the posting list
                if (postingLists.get(i).getCurrent().getDocId() == currentDoc)
                {
                    score += computeScore(i);
                }
            }
// )

// ( INSERT IN QUEUE AND UPDATE PIVOT
            if (topKDocs.add(new ScoredDocument(currentDoc, score)))
            {
                // Update the current sigma
                sigma = topKDocs.peek().getScore();

                // Update the pivot
                while (pivot < terms.size() && postingLists.get(pivot).getTermUpperBoundTFIDF() <= sigma)
                {
                    pivot++;
                }
            }
// )
            // Update the current docID
            currentDoc = nextDoc;
        }

        return topKDocs;
    }

    /**
     * Return the minimum DocID from each posting list
     * @return Minimum DocID
     */
    private int getMinimumDocID()
    {
        // TODO -> Check if this is correct
        int min = Integer.MAX_VALUE;

        for (PostingListSkippable pl : postingLists)
        {
            if (pl.getCurrent().getDocId() < min)
            {
                min = pl.getCurrent().getDocId();
            }
        }
        return min;
    }

    /**
     * Compute the score of essential lists only
     * @param postingIndex Index of current the posting list
     * @return Score of essential lists only
     */
    private float computeScore(int postingIndex)
    {
        float score = 0;

        if (BM25)
        {
            // TODO -> Compute BM25 score

        }
        else
        {
            // Compute TF-IDF score
            score = ScoreFunction.computeTFIDF (
                    postingLists.get(postingIndex).getCurrent().getFrequency(),
                    terms.get(postingLists.get(postingIndex).getTerm()).getTotalFrequency(),
                    CollectionInformation.getTotalDocuments()
            );
        }


        return score;
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