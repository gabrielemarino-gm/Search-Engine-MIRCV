package it.unipi.aide;

import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.*;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.QueryPreprocessing;
import me.tongfei.progressbar.ProgressBar;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MatteFaCose {

    public static void main(String[] args) {

        Preprocesser preprocesser = new Preprocesser(true);

        MaxScore ms = new MaxScore();

        Corpus corpus = new Corpus("data/trec-eval/msmarco-test2020-queries.tsv");
        long tot = 0;
        for(String s: corpus) {
            List<String> queryTerms = preprocesser.process(s.split("\t")[1]);

            long start = System.currentTimeMillis();
            List<ScoredDocument> scoredDocs =  ms.executeMaxScore(queryTerms, true, 10);
            for(ScoredDocument sd: scoredDocs) {
                System.out.println(sd.getDocID() + " " + sd.getScore());
            }
            tot += System.currentTimeMillis() - start;
        }

        System.out.println(tot / 200);
    }
}
