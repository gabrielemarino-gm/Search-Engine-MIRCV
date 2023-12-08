package it.unipi.aide;

import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.DocumentIndex;

public class MatteFaCose {

    public static void main(String[] argv) {

        DocumentIndex documentIndex = new DocumentIndex();
        int max = 0;
        for(int i = 0; i < CollectionInformation.getTotalDocuments(); i++){
            max = Math.max(max, documentIndex.get(i).getTokenCount());
        }
        System.out.println(max);
    }
}
