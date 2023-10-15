package it.unipi.aide;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.OutputUtil;
import it.unipi.aide.algorithms.Merging;
import it.unipi.aide.algorithms.SPIMI;
import  it.unipi.aide.utils.Preprocesser;

import java.util.List;

public class CreateIndex
{

    private static String TEST_COLLECTION = "data/source/mini_collection.tsv";
    private static String OUTPUT_PATH = "data/partial/";

    public static void main(String[] args)
    {
        boolean DEBUG = false;
        String MODE = "TFIDF";
        /*
         * TODO -> Use args[] to retrieve input parameters
         *  To add input parameters change the Configuration from
         *  Run > Edit configurations...
         *  Then add an 'Application'
         *  Set a custom name, insert the desired Main Class
         *  and add parameters on 'Program Arguments' input bar
         *  ie. -d -mode TFIDF -ss
         *  When this piece of code will be done, the upper example
         *  will run this in debug mode, creating the index by TFIDF and
         *  StemmingStopwords removal enabled
         * */

        /*
         * TODO -> Functions to build index should go there
         *  Avoid using static paths, relative paths are better
         * */

        // Index building
        SPIMI spimi = new SPIMI(TEST_COLLECTION, OUTPUT_PATH, 60, true);
        int numBlocks = spimi.algorithm(true);
        System.out.println("DBG:    SPIMI writes " + numBlocks + " blocks");
        // Index merging
        Merging merge = new Merging();
        merge.mergeBloks(numBlocks);
        // merge.mergeBloks(3);

    }

    private static void tests()
    {
        Preprocesser p = new Preprocesser(true);

        List<String> l = p.process("Hi i am Matteo and I LiveIn Viterbo, because my family transferred here");
        for(String s : l){
            System.out.println(s);
        }
        System.exit(0);
    }

}