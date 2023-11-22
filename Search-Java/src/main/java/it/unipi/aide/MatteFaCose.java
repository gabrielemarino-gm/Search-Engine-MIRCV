package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;

import java.util.List;

public class MatteFaCose {


    public static void main(String[] argv) {


        Preprocesser preprocesser = new Preprocesser(true);
        DAAT daat = new DAAT(10);

        List<String> tokens = preprocesser.process("The brown fox jumps over the lazy dog");

        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }


    }
}
