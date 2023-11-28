package it.unipi.aide;

import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.PostingListSkippable;
import it.unipi.aide.utils.QueryPreprocessing;

public class MatteFaCose {

    public static void main(String[] argv) {

        QueryPreprocessing qp = new QueryPreprocessing();

        PostingListSkippable pls = new PostingListSkippable(qp.binarySearch("fentanyl"));

        while(pls.hasNext()) {
            System.out.print(pls.next() + " ");
        }
    }
}
