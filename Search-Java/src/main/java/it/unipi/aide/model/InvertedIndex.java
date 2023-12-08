package it.unipi.aide.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

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
            // get the last posting
            Posting lastPosting = postingList.get(postingList.size() - 1);

            // if the last posting for that term is about current document
            if (lastPosting.getDocId() == doc)
            {
                lastPosting.incrementFrequency();
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
     * Get a posting list by term
     * @param t Term from which get the PostingList
     * @return Its PostingList
     */
    public List<Posting> getPostingList(String t)
    {
        return index.get(t);
    }

    /**
     * Get the last posting for given term
     * @param t Term to get the posting
     * @return
     */
    public Posting getLastPosting(String t)
    {
        List<Posting> pl = getPostingList(t);
        return pl.get(pl.size() - 1);
    }

    /**
     * Clear the inverted index
     */
    public void clear()
    {
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
