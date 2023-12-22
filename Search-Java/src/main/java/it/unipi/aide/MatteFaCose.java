package it.unipi.aide;

import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.*;
import it.unipi.aide.utils.Preprocesser;
import java.util.List;

public class MatteFaCose {

    public static void main(String[] args) {

        Preprocesser preprocesser = new Preprocesser(true);
        DocumentIndex didx = new DocumentIndex(true);

        MaxScore ms = new MaxScore();
        Corpus corpus = new Corpus("data/trec-eval/msmarco-test2020-queries.tsv");
        long tot = 0;
        for(String s: corpus)
        {
            List<String> queryTerms = preprocesser.process(s.split("\t")[1]);

            long start = System.currentTimeMillis();
            List<ScoredDocument> scoredDocs =  ms.executeMaxScore(queryTerms, true, 10);
            for(ScoredDocument sd: scoredDocs) {
                System.out.println(didx.get(sd.getDocID()).getPid() + " " + sd.getScore());
            }
            tot += System.currentTimeMillis() - start;
        }

        System.out.println(tot / 200);
    }
}
