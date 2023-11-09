package it.unipi.aide;

import it.unipi.aide.model.Vocabulary;
import it.unipi.aide.utils.ConfigReader;

public class MatteFaCose {

    private static Vocabulary vocabulary;
    private static final String INPUT_PATH = "data/out/complete/";

        public static void main(String[] argv) {

            System.out.println(ConfigReader.getRawCollectionPath());
        }

}
