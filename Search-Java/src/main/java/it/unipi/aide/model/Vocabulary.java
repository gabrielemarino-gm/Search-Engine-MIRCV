package it.unipi.aide.model;

import java.util.*;

public class Vocabulary
{
    private final TreeMap<String, TermInfo> vocab = new TreeMap<>();

    /**
     * Add a new term in the vocabulary it doesn't exist yet,
     * otherwise just update the existing one
     */
    public void add(String term, boolean addPosting)
    {
        if (!vocab.containsKey(term))
        {
            vocab.put(term, new TermInfo(term));
        }
        else
        {
            vocab.get(term).incrementTotalFrequency();
            if(addPosting){
                vocab.get(term).incrementNumPosting();
            }
        }
    }

    /**
     * Get the list of just the terms of the vocabulary
     * */
    public List<String> getTerms()
    {
        Set<String> keySet = vocab.keySet();

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
        return vocab.getOrDefault(term, new TermInfo(term));
    }

    public void set(TermInfo terminfo)
    {
        vocab.put(terminfo.getTerm(), terminfo);
    }

    /**
     * Clear the vocabulary
     */
    public void clear(){
        vocab.clear();
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        for (String term: vocab.keySet())
        {
            TermInfo ti = vocab.get(term);
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
