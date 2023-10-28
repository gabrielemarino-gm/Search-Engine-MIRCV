//Warning: this class have a main to use during development. In the final version it will be removed.

package it.unipi.aide.algorithms;

import it.unipi.aide.model.Posting;
import it.unipi.aide.model.PostingList;
import it.unipi.aide.utils.Preprocesser;

import java.util.*;

public class DAAT {

    private static final ArrayList<PostingList> queryPostingList = new ArrayList<>();

    static int currentPostingListIndex;

    static int k;

    static String scoreType;

    static int numberOfDocInvolved;

    static QueryManager queryManager = new QueryManager("../testfiles/complete/");

    public DAAT(String scoreType) {
        this.currentPostingListIndex = -1;
        this.k = 5;
        this.scoreType = scoreType;
        this.numberOfDocInvolved = 0;
    }

    public static void main(String[] args)
    {

        /* Preprocess the query. */
        String queryTest = "Sleeping cat";
        Preprocesser p = new Preprocesser(true);
        List<String> preprocessedQuery = p.process(queryTest);

        /* Remove the duplicate terms. */
        Set<String> uniqueStrings = new LinkedHashSet<>(preprocessedQuery);
        List<String> processedQuery = new ArrayList<>(uniqueStrings);

        System.out.println(processedQuery);

        /* Starting document-at-a-time retrival algorithm. */
        DAATRetrieval(processedQuery);
    }

    public static void DAATRetrieval(List<String> queryTerms)
    {
        /* Fill the list with the pairs <Terms, PostingList> from Inverted Index related to the query terms. */
        fillQueryPostingList(queryTerms);

        /*Variable startTime to compute overall DAAT execution time.*/
        long startTime = System.currentTimeMillis();

        /* DEBUG: query Posting List obtained from the previous operation.  */
        System.out.println(queryPostingList);

        /* Creation of a PriorityQueue to build an ordered list of results. */
        PriorityQueue<ScoredDocument> results = new PriorityQueue<>();

        /* Compute the ranking based . */
        String type = "TFIDF"; //todo toremove
        if(type.equals("TFIDF"))
            results = computeScoresWithTFIDF();
        /*else
            results = computeScoresWithBM25();*/

        /* Print the top K results. */
        System.out.print("PriorityQueue elements: ");
        while (!results.isEmpty()) {
            ScoredDocument doc = results.poll();
            System.out.print(doc);
        }

        /* Compute total DAAT execution time. */
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - startTime;
        System.out.println("Time passed during DAAT computation: " + timePassed);
    }

    private static void fillQueryPostingList(List<String> queryTerms)
    {

        /* For every query term retrieve the posting list from inverted index. */
        for (String term : queryTerms) {
            /* Create the new term posting list. */
            List<Posting> termPosting = queryManager.getPostingsByTerm(term);
            PostingList termPostingList = new PostingList(term, termPosting);

            /* Add the new term posting list to the query posting list. */
            queryPostingList.add(termPostingList);
        }
    }

    private static PriorityQueue<ScoredDocument> computeScoresWithTFIDF()
    {
        /* Vector with boolean elements to sign as True if the i-th posting list fo the query posting list has been
         * totally visited and processed. */
        boolean[] isVisited = createVisitedFlags();

        //todo tofix: score size is NOT queryPostingList size
        /* Vector with double elements to progressively update during the computation of the documents score:*/
        double[] score = createScoreVector();

        /* Initialize docId with the id of the first document to visit, that is the minimum docId. */
        int docId = next(isVisited);

        //todo to properly change
        int totDocuments = 8000000;

        /* Create a PriorityQueue list of ScoredDocument to create an ordered list based on score. */
        Comparator<ScoredDocument> scoredDocumentComparator = new ScoredDocumentComparator();
        PriorityQueue<ScoredDocument> topKDocuments = new PriorityQueue<>(scoredDocumentComparator);

        while(docId!=-1)
        {
            /* Set score of the current document to 0.0 to start the computation. */
            double currentScore = 0.0;

            /* Variable that will containt wtd. */
            double termDocWeight;

            /* For every posting list retrieved for the query: */
            for(int i=0; i<queryPostingList.size(); i++)
            {
                /* Retrieve the current list of posting. */
                PostingList elem = queryPostingList.get(currentPostingListIndex);
                List<Posting> currentPosting = elem.getPostings();

                /* Retrieve the current single posting in pointer for the current posting list. */
                int currentPostingIndex = elem.getPointerIndex();
                Posting pointer = currentPosting.get(currentPostingIndex);

                /* Compute the number of postings in the current posting list. */
                int numPostings = currentPosting.size();

                /* Compute the index of the element pointed in the current posting list. */
                int currentIndexPointer = queryPostingList.indexOf(elem);

                /* If the current posting list has already been visited, continue to the next iteration. */
                if(isVisited[currentIndexPointer])
                    continue;

                //todo tocheck these two if
                /* If the current posting list has not totally been visited. */
                if(currentPostingIndex<numPostings)
                {
                    if(pointer.getDocId()==docId)
                    {
                        /* Compute wtd and update the document score. */
                        termDocWeight =(1+Math.log(pointer.getFrequency()))*Math.log(totDocuments/numPostings);
                        score[currentIndexPointer] += termDocWeight;
                        currentScore = score[currentIndexPointer];

                        /* If this is the last posting in the current posting list, set the posting list as visited.
                        * Otherwise, move the pointer forward. */
                        if(currentPostingIndex+1==numPostings)
                            isVisited[currentIndexPointer] = true;
                        else
                            elem.setPointerIndex(currentPostingIndex+1);

                        break;
                    }
                }
                else
                    isVisited[currentIndexPointer] = true;

                /* If the current posting list has not entirely been visited, move the pointer forward. */
                if (!isVisited[currentIndexPointer])
                    elem.setPointerIndex(i+1);
            }

            /* Add or update the pair <docId, score> in the PriorityQueue of ScoredDocument. */
            addOrUpdateScoredDocument(topKDocuments, docId, currentScore);

            /* Update the docId with the next minimum docId left. */
            docId = next(isVisited);
        }
        return topKDocuments;
    }

    private static void addOrUpdateScoredDocument(PriorityQueue<ScoredDocument> topKDocuments, int docId,
                                                  double newScore)
    {
        for(ScoredDocument scoredDocument: topKDocuments) {
            if(scoredDocument.getDocID()==docId) {
                scoredDocument.setScore(newScore);
                return;
            }
        }

        ScoredDocument scoredDocument = new ScoredDocument(docId, newScore);
        topKDocuments.add(scoredDocument);
    }

    private static double[] createScoreVector() {

        //number of doc
        double[] visitedFlagsVector = new double[queryPostingList.size()];

        for(int i=0; i<queryPostingList.size(); i++)
            visitedFlagsVector[i] = 0.0;

        return visitedFlagsVector;
    }

    private static boolean[] createVisitedFlags() {

        boolean[] visitedFlagsVector = new boolean[queryPostingList.size()];

        for(int i=0; i<queryPostingList.size(); i++)
            visitedFlagsVector[i] = false;

        return visitedFlagsVector;
    }

    private static int next(boolean[] isVisited) {

        int nextDocId = -1;
        for (int i=0; i<queryPostingList.size(); i++) {

            PostingList currentPostingList = queryPostingList.get(i);

            if (currentPostingList != null && currentPostingList.getPostings() != null) {

                int currentPostingIndex = currentPostingList.getPointer();
                int currentDocId = currentPostingList.getPostings().get(currentPostingIndex).getDocId();

                if (nextDocId == -1 || currentDocId < nextDocId) {

                    if(!isVisited[i]) {
                        nextDocId = currentDocId;
                        currentPostingListIndex = i;
                    }
                }
            }
        }

        return nextDocId;
    }

    /* Class to implement comparing mechanism to rank the documents. */
    private static class ScoredDocumentComparator implements Comparator<ScoredDocument> {
        @Override
        public int compare(ScoredDocument doc1, ScoredDocument doc2) {
            return Double.compare(doc2.getScore(), doc1.getScore());
        }
    }
}
