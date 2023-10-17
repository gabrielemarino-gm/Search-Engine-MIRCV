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
            vocab.put(term, new TermInfo(term));
        }
        else
        {
            vocab.get(term).incrementTotalFrequency();
        }
    }

    public void updateNumPosting(String term, int np)
    {
        vocab.get(term).setNumPosting(np);
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
    public TermInfo get(String term) {
        return vocab.getOrDefault(term, new TermInfo(term, 1,0,1));
    }

    public void set(TermInfo terminfo){
        vocab.put(terminfo.getTerm(), terminfo);
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
