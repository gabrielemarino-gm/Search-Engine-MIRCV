package it.unipi.aide.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import ca.rmen.porterstemmer.PorterStemmer;

public class Preprocesser {
    private final Pattern urlPattern;
    private final Pattern htmlPattern;
    private final Pattern nonDigitPattern;
    private final Pattern multipleSpacePattern;
    private final Pattern consecutiveLettersPattern;
    private final Pattern camelCasePattern;

    private final boolean stemmstopActive;
    private Set<String> stopwords;

    PorterStemmer stemmer = new PorterStemmer();

    public Preprocesser(boolean stemmstop) {
        // Initialize regular expression patterns
        urlPattern = Pattern.compile("(https?://\\S+|www\\.\\S+)");
        htmlPattern = Pattern.compile("<[^>]+>");
        nonDigitPattern = Pattern.compile("[^a-zA-Z ]");
        multipleSpacePattern = Pattern.compile(" +");
        consecutiveLettersPattern = Pattern.compile("(.)\\1{2,}");
        camelCasePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])");

        // Initialize mode flags
        stemmstopActive = stemmstop;

        if (stemmstopActive) {

            // Load stopwords from a file
            String stopwordsFilePath = "path/to/stopwords.txt"; // Specify the actual file path
            stopwords = new HashSet<>();

            try (BufferedReader br = new BufferedReader(new FileReader(stopwordsFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    stopwords.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Generic text cleaning
    public String clean(String text) {
        text = urlPattern.matcher(text).replaceAll("");
        text = htmlPattern.matcher(text).replaceAll(" ");
        text = nonDigitPattern.matcher(text).replaceAll(" ");
        text = multipleSpacePattern.matcher(text).replaceAll(" ");
        text = consecutiveLettersPattern.matcher(text).replaceAll(" ");
        text = camelCasePattern.matcher(text).replaceAll(" ");

        return text;
    }

    // Removal of stopwords from a list of words
    public List<String> removeStopwords(List<String> tokens) {
        List<String> filteredTokens = new ArrayList<>();
        for (String token : tokens) {
            if (!stopwords.contains(token)) {
                filteredTokens.add(token);
            }
        }
        return filteredTokens;
    }

    // Stemming of a list of words
    public List<String> performStemming(List<String> words) {
        List<String> stemmedWords = new ArrayList<>();
        for (String word : words) {
            stemmedWords.add(stemmer.stemWord(word));
        }
        return stemmedWords;
    }

    public List<String> process(String text) {
        // Text cleaning
        text = clean(text);
        text = text.toLowerCase();

        // Text tokenization
        String[] termsArray = text.split(" ");
        List<String> terms = Arrays.asList(termsArray);

        // Stopwords removal
        if (stemmstopActive) {
            terms = removeStopwords(terms);
            terms = performStemming(terms);
        }

        // Return as a Tuple or another suitable data structure in Java
        // You can define a Tuple class or use an existing one
        return terms;
    }
}
