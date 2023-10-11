package it.unipi.aide;

import it.unipi.aide.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        boolean DEBUG = false;
        String MODE = "TFIDF";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                DEBUG = true;
            } else if (args[i].equals("--mode")) {
                MODE = args[i + 1];
            }
        }

        if (!DEBUG) {
            // Disabilita le ic.enable()
        }

        String CORPUS_PATH = "/data"; // Sostituisci con il percorso effettivo del corpus

        String testPath = "utils/prova.txt";
        System.out.println("ciao" + System.getProperty("user.dir"));
        String wholeText = readTextFile(testPath);
        System.out.println(wholeText);

        File file = new File("config/stopwords.txt");
        if (file.exists()) {
            System.out.println("ok");
        } else {
            System.out.println("Il file non esiste.");
        }

        Preprocesser pTest = new Preprocesser(true);
        List<String> processedText = pTest.process(wholeText);
        System.out.println(processedText);
    }

    public static String readTextFile(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
