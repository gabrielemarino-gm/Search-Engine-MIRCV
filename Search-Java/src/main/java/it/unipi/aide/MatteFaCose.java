package it.unipi.aide;

import it.unipi.aide.algorithms.ConjunctiveRetrieval;
import it.unipi.aide.model.PostingListSkippable;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.QueryPreprocessing;
import org.apache.commons.compress.utils.Lists;

import java.util.Arrays;
import java.util.List;

public class MatteFaCose {
    public static void main(String[] args) {

        Preprocesser p = new Preprocesser(true);
        ConjunctiveRetrieval cr = new ConjunctiveRetrieval();
        QueryPreprocessing qp = new QueryPreprocessing();

        for(ScoredDocument s : cr.executeConjunctive(p.process("United States"), true, 10))
            System.out.print(s);
        System.out.println("--------------------------------------------------");
        for(ScoredDocument s : cr.executeConjunctive(p.process("Lucky year"), true, 10))
            System.out.print(s);
        System.out.println("--------------------------------------------------");
        for(ScoredDocument s : cr.executeConjunctive(p.process("United States"), true, 10))
            System.out.print(s);
        System.out.println("--------------------------------------------------");
        for(ScoredDocument s : cr.executeConjunctive(p.process("Lucky year"), true, 10))
            System.out.print(s);

    }
}
