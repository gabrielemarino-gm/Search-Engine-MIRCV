package it.unipi.aide.model;

import java.util.*;

public class InvertedIndex
{
    private final Map<String, List<Posting>> index = new HashMap<>();

    /**
     * Add a new Posting List in the index it not exits yet,
     * otherwise just update the existing one
     */
    public void add(int doc, String term)
    {
        List<Posting> postingList = index.get(term);

        // Check if the Posting List already exists
        if (postingList == null)
        {
            // If no, create a new one
            Posting newPosting = new Posting(doc, 1);
            index.put(term, new ArrayList<>(Collections.singletonList(newPosting)));
        }
        else
        {
            // Else, just update
            Posting lastPosting = postingList.get(postingList.size() - 1);
            if (lastPosting.getDocId() == doc)
            {
                lastPosting.increment();
            }
            else
            {
                Posting newPosting = new Posting(doc, 1);
                postingList.add(newPosting);
            }
        }
    }


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


    public void sort()
    {

    }
}
