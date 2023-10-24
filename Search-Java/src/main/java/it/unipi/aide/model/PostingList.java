package it.unipi.aide.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PostingList
{
    private final String term;
    private List<Posting> postingList = new ArrayList<>();

    /**
     * Create a new empty Posting List for given term
     * @param term Term to create the list for
     */
    public PostingList(String term)
    {
        this.term = term;
    }

    /**
     * Create a Posting List for given term and given documents list
     * @param term Term to create the list for
     * @param documentsList list of Posting to initialize posting list of Term
     */
    public PostingList(String term,List<Posting> documentsList)
    {
        this.term = term;
        this.postingList = documentsList;
    }

    /**
     * Add a new Posting to the Posting List
     * @param p Posting to append
     */
    public void addPosting(Posting p)
    {
        postingList.add(p);
    }

    /**
     * Ad an entire posting list, and the sort it for DocID
     */
    public void addPostingList(PostingList pl)
    {
        postingList.addAll(pl.getPostings());

        Comparator<Posting> comparator = new Comparator<Posting>()
        {
            @Override
            public int compare(Posting p1, Posting p2){
                return Integer.compare(p1.getDocId(), p2.getDocId());
            }
        };
        postingList.sort(comparator);
    }

    public List<Posting> getPostings()
    {
        return postingList;
    }

    public String getTerm() { return term; }

    @Override
    public String toString() {
        return "PostingList{" +
                "term='" + term + '\'' +
                ", postingList=" + postingList +
                '}';
    }
}
