package it.unipi.aide;

import it.unipi.aide.algorithms.ConjunctiveRetrieval;
import it.unipi.aide.model.PostingListSkippable;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.QueryPreprocessing;
import org.apache.commons.compress.utils.Lists;

import java.util.Arrays;
import java.util.List;

public class MatteFaCose {
    public static void main(String[] args) {

        ConjunctiveRetrieval cr = new ConjunctiveRetrieval();
        QueryPreprocessing qp = new QueryPreprocessing();

        List<PostingListSkippable> pls = qp.retrievePostingList(Arrays.asList(new String[]{"lucki", "year"}), true);


        for (PostingListSkippable ps : pls)
        {
            while(ps.hasNext())
                System.out.println(ps.next());
        System.out.println("================");
            }

        System.out.println("================");


        for(ScoredDocument s : cr.executeConjunctiveRankedRetrieval(Arrays.asList(new String[]{"lucki", "year"}), true, 10))
            System.out.print(s);



    }
}
