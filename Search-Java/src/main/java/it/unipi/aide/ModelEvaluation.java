package it.unipi.aide;

import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;
import me.tongfei.progressbar.ProgressBar;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class ModelEvaluation
{
    static String ALGORITHM = "DAAT";
    static int TOP_K = 10;
    static boolean BM25 = false;
    static Preprocesser preprocesser = new Preprocesser(true);
    static MaxScore maxScore = new MaxScore();
    static DAAT daat = new DAAT();
    static final String queryFile = ConfigReader.getTrecEvalPath() + "/msmarco-test2020-queries.tsv";
    static final String resultsFile = ConfigReader.getTrecEvalPath() + "/resultsTrecEval.txt";
    static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args)
    {
        // Setup the system
        setupEvaluation();

        ProgressBar pb = new ProgressBar("Model Evaluation >", 200);
        pb.start();

        // Remove the file if already exists, then create it
        FileManager.removeFile(resultsFile);
        FileManager.createFile(resultsFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile)))
        {
            String line;

            // Read the file line by line until the end
            while ((line = reader.readLine()) != null)
            {
                // Split the line by tab
                String[] tokens = line.split("\t");
                // tokens[0] is the query id
                // tokens[1] is the query text

                //  execute the algorithm
                pb.step();
                List<String> queryTerms = preprocesser.process(tokens[1]);
                if (ALGORITHM.equals("DAAT"))
                {
                    List<ScoredDocument> resultsDAAT = daat.executeDAAT(queryTerms, BM25, TOP_K);
                    // write results to file
                    if (!resultsDAAT.isEmpty())
                        writeResults(tokens[0], resultsDAAT);
                }
                else if (ALGORITHM.equals("MAX-SCORE"))
                {
                    List<ScoredDocument> resultsMaxScore = maxScore.executeMaxScore(queryTerms, BM25, TOP_K);

                    // write results to file
                    if (!resultsMaxScore.isEmpty())
                        writeResults(tokens[0], resultsMaxScore);
                }
            }

            pb.stepTo(200);
            pb.stop();

            String trecEvalPath = "../../Trec-Eval/trec_eval-main";
            Process out = Runtime.getRuntime().exec(trecEvalPath + "/trec_eval -m all_trec " + trecEvalPath + "/2020qrels-pass.txt " + resultsFile);
            BufferedReader stdout = new BufferedReader(new InputStreamReader(out.getInputStream()));
            try
            {
                StringBuilder sb = new StringBuilder();
                String riga;

                while ((riga = stdout.readLine()) != null)
                    sb.append(riga).append("\n");

                System.out.println(sb);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void setupEvaluation() 
    {
        System.out.println("Model Evaluation > Setting up the system...");
        System.out.println("Model Evaluation > Choose the algorithm to use for the query, type 1 for DAAT, 2 for MaxScore");
        System.out.print("Model Evaluation > ");
        
        String input = scanner.nextLine();
        while(!(input.equals("1") || input.equals("2")))
        {
            System.err.println("Model Evaluation ERR > Invalid input. Try again.");
            System.out.println();
            System.out.print("Model Evaluation > ");
            input = scanner.nextLine();
        }
        
        if(input.equals("1"))
            ALGORITHM = "DAAT";
        else
            ALGORITHM = "MAX-SCORE";

        System.out.println("Model Evaluation > What kind of score function do you want to use? Type 1 for TF-IDF, 2 for BM25 ");
        System.out.print("Model Evaluation > ");

        input = scanner.nextLine();
        while(!(input.equals("1") || input.equals("2")))
        {
            System.err.println("Model Evaluation ERR > Invalid input. Try again.");
            System.out.println();
            System.out.print("Model Evaluation > ");
            input = scanner.nextLine();
        }

        BM25 = !input.equals("1");

        
        System.out.println("Model Evaluation > Choose the number of documents to retrieve for each query");
        System.out.print("Model Evaluation > ");
        
        input = scanner.nextLine();

        try
        {
            TOP_K = Integer.parseInt(input);
        }
        catch (NumberFormatException e)
        {
            System.err.println("MODEL EVALUATION ERR > Invalid input. Try again.");
        }
    }


    /**
     * Write the results of the algorithm to a file
     * where each line is in the format:
     *      query_id Q0 doc_id rank score STANDARD
     * where:
     *       query_id is the query id
     *       Q0 is a fixed string
     *       doc_id is the document id
     *       rank is the rank of the document in the top-k list
     *       score is the score of the document
     *       STANDARD is a fixed string to identify the current run
     * @param query_id Query id
     * @param results List of scored documents
     */
    private static void writeResults(String query_id, List<ScoredDocument> results)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile, true)))
        {
            int position = 1;
            for (ScoredDocument document : results)
            {
                // Create the line to write
                String line =   query_id +
                                " Q0 " +
                                document.getDocID() +
                                " " + position + " " +
                                document.getScore()
                                + " STANDARD";
                writer.write(line);
                writer.newLine();
                position++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
