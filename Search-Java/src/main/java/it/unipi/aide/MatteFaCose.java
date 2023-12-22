package it.unipi.aide;

import it.unipi.aide.model.Corpus;
import it.unipi.aide.utils.Preprocesser;
import me.tongfei.progressbar.ProgressBar;

import java.util.HashMap;
import java.util.List;

public class MatteFaCose {

    public static void main(String[] args) {

        ProgressBar pb = new ProgressBar("MatteFaCose", 0, 1000);

        new Thread(() -> {
            Corpus corpus = new Corpus("data/source/collection.tar.gz");
            for (String s : corpus) {
                pb.maxHint(pb.getMax() + 1);
            }
        }).start();

        Preprocesser preprocesser = new Preprocesser(true);
        Corpus corpus = new Corpus("data/source/collection.tar.gz");
        HashMap<Integer, Integer> map = new HashMap<>();
        int j = 0;
        pb.start();
        for(String s : corpus)
        {
            String[] a;
            a = s.split("\t");

            List<String> r = preprocesser.process(a[1]);
            for(String p: r)
                map.put(p.length(), map.getOrDefault(p.length(), 0) + 1);

            pb.stepBy(1);
        }
        pb.stop();

        for(Integer i : map.keySet()){
            System.out.println(i + " " + map.get(i));
        }
    }
}
