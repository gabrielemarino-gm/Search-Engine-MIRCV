package it.unipi.aide.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PostingList implements Iterator<Posting>
{
    private final String term;
    private List<Posting> postingList = new ArrayList<>();

    /* Useful in DAAT: it allows us to traverse the posting list with a pointer-like logic. */
    private int pointerIndex;

    /**
     * Create a new empty Posting List for given term
     * @param term Term to create the list for
     */
    public PostingList(String term)
    {
        this.term = term;
        this.pointerIndex = -1;
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
        this.pointerIndex = 0;
    }

    public void setPointerIndex(int newIndex) {
        pointerIndex = newIndex;
    }

    public int getPointerIndex() {
        return pointerIndex;
    }

    /* todo tocheck: is it safe to assume the posting we are adding have the greatest docid of the entire posting list? */
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

    public int getPointer() {
        return pointerIndex;
    }

    public void setPointer(int i) {
    }


    /* getCurrent is used to memorize the first element
    *  next is invoked only when we want to shift our list
    *   as we don't need current anymore
    *  hasNext is used to check if the list has remaining
    *   Postings or not
    */

    private Posting current = null;
    public Posting getCurrent() {
        if(current == null && hasNext())
            current = next();

        return current;
    }
    public boolean hasNext() {
        return !postingList.isEmpty();
    }

    public Posting next() {
        if(hasNext()) {
            current = postingList.remove(0);
        } else
        {
            current = null;
        }
        return current;
    }

}
