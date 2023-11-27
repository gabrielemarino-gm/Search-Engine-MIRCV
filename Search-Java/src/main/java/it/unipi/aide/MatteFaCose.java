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
        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");

        tokens = preprocesser.process("Folklore tales in the wood");
        startTime = System.currentTimeMillis();
        System.out.println("Folklore tales in the wood Results 2:");
        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");

        tokens = preprocesser.process("Folklore tales");
        startTime = System.currentTimeMillis();
        System.out.println("Folklore tales Results 3:");
        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");

        tokens = preprocesser.process("Folklore tales in the sea");
        startTime = System.currentTimeMillis();
        System.out.println("Folklore tales in the sea Results 4:");
        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");

        tokens = preprocesser.process("English language");
        startTime = System.currentTimeMillis();
        System.out.println("English language Results 5:");
        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");

        tokens = preprocesser.process("English words");
        startTime = System.currentTimeMillis();
        System.out.println("English words Results 6:");
        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");

        tokens = preprocesser.process("English words");
        startTime = System.currentTimeMillis();
        System.out.println("English words Results 7:");
        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");

        tokens = preprocesser.process("Manhattan Project");
        startTime = System.currentTimeMillis();
        System.out.println("Manhattan Project Results 8:");
        for(ScoredDocument sd : daat.executeDAAT(tokens)){
            System.out.print(sd);
        }
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Tempo impiegato: " + elapsedTime + " millisecondi");
    }
}
