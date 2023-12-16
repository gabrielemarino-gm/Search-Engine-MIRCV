package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.ScoreFunction;
import it.unipi.aide.utils.QueryPreprocessing;

import java.util.*;

import static it.unipi.aide.utils.beautify.ColorText.*;

public class ConjunctiveRetrieval
{
    private int TOP_K;
    private boolean BM25;
    boolean COMPRESSION;
    HashMap<String, TermInfo> terms = new HashMap<>();
    private final DocumentIndex DOCUMENTINDEX;

    public ConjunctiveRetrieval()
    {
        DOCUMENTINDEX = new DocumentIndex(true);
    }

    /**
     * Execute the Conjunctive Ranked Retrieval algorithm
     * @param queryTerms List of query terms
     * @param bm25 Boolean to indicate if BM25 should be used
     * @param top_k Number of top-k documents to retrieve
     *
     * @return List of top-k scored documents
     */
    public List<ScoredDocument> executeConjunctive(List<String> queryTerms, Boolean bm25, int top_k)
    {
        BM25 = bm25;
        TOP_K = top_k;

        QueryPreprocessing qp = QueryPreprocessing.getInstance();

        List<PostingListSkippable> postingLists = qp.retrievePostingList(queryTerms, true);

        if(postingLists == null || postingLists.isEmpty())
        {
            // System.out.println(RED + "Conjunctive Retrieval ERR > No posting lists found" + ANSI_RESET);
            return new ArrayList<>();
        }

        terms = qp.getTerms();
        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(TOP_K, ScoredDocument.compareTo());
        orderPostingLists(postingLists);

        // Get the first Posting of the first Posting List (The shortest one)
        PostingListSkippable firstPL = postingLists.get(0);

        // While firstPL list has elements
        extern:
        while(firstPL.getCurrentPosting() != null)
        {
            // Get first docID
            int currentDocID = firstPL.getCurrentPosting().getDocId();

            // Create a new ScoredDocument with the current docID and score 0
            ScoredDocument documentToAdd = new ScoredDocument(currentDocID, 0);

            // Compute the score of the document
            giveScore(documentToAdd, firstPL, currentDocID);

            // For each other posting list
            for(int i = 1; i < postingLists.size(); i++)
            {
                PostingListSkippable pl = postingLists.get(i);

                // If it's not empty
                if (pl.getCurrentPosting() != null)
                {
                    // Take the next element greater or equal
                    pl.nextGEQ(currentDocID);

                    // If this cause the posting list to be empty, return the top-k documents found
                    if (pl.getCurrentPosting() == null)
                    {
                        break extern;
                    }

                    // If pl.nextGEQ is greater than the current docID, update the firstPL
                    if (pl.getCurrentPosting().getDocId() > currentDocID)
                    {
                        // Update the current Posting of the first Posting List (The shortest one)
                        firstPL.nextGEQ(pl.getCurrentPosting().getDocId());

                        // restartFormFirst means that the current document is not in the intersection
                        continue extern;
                    }

                    // else we are found the docID target
                    else
                    {
                        if (pl.getCurrentPosting().getDocId() < currentDocID)
                        {
                            break extern;
                        }

                        // If we are here, the posting list has the same docID of the firstPL
                        giveScore(documentToAdd, pl, currentDocID);
                    }
                }

                // we are here if the current posting list is empty
                else
                {
                    break extern;
                }
            }

            // If the document is not in the intersection, restart from the first posting list
//            if (restartFormFirst)
//                continue;

            // If the document survives, add it to the top-k list
            scoredDocuments.add(documentToAdd);
            firstPL.next();
        }

        // Return the top-k documents
        ArrayList<ScoredDocument> result = new ArrayList<>();
        for(int j = 0; j < TOP_K && !scoredDocuments.isEmpty(); j++)
        {
            result.add(scoredDocuments.poll());
        }

        for(PostingListSkippable pls : postingLists)
            pls.closeChannels();

        return result;
    }

    private void giveScore(ScoredDocument documentToAdd, PostingListSkippable pl, int currentDocID)
    {
        if (BM25)
        {
            documentToAdd.setScore(ScoreFunction.computeBM25(
                    pl.getCurrentPosting().getFrequency(),
                    terms.get(pl.getTerm()).getNumPosting(),
                    DOCUMENTINDEX.getLen(currentDocID)
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

//    public List<ScoredDocument> executeConjunctiveRankedRetrieval(List<String> queryTerms, Boolean bm25, int top_k)
//    {
//        BM25 = bm25;
//        TOP_K = top_k;
//        QueryPreprocessing qp = new QueryPreprocessing();
//
//        List<PostingListSkippable> postingLists = qp.retrievePostingList(queryTerms, true);
//
//        if(postingLists == null || postingLists.isEmpty())
//        {
//            System.out.println(RED + "Conjunctive Retrieval ERR > No posting lists found" + ANSI_RESET);
//            return new ArrayList<>();
//        }
//        terms = qp.getTerms();
//        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(TOP_K, ScoredDocument.compareTo());
//        orderPostingLists(postingLists);
//
//        // Get the first Posting of the first Posting List (The shortest one)
//        Posting currentPosting = postingLists.get(0).getCurrentPosting();
//
//        // While there is at least one Posting List with elements
//        int indexPL = 1;
//        while(currentPosting != null)
//        {
//            int current = currentPosting.getDocId();
//            //current = postingLists.get(0).getCurrentPosting().getDocId();
//            ScoredDocument documentToAdd = null;
//
//            // For each Posting List
//            while(indexPL < postingLists.size())
//            {
//                currentPosting = postingLists.get(0).getCurrentPosting();
//
//                PostingListSkippable pl = postingLists.get(indexPL);
//
//                // Check if the current Posting List is not empty
//                if (pl.getCurrentPosting() != null)
//                {
//                    // Take the next element greater or equal than the current one (NextGEQ)
//                    pl.nextGEQ(current);
//
//                    // If the current Posting List has elements and the current Posting is greater than the current one
//                    if (pl.getCurrentPosting().getDocId() > current)
//                    {
//                        // Update the current Posting of the first Posting List (The shortest one)
//                        postingLists.get(0).nextGEQ(pl.getCurrentPosting().getDocId());
//
//                        // if in the current posting list the docID greater than the current one,
//                        // we need to update the current posting
//                        if (postingLists.get(0).getCurrentPosting().getDocId() > pl.getCurrentPosting().getDocId())
//                        {
//                            currentPosting = postingLists.get(0).getCurrentPosting();
//                            current = postingLists.get(0).getCurrentPosting().getDocId();
//                            indexPL = 1;
//                        }
//
//                        // else we need to update the current posting of the current posting list
//                        else
//                        {
//                            currentPosting = pl.getCurrentPosting();
//                            current = currentPosting.getDocId();
//                            indexPL = 1;
//                        }
//                        break;
//                    }
//                    indexPL += 1;
//
//                }
//            }
//
//            // We arrived at the end of the posting lists, we can add the document to the top-k list
//            // using the score function
//            if(indexPL == postingLists.size())
//            {
//                documentToAdd = new ScoredDocument(current, 0);
//
//                // Compute the score of the document
//                for(PostingListSkippable pl: postingLists)
//                {
//                    if (BM25)
//                    {
//                        documentToAdd.setScore(ScoreFunction.computeBM25(
//                                pl.getCurrentPosting().getFrequency(),
//                                terms.get(pl.getTerm()).getNumPosting(),
//                                DOCUMENTINDEX.getLen(current)
//                        ));
//                    }
//                    else
//                    {
//                        documentToAdd.setScore(ScoreFunction.computeTFIDF(
//                                        pl.getCurrentPosting().getFrequency(),
//                                        terms.get(pl.getTerm()).getNumPosting()
//                                )
//                        );
//                    }
//                }
//
//                // Add the document to the top-k documents
//                scoredDocuments.add(documentToAdd);
//
//                // Update the current Posting of the first Posting List (The shortest one)
//                // to the next element
//                postingLists.get(0).next();
//                currentPosting = postingLists.get(0).getCurrentPosting();
//
//                // Reset the index of the Posting Lists
//                indexPL = 1;
//            }
//        }
//
//        // Return the top-k documents
//        ArrayList<ScoredDocument> result = new ArrayList<>();
//        for(int j = 0; j < TOP_K && !scoredDocuments.isEmpty(); j++)
//        {
//            result.add(scoredDocuments.poll());
//        }
//        return result;
//    }

    /**
     * Order the posting lists by the number of posting, in descending order
     * @param postingLists
     */
    private void orderPostingLists(List<PostingListSkippable> postingLists)
    {
        Comparator<PostingListSkippable> comparator = Comparator.comparingInt(postingList -> postingList.getTermInfo().getNumPosting());
        Collections.sort(postingLists, comparator);
    }
}