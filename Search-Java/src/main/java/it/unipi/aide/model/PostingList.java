package it.unipi.aide.model;

import java.util.List;

public class PostingList
{
    private final String term;
    private List<Posting>  postingList;

    public PostingList(String term)
    {
        this.term = term;
    }

    public void addPosting(Posting p)
    {
        postingList.add(p);
    }

    @Override
    public String toString() {
        return "PostingList{" +
                "term='" + term + '\'' +
                ", postingList=" + postingList +
                '}';
    }
}
