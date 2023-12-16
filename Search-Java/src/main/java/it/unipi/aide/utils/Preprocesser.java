package it.unipi.aide.utils;

import ca.rmen.porterstemmer.PorterStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static it.unipi.aide.utils.beautify.ColorText.*;

/**
 * Class to perform preprocessing on a string
 */
public class Preprocesser
{
    private final Pattern urlPattern;
    private final Pattern htmlPattern;
    private final Pattern nonDigitPattern;
    private final Pattern multipleSpacePattern;
    private final Pattern consecutiveLettersPattern;
    private final Pattern camelCasePattern;

    private final boolean stemmstopActive;
    private HashSet<String> stopwords;

    PorterStemmer stemmer = new PorterStemmer();

    /**
     * Preprocesser constructor
     * @param stemmstop enable Stopwords removal and Stemming
     */
    public Preprocesser(boolean stemmstop)
    {
        urlPattern = Pattern.compile("(https?://\\S+|www\\.\\S+)");
        htmlPattern = Pattern.compile("<[^>]+>");
        nonDigitPattern = Pattern.compile("[^a-zA-Z ]");
        multipleSpacePattern = Pattern.compile("\\s+");
        consecutiveLettersPattern = Pattern.compile("(.)\\1{2,}");
        camelCasePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])");

        // Initialize mode flags
        stemmstopActive = stemmstop;

        if (stemmstopActive)
        {
            stopwords = new HashSet<>();

           /*
           * ClassLoader and InputStream to load the file relatively to the package
           *
           * Note: utility non-class files goes in resources folder
           */
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = ConfigReader.getStopwordsPath();
            InputStream inputStream = classLoader.getResourceAsStream(ConfigReader.getStopwordsPath());

            if(inputStream == null)
            {
                System.out.println(RED + "Preprocesser ERR > Error while loading Stopwords file" + ANSI_RESET);
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    stopwords.add(line);
                }
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Basic text cleaning
     * @param text Text to clean
     * @return Cleaned text
     */
    private String clean(String text)
    {
        text = urlPattern.matcher(text).replaceAll(" ");
        text = htmlPattern.matcher(text).replaceAll(" ");
        text = camelCasePattern.matcher(text).replaceAll(" ");
        text = consecutiveLettersPattern.matcher(text).replaceAll(" ");
        text = nonDigitPattern.matcher(text).replaceAll(" ");

        // Useless if we do it later with split("\\s+")
//        text = multipleSpacePattern.matcher(text).replaceAll(" ");

        return text;
    }

    /**
     * Remove Stopwords from a list of tokens
     * @param tokens Set of tokens
     * @return List of tokens without Stopwords
     */
    private List<String> removeStopwords(ArrayList<String> tokens) {
        tokens.removeIf(stopwords::contains);
        return tokens;
    }

    /**
     * Perform Stemming on a list of tokens
     * @param words List of tokens
     * @return List of stemmed tokens
     */
    private ArrayList<String> performStemming(List<String> words)
    {
        /*
         *TODO: This may be enhanced by directly extract and put back
         * stemmed tokens in the same 'words' list
         */

        words.replaceAll(word -> stemmer.stemWord(word));

        ArrayList<String> stemmedWords = new ArrayList<>();
        for (String word : words)
        {
            stemmedWords.add(stemmer.stemWord(word));
        }
        return stemmedWords;
    }

    /**
     * Perform the preprocessing on a string
     * @param text Text to preprocess
     * @return List of preprocessed tokens
     */
    public List<String> process(String text)
    {
        // STEP 1: Text cleaning
        text = clean(text);

        // STEP 2: Lowercasing
        text = text.toLowerCase();

        // STEP 3: Tokenization
        String[] termsArray = text.split("\\s+");
        ArrayList<String> terms = new ArrayList<>(Arrays.asList(termsArray));

        // STEP 4: Remove empty tokens
        terms.removeIf(term -> term.trim().isEmpty());

        // STEP 5: Remove Stopwords and Stemming
        if (stemmstopActive)
        {
            terms = performStemming(
                    removeStopwords(terms)
            );
        }

        return terms;
    }
}
