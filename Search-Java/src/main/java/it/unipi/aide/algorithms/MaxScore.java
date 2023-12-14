package it.unipi.aide.algorithms;

import it.unipi.aide.model.DocumentIndex;
import it.unipi.aide.model.PostingListSkippable;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.ScoreFunction;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.*;

import static it.unipi.aide.utils.ColorText.ANSI_RESET;
import static it.unipi.aide.utils.ColorText.RED;

/**
 * Class to implement the MaxScore algorithm
 */

public class MaxScore
{
    private int TOP_K = 10;
    private HashMap<String, TermInfo> terms = new HashMap<>();
    private List<PostingListSkippable> postingLists= new ArrayList<>();
    private boolean BM25;
    private final DocumentIndex DOCUMENTINDEX;
    /**
     * Initialization method if needed
     */
    public MaxScore()
    {
        DOCUMENTINDEX = new DocumentIndex(true);
    }

    /**
     * Execute the MaxScore algorithm
     * @param queryTerms List of query terms
     * @return List of top-k scored documents
     */
    public List<ScoredDocument> executeMaxScore(List<String> queryTerms, Boolean bm25, int top_k)
    {
        BM25 = bm25;
        TOP_K = top_k;
        // Retrieve the posting lists of the query terms
        QueryPreprocessing qp = new QueryPreprocessing();
        this.postingLists = qp.retrievePostingList(queryTerms, false);
        terms = qp.getTerms();

        if(postingLists.isEmpty())
        {
            System.out.println(RED + "MAX-SCORE ERR > No posting lists found" + ANSI_RESET);
            return new ArrayList<>();
        }

        // Initial pivot for non-essential lists is the first one
        int pivot = 0;

        // Initial priority queue, in increasing order of score
        // PriorityQueue<ScoredDocument> topKDocs =  new PriorityQueue<>(TOP_K, ScoredDocument.compareTo());
        List<ScoredDocument> topKDocs =  new ArrayList<>();
//        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(TOP_K, Comparator.comparingInt(o -> o));

        // TODO: Forse è meglio usare una PriorityQueue invece di ordinare ogni volta
        // Make sure that the list of PostingList is ordered by increasing upper bound
        if (BM25)
            Collections.sort(postingLists, PostingListSkippable.compareToBM25());
        else
            Collections.sort(postingLists, PostingListSkippable.compareToTFIDF());


        float[] s = new float[postingLists.size()];
        float sigma = 0;

        // Compute the upper bound of each posting list
        for(int i = 0; i < postingLists.size() ; i++)
        {
            sigma += postingLists.get(i).getTermUpperBoundTFIDF();
            s[i] = sigma;
        }

        sigma = 0;

        // Repeat the followings until the top-k documents are retrieved
        // Get minimum docID from the first posting list
        int currentDoc = getMinimumDocID();

        while (pivot < terms.size())
        {
            // If the current docID is equal to the maximum integer value, it means that there are no more documents
            if (currentDoc == Integer.MAX_VALUE)
                break;

            float score = 0;
            int nextDoc = Integer.MAX_VALUE;

            // Take the document length of the current document
            int docLength = 0;
            if(BM25) docLength = DOCUMENTINDEX.getLen(currentDoc);

// ( ESSENTIAL LISTS
            // For current DocID, compute the score of essential lists only
            for (int i = pivot; i < terms.size(); i++)
            {
                if (postingLists.get(i).getCurrentPosting() != null)
                {
                    // if the current docID of the posting list i, is equal to the docID under examination
                    if (postingLists.get(i).getCurrentPosting().getDocId() == currentDoc)
                    {
                        score += computeScore(i, docLength);

                        // Move to the next posting of the posting list i-th
                        postingLists.get(i).next();
                    }
                    if (postingLists.get(i).getCurrentPosting() != null)
                        nextDoc = Math.min(nextDoc, postingLists.get(i).getCurrentPosting().getDocId());
                }
            }
// )

// ( NON-ESSENTIAL LISTS
            // For current DocID, compute the score of non-essential lists only
            for (int i = pivot - 1; i >= 0; i--)
            {
                // If score + upper bound of the first non-essential list is lower than the current sigma,
                // skip the document
                if (score + s[i] <= sigma)
                {
                    break;
                }
                // Every time we calculate a score, we also have to increase the posting list pointer
                // to the posting with the DocID equal to the first docid equal to the next in the essential lists

                // If i get there, the currentDoc still has a chance to surpass the threshold
                // Calculate it's score
                if (postingLists.get(i).nextGEQ(currentDoc) != null)
                {
                    // prima di GEQ - DocID 2604949 | BlockIndexer 90
                    // Compute the score if the current DocID is in the posting list
                    if (postingLists.get(i).getCurrentPosting().getDocId() == currentDoc)
                    {
                        score += computeScore(i, docLength);
                    }
                }
            }
// )

// ( INSERT IN QUEUE AND UPDATE PIVOT
            topKDocs.add(new ScoredDocument(currentDoc, score));

            // Order the List in increasing order of score
            topKDocs.sort((o1, o2) -> {
                return Float.compare(o2.getScore(), o1.getScore());
            });

            // If the queue is full, remove the minimum score
            if (topKDocs.size() > TOP_K)
            {
                topKDocs.remove(TOP_K); // The minimum score is at the top of the queue, because it is ordered in increasing order
                sigma = topKDocs.get(TOP_K - 1).getScore();
            }
            // else just update the current sigma with the minimum score in the queue
            else
            {
                sigma = topKDocs.get(topKDocs.size() - 1).getScore();
            }

            // Update the pivot
            while (pivot < terms.size() && s[pivot] <= sigma)
            {
                pivot++;
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

    /**
     * Compute the score of essential lists only
     * @param postingIndex Index of current the posting list
     * @return Score of essential lists only
     */
    private float computeScore(int postingIndex, int docLength)
    {
        float score;

        if (BM25)
        {
            // Compute BM25 score
            score = ScoreFunction.computeBM25 (
                    postingLists.get(postingIndex).getCurrentPosting().getFrequency(),
                    terms.get(postingLists.get(postingIndex).getTerm()).getNumPosting(),
                    docLength
            );
        }
        else
        {
            // Compute TF-IDF score
            score = ScoreFunction.computeTFIDF (
                    postingLists.get(postingIndex).getCurrentPosting().getFrequency(),
                    terms.get(postingLists.get(postingIndex).getTerm()).getNumPosting()
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