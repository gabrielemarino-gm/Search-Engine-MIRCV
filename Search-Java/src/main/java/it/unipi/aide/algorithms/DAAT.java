//Warning: this class have a main to use during development. In the final version it will be removed.

package it.unipi.aide.algorithms;

import it.unipi.aide.model.Posting;
import it.unipi.aide.model.PostingList;
import it.unipi.aide.utils.Preprocesser;
import java.util.ArrayList;
import java.util.List;

public class DAAT {

    private static final ArrayList<PostingList> queryPostingList = new ArrayList<>();

    public static void main(String[] args)
    {
        String queryTest = "England folklore tales tales";
        Preprocesser p = new Preprocesser(true);
        List<String> processedQuery = p.process(queryTest);

        int k = 5;
        DAATRetrieval(processedQuery,k);
    }

    /* In the final version, this method will return ranked documents. */
    public static void DAATRetrieval(List<String> queryTerms, int k)
    {
        /* Fill the list with the pairs <Terms, PostingList> from Inverted Index related to the query terms. */
        createRelatedIndexElemsList(queryTerms);

        System.out.println(queryPostingList);

        /* Compute the collection scores. */
        String scoreType = "TFIDF";
        Scorer scoreComputer = new Scorer(scoreType,queryPostingList);
    }


    private static void createRelatedIndexElemsList(List<String> queryTerms)
    {
        QueryManager queryManager = new QueryManager("../testfiles/complete/");

        /* For every query term retrieve the posting list from inverted index. */
        for (String term : queryTerms) {
            /* Create the new term posting list. */
            List<Posting> termPosting = queryManager.getPostingsByTerm(term);
            PostingList termPostingList = new PostingList(term, termPosting);

            /* Add the new term posting list to the query posting list. */
            queryPostingList.add(termPostingList);
        }
    }
}
