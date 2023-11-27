package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.Preprocesser;

import java.util.List;

public class MatteFaCose {

    public static void main(String[] argv) {

        Preprocesser preprocesser = new Preprocesser(true);
        DAAT daat = new DAAT(10);

        List<String> tokens = preprocesser.process("Folklore tales in the wood");
        long startTime = System.currentTimeMillis();
        System.out.println("Folklore tales in the wood Results:");
        for (ScoredDocument sd : daat.executeDAAT(tokens)) {
            System.out.print(sd);
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");
    }
}
