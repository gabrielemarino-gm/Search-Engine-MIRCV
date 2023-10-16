package it.unipi.aide.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PostingList
{
    private final String term;
    private List<Posting>  postingList = new ArrayList<Posting>();

    public PostingList(String term)
    {
        this.term = term;
    }

    public void addPosting(Posting p)
    {
        postingList.add(p);
    }

    /**
     * Ad an entire posting list, and the sort it for DocID
     * */
    public void addPostingList(PostingList pl)
    {
        postingList.addAll(pl.getPostings());

        Comparator<Posting> comparator = new Comparator<Posting>()
        {
            @Override
            public int compare(Posting p1, Posting p2) {
                return Integer.compare(p1.getDocId(), p2.getDocId());
            }
        };

        postingList.sort(comparator);
    }

    List<Posting> getPostings()
    {
        return this.postingList;
    }

    @Override
    public String toString() {
        return "PostingList{" +
                "term='" + term + '\'' +
                ", postingList=" + postingList +
                '}';
    }


}
