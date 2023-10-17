package it.unipi.aide.model;

import java.util.*;

public class InvertedIndex
{
    private final TreeMap<String, List<Posting>> index = new TreeMap<>();

    /**
     * Add a new Posting List in the index it not exits yet,
     * otherwise just update the existing one
     */
    public void add(int doc, String term)
    {
        List<Posting> postingList = index.get(term);

        if (postingList == null)
        {
            // No Posting List for that term, creating new
            Posting newPosting = new Posting(doc);
            index.put(term, new ArrayList<>(Collections.singletonList(newPosting)));
        }
        else
        {
            // Posting List exists, use that
            Posting lastPosting = postingList.get(postingList.size() - 1);
            // Last posting for that term is about current document
            if (lastPosting.getDocId() == doc)
            {
                lastPosting.increment();
            }
            else
            {
                // Last posting is for another document, create a new posting
                Posting newPosting = new Posting(doc);
                postingList.add(newPosting);
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
     * Debug method
     */
    public void printIndex()
    {
        System.out.println("Inverted Index:");
        for (String term : index.keySet())
        {
            System.out.print("'" + term + "': ");
            List<Posting> postingList = index.get(term);

            for (Posting posting : postingList)
            {
                System.out.print(posting.toString() + " ");
            }
            System.out.println();
        }
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
