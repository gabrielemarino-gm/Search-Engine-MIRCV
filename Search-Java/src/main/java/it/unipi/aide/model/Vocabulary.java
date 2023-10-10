package it.unipi.aide.model;

import java.util.*;

public class Vocabulary {
    private final Map<String, Integer> vocab = new HashMap<>();

    public void add(String term) {
        if (!vocab.containsKey(term)) {
            vocab.put(term, 1);
        } else {
            vocab.put(term, vocab.get(term) + 1);
        }
    }

    public int get(String term) {
        return vocab.getOrDefault(term, 0);
    }
}
