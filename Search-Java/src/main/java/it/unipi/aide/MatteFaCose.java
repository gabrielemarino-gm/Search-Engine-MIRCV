package it.unipi.aide;

import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.DocumentIndex;

public class MatteFaCose {

    public static void main(String[] argv) {

        DocumentIndex documentIndex = new DocumentIndex(true);
        for(int i = 8790000; i < CollectionInformation.getTotalDocuments(); i++){
            System.out.println(String.format("Document %d has length %d, but real lenght is %d", i, documentIndex.getLen(i), documentIndex.get(i).getTokenCount()));
        }

    }
}
