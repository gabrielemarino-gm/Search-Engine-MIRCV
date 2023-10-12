package it.unipi.aide;

public class CreateIndex {
    public static void main(String[] args) {
        boolean DEBUG = false;
        String MODE = "TFIDF";

        // Preprocessing test
        /*List<String> terms = runPreprocessing();
        System.out.println(Arrays.toString(terms.toArray()));*/

        // Index building
        buildIndex();
    }

    /*private static List<String> runPreprocessing() {

        List<String> terms = null;

        try
        {
            // File access
            String currentDirectory = System.getProperty("user.dir");

            String filePath = currentDirectory + "/src/main/java/it/unipi/aide/config/prova.txt";
            String text = new String(Files.readAllBytes(Paths.get(filePath)));

            // Preprocessing application
            Preprocesser p = new Preprocesser(true);
            terms = p.process(text);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return terms;
    }*/

    private static void buildIndex() {

        String currentDirectory = System.getProperty("user.dir");
        System.out.println(currentDirectory);
        String inputPath = currentDirectory + "/src/main/java/it/unipi/aide/mini_collection.tsv";
        String outputPath = currentDirectory + "/src/main/java/it/unipi/aide/data";

//        SPIMI s = new SPIMI(inputPath, outputPath, 75, true);
//        s.algorithm(true);
    }
}