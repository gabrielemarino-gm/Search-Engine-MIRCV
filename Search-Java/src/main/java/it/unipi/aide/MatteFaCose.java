package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;

import java.util.List;

public class MatteFaCose {


    public static void main(String[] argv) {

//        testing();

        Preprocesser preprocesser = new Preprocesser(true);
        DAAT daat = new DAAT(10);

        List<String> tokens = preprocesser.process("Sleeping cat");

        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.println(sd);
        }


    }
}
