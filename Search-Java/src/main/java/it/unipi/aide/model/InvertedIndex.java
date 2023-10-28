package it.unipi.aide.model;

import java.util.*;

public class InvertedIndex
{
    private final TreeMap<String, List<Posting>> index = new TreeMap<>();

    /**
     * Add a new Posting List in the index it doesn't exist one yet for that term,
     *  otherwise just update the existing one
     * @param doc DocID of the document
     * @param term Term contained in that document
     * @return true if a new Posting has been added, 0 otherwise
     */
    public boolean add(int doc, String term)
    {
        List<Posting> postingList = index.get(term);

        // No Posting List for that term, creating new
        if (postingList == null)
        {
            Posting newPosting = new Posting(doc);
            index.put(term, new ArrayList<>(Collections.singletonList(newPosting)));
            return true;
        }

        // Posting List exists, use that
        else
        {
            Posting lastPosting = postingList.get(postingList.size() - 1);

            // Last posting for that term is about current document
            if (lastPosting.getDocId() == doc)
            {
                lastPosting.increment();
                return false;
            }

            // Last posting is for another document, create a new posting
            else
            {
                Posting newPosting = new Posting(doc);
                postingList.add(newPosting);
                return true;
            }
        }
    }

    /**
     * Add a Posting List for given document
     * @param t Term inside the Inverted Index
     * @param pl Posting List to add
     */
    public void addPostingList(String t, PostingList pl)
    {
        index.put(t, pl.getPostings());
    }

    /**
     * Get a posting list by term
     * @param t Term from which get the PostingList
     * @return Its PostingList
     */
    public List<Posting> getPostingList(String t)
    {
        return index.get(t);
    }

    /**
     * Clear the inverted index
     */
    public void clear(){
        index.clear();
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("[");
        for (String key : index.keySet())
        {
            result.append("'").append(key).append("':");
            for (Posting e : index.get(key))
            {
                result.append(e.toString());
            }
            result.append("\n");
        }
        result.append("]");
        return result.toString();
    }
}

/*
 * Tale classe rappresenta l'Inverted Index
 *
 * Ogni volta che un termine viene aggiunto, vengono gestiti automaticamente i casi in cui il termine non ci fosse,
 *  fosse gia presente, oppure fosse gia presente e con lo stesso docid del documento corrente.
 *
 * Viene implementata anche una funzione clear() per liberare la memoria in fase di SPIMI in maniera sicura
 *
 */
