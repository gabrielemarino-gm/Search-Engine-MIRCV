package it.unipi.aide;

public class CreateIndex {
    public static void main(String[] args) {
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
        *  Avoid using static patha, relative paths are better
        * */
        // Index building
        buildIndex();
    }

    private static void buildIndex() {

        String currentDirectory = System.getProperty("user.dir");
        System.out.println(currentDirectory);
        String inputPath = currentDirectory + "/src/main/java/it/unipi/aide/mini_collection.tsv";
        String outputPath = currentDirectory + "/src/main/java/it/unipi/aide/data";

//        SPIMI s = new SPIMI(inputPath, outputPath, 75, true);
//        s.algorithm(true);
    }
}