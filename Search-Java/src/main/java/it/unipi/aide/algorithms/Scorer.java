package it.unipi.aide.algorithms;

import it.unipi.aide.model.Posting;
import it.unipi.aide.model.PostingList;

import java.util.*;

public class Scorer {

    ArrayList<PostingList> queryPostingList;

    public Scorer(String scoreType, ArrayList<PostingList> queryPostingList)
    {
        this.queryPostingList = queryPostingList;

        if (scoreType.equals("TFIDF")) {
            int totDocuments = 8000000;
            PriorityQueue<TFIDFTermWeight> TFIDFTermWeights = calculateTFIDFTermWeights(totDocuments);

            // Print the resulted priorityQueue
            while(!TFIDFTermWeights.isEmpty())
            {
                TFIDFTermWeight currentTermWeight = TFIDFTermWeights.poll();
                System.out.println("< " + currentTermWeight.getTerm() + ", " + currentTermWeight.getDocId() + ", "
                    + currentTermWeight.getWeight() + " >");
            }
        }
        /*else
            computedScore = calculateBM25();*/
    }

    // score(d,q) = sum with t belonging to p and q of (1+log(tf(t,d))(log(N/df(t)) if tf(t,d)>0
    public PriorityQueue<TFIDFTermWeight> calculateTFIDFTermWeights(int totalDocuments)
    {
        PriorityQueue<TFIDFTermWeight> termWeights = createRankedPriorityQueue();
        for (PostingList elem : queryPostingList)
        {
            String term = elem.getTerm();
            List<Posting> termPosting = elem.getPostings();

            // Compute df(t)
            Integer documentFrequency = termPosting.size();

            for (Posting currentTermPosting: termPosting)
            {
                // Get tf(t,d)
                Integer docId = currentTermPosting.getDocId();
                Integer termFrequency = currentTermPosting.getFrequency();
                System.out.println("for term " + term + " and docID " + docId + " freq " + termFrequency);

                // Compute w(t,d)
                if(termFrequency>0)
                {
                    Double weight = (1+Math.log(termFrequency))*Math.log(totalDocuments/documentFrequency);
                    System.out.println((1+Math.log(termFrequency)));
                    System.out.println(Math.log(totalDocuments/documentFrequency));
                    termWeights.add(new TFIDFTermWeight(term, docId, weight));
                }
                else
                    termWeights.add(new TFIDFTermWeight(term, docId, 0.0));
            }
        }

        return termWeights;
    }

    private PriorityQueue<TFIDFTermWeight> createRankedPriorityQueue()
    {
        Comparator<TFIDFTermWeight> customComparator = (w1, w2) -> Double.compare(w2.getWeight(), w1.getWeight());

        return new PriorityQueue<>(customComparator);
    }

    private static class TFIDFTermWeight
    {
        String term;
        Integer docId;
        Double weight;

        private TFIDFTermWeight(String term, Integer docId, Double weight)
        {
            this.term = term;
            this.docId = docId;
            this.weight = weight;
        }

        private String getTerm() { return term; }

        private Integer getDocId() { return docId; }

        private Double getWeight() { return weight; }
    }
}
