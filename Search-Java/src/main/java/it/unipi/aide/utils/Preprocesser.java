package it.unipi.aide.utils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import ca.rmen.porterstemmer.PorterStemmer;

/**
 * Class to perform preprocessing on a string
 */
public class Preprocesser
{
    private final String STOPWORD_FILE_PATH = "stopwords.txt";

    private final Pattern urlPattern;
    private final Pattern htmlPattern;
    private final Pattern nonDigitPattern;
    private final Pattern multipleSpacePattern;
    private final Pattern consecutiveLettersPattern;
    private final Pattern camelCasePattern;

    private final boolean stemmstopActive;
    private Set<String> stopwords;

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
        multipleSpacePattern = Pattern.compile(" +");
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
            InputStream inputStream = classLoader.getResourceAsStream(STOPWORD_FILE_PATH);

            if(inputStream == null)
            {
                System.err.println("Error while loading Stopwords file");
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
        text = nonDigitPattern.matcher(text).replaceAll(" ");
        text = multipleSpacePattern.matcher(text).replaceAll(" ");
        text = consecutiveLettersPattern.matcher(text).replaceAll(" ");
        text = camelCasePattern.matcher(text).replaceAll(" ");

        return text;
    }

    /**
     * Remove Stopwords from a list of tokens
     * @param tokens List of tokens
     * @return List of tokens without Stopwords
     */
    private List<String> removeStopwords(List<String> tokens)
    {
        /*
         *TODO: This may be enhanced by removing Stopwords from 'tokens' list without
         * creating a brand-new list
         */
        List<String> filteredTokens = new ArrayList<>();
        for (String token : tokens) {
            if (!stopwords.contains(token)) {
                filteredTokens.add(token);
            }
        }
        return filteredTokens;
    }

    /**
     * Perform Stemming on a list of tokens
     * @param words List of tokens
     * @return List of stemmed tokens
     */
    private List<String> performStemming(List<String> words)
    {
        /*
         *TODO: This may be enhanced by directly extract and put back
         * stemmed tokens in the same 'words' list
         */
        List<String> stemmedWords = new ArrayList<>();
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
        String[] termsArray = text.split(" ");
        List<String> terms = Arrays.asList(termsArray);

        // STEP 4: Remove Stopwords and Stemming
        if (stemmstopActive)
        {
            terms = removeStopwords(terms);
            terms = performStemming(terms);
        }

        // STEP 5: Remove empty tokens
        for (int i = terms.size() - 1; i >= 0; i--)
        {
            String element = terms.get(i);
            if (element.trim().isEmpty())
            {
                terms.remove(i);
            }
        }

        return terms;
    }
}
