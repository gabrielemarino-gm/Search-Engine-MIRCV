package it.unipi.aide.model;

import java.util.*;

public class Vocabulary
{
    private final TreeMap<String, TermInfo> vocabulary = new TreeMap<>();

    /**
     * Add a new term in the vocabulary it doesn't exist yet,
     * otherwise just update the existing one
     */
    public void addNew(String term, boolean newPostingAdded)
    {
        // If the term is not in the vocabulary, add it
        if (!vocabulary.containsKey(term))
        {
            vocabulary.put(term, new TermInfo(term));
        }

        // Otherwise, update the existing one
        else
        {
            // If the term is in the vocabulary,
            // but it's the first time it's found in the document, add a posting
            if(newPostingAdded)
            {
                vocabulary.get(term).incrementNumPosting();
            }
            // In any case increments the total frequency
            vocabulary.get(term).incrementTotalFrequency();
        }
    }

    /**
     * Get the list of just the terms of the vocabulary
     * */
    public List<String> getTerms()
    {
        Set<String> keySet = vocabulary.keySet();

        // Converte l'insieme delle chiavi in una lista
        return new ArrayList<>(keySet);
    }

    /**
     * Returns term info from the dictionary
     * @param term Term to retrieve
     * @return TermInfo of given term
     */
    public TermInfo get(String term)
    {
        return vocabulary.getOrDefault(term, new TermInfo(term));
    }

    /**
     *
     * @param terminfo
     */
    public void set(TermInfo terminfo)
    {
        vocabulary.put(terminfo.getTerm(), terminfo);
    }

    /**
     * Clear the vocabulary
     */
    public void clear(){
        vocabulary.clear();
    }

    /**
     *
     * @param term
     * @return
     */
    public TermInfo getTermInfo(String term)
    {
        return vocabulary.get(term);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        for (String term: vocabulary.keySet())
        {
            TermInfo ti = vocabulary.get(term);
            result.append(ti.toString()).append('\n');
        }

        return result.toString();
    }
}

/*
 * Tale classe rappresenta il vocabolario di tutti i termini presenti nel Corpus o in partizioni di esso
 *  Eventuali occorrenze multiple di ogni termine nello stesso o in diversi documenti, e' gestita automaticamente
 *  consentendo quindi di tarare il calcolo del numero dei Posting e di TF per quel termine nella partizione
 *
 */
