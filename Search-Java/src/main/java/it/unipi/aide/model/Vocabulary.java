package it.unipi.aide.model;

import java.util.*;

public class Vocabulary
{
    private final TreeMap<String, TermInfo> vocab = new TreeMap<>();

    /**
     * Add a new term in the vocabulary it not exits yet,
     * otherwise just update the existing one
     */
    public void add(String term)
    {
        if (!vocab.containsKey(term))
        {
            vocab.put(term, new TermInfo(1, 0, 1));
        }
        else
        {
            vocab.get(term).incrementTotalFrequency();
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

    public TermInfo get(String term) {
        return vocab.getOrDefault(term, new TermInfo(1,0,1));
    }

    @Override
    public String toString()
    {
        String ret = null;
        for (String term: vocab.keySet())
        {
            TermInfo ti = vocab.get(term);
            ret = ret + term + ": " + ti.toString() + "\n";
        }

        return ret;
    }
}
