package it.unipi.aide;

import it.unipi.aide.model.ScoredDocument;

import java.util.Comparator;
import java.util.PriorityQueue;

public class MatteFaCose {
    public static void main(String[] args) {



        PriorityQueue<ScoredDocument> p1 = new PriorityQueue<>(5, (o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));
        p1.offer(new ScoredDocument(1, 0.5f));
        p1.offer(new ScoredDocument(2, 0.3f));
        p1.offer(new ScoredDocument(3, 0.7f));
        p1.offer(new ScoredDocument(4, 0.9f));
        p1.offer(new ScoredDocument(5, 0.2f));

        p1.peek();

        for (ScoredDocument s : p1)
            System.out.println(s.getDocID() + " " + s.getScore());


        System.out.println("--------------");


        PriorityQueue<ScoredDocument> p2 = new PriorityQueue<>(5, (o1, o2) -> Float.compare(o1.getScore(), o2.getScore()));
        p2.offer(new ScoredDocument(1, 0.5f));
        p2.offer(new ScoredDocument(2, 0.3f));
        p2.offer(new ScoredDocument(3, 0.7f));
        p2.offer(new ScoredDocument(4, 0.9f));
        p2.offer(new ScoredDocument(5, 0.2f));

        p2.peek();

        for (ScoredDocument s : p2)
            System.out.println(s.getDocID() + " " + s.getScore());

        for(int i = 0; i < 5; i++)
            System.out.println(p2.peek());
    }
}
