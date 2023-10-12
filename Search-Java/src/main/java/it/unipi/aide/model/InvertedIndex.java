package it.unipi.aide.model;

import java.util.*;

public class InvertedIndex {
    private final Map<String, List<Posting>> index = new HashMap<>();

    public void add(int doc, String term) {
        List<Posting> postingList = index.get(term);
        if (postingList == null) {
            Posting newPosting = new Posting(doc);
            index.put(term, new ArrayList<>(Collections.singletonList(newPosting)));
        } else {
            Posting lastPosting = postingList.get(postingList.size() - 1);
            if (lastPosting.getDocId() == doc) {
                lastPosting.increment();
            } else {
                Posting newPosting = new Posting(doc);
                postingList.add(newPosting);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        for (String key : index.keySet()) {
            result.append("'").append(key).append("':");
            for (Posting e : index.get(key)) {
                result.append(e.toString());
            }
            result.append(",");
        }
        result.append("]");
        return result.toString();
    }

    public void printIndex() {
        System.out.println("Inverted Index:");
        for (String term : index.keySet()) {
            System.out.print("'" + term + "': ");
            List<Posting> postingList = index.get(term);
            for (Posting posting : postingList) {
                System.out.print(posting.toString() + " ");
            }
            System.out.println();
        }
    }
}
