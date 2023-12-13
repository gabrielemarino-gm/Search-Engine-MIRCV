package it.unipi.aide;

import it.unipi.aide.algorithms.ConjunctiveRetrieval;
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

import static it.unipi.aide.utils.ColorText.*;

public class ModelEvaluation
{
    static String ALGORITHM = "DAAT";
    static boolean CONJUNCTIVE = false;
    static int TOP_K = 10;
    static boolean BM25 = false;
    static boolean conjunctiveMode = false;
    static Preprocesser preprocesser = new Preprocesser(true);
    static MaxScore maxScore = new MaxScore();
    static DAAT daat = new DAAT();
    static ConjunctiveRetrieval conjunctiveRetrieval = new ConjunctiveRetrieval();
    static String queryFile = ConfigReader.getTrecEvalDataPath() + "/msmarco-test2020-queries.tsv";
    static final String resultsFile = ConfigReader.getTrecEvalDataPath() + "/resultsTrecEval.txt";
    static final String datasetFile = ConfigReader.getTrecEvalDataPath() + "/resultsDataset.csv";
    static Scanner scanner = new Scanner(System.in);
    static String trecEvalPath = "../../Trec-Eval/trec_eval-main";
    static String year = "2020";

    public static void main(String[] args)
    {
        // Setup the system
        setupEvaluation();

        ProgressBar pb = new ProgressBar(BLUE + "Model Evaluation >" + ANSI_RESET, 200);
        pb.start();

        // Remove the file if already exists, then create it
        FileManager.removeFile(resultsFile);
        FileManager.createFile(resultsFile);
        FileManager.removeFile(datasetFile);
        FileManager.createFile(datasetFile);

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

                if (CONJUNCTIVE)
                {
                    long startTime = System.currentTimeMillis();
                    List<ScoredDocument> resultsCONJ = conjunctiveRetrieval.executeConjunctiveRankedRetrieval(queryTerms, BM25, TOP_K);
                    // write results to file
                    if (!resultsCONJ.isEmpty())
                        writeResults(tokens[0], resultsCONJ);

                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    printDataset(tokens[0], elapsedTime, "CONJUNCTIVE");
                }
                else
                {
                    if (ALGORITHM.equals("DAAT"))
                    {
                        long startTime = System.currentTimeMillis();
                        List<ScoredDocument> resultsDAAT = daat.executeDAAT(queryTerms, BM25, TOP_K);
                        // write results to file
                        if (!resultsDAAT.isEmpty())
                            writeResults(tokens[0], resultsDAAT);

                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;
                        printDataset(tokens[0], elapsedTime, "DAAT");
                    }
                    else if (ALGORITHM.equals("MAX-SCORE"))
                    {
                        long startTime = System.currentTimeMillis();
                        List<ScoredDocument> resultsMaxScore = maxScore.executeMaxScore(queryTerms, BM25, TOP_K);

                        // write results to file
                        if (!resultsMaxScore.isEmpty())
                            writeResults(tokens[0], resultsMaxScore);

                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;
                        printDataset(tokens[0], elapsedTime, "MAX-SCORE");
                    }
                }

            }

            pb.stepTo(200);
            pb.stop();

            // evaluatePerformance -in ../../Trec-Eval/trec_eval-main -y 2019
            if (args.length > 3 && args[1].equals("-in") && args[3].equals("-y"))
            {
                trecEvalPath = args[2];
                year = args[4];
            }
            else
            {
                System.out.println(RED + "MODEL EVALUATION ERR > Invalid input. Try again." + ANSI_RESET);
                System.exit(1);
            }

            // Setup path to input files
            String queryResFile = ConfigReader.getTrecEvalDataPath() + "/" + year + "qrels-pass.txt";
            Process out = Runtime.getRuntime().exec(trecEvalPath
                                                            + "/trec_eval -m all_trec "
                                                            + queryResFile + " "
                                                            + resultsFile);
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
        System.out.println(BLUE + "Model Evaluation > " + ANSI_RESET + "Setting up the system...");

        // Conjunction Mode or Disjunction Mode
        System.out.println(BLUE + "Model Evaluation > " + ANSI_RESET + "Choose the mode to use for the query, type 1 for Disjunction, 2 for Conjunctive");
        System.out.print(BLUE + "Model Evaluation > " + ANSI_RESET);

        String input = scanner.nextLine();
        while(!(input.equals("1") || input.equals("2")))
        {
            System.out.println(RED + "Model Evaluation ERR > Invalid input. Try again." + ANSI_RESET);
            System.out.println();
            System.out.print(BLUE + "Model Evaluation > " + ANSI_RESET);
            input = scanner.nextLine();
        }

        if (input.equals("2"))
            CONJUNCTIVE = true;
        else
            CONJUNCTIVE = false;

        if (!CONJUNCTIVE)
        {
            // Set up Algorithm
            System.out.println(BLUE + "Model Evaluation > " + ANSI_RESET + "Choose the algorithm to use for the query, type 1 for DAAT, 2 for MaxScore");
            System.out.print(BLUE + "Model Evaluation > " + ANSI_RESET);

            input = scanner.nextLine();
            while(!(input.equals("1") || input.equals("2")))
            {
                System.out.println(RED + "Model Evaluation ERR > Invalid input. Try again." + ANSI_RESET);
                System.out.println();
                System.out.print(BLUE + "Model Evaluation > " + ANSI_RESET);
                input = scanner.nextLine();
            }

            if(input.equals("1"))
                ALGORITHM = "DAAT";
            else
                ALGORITHM = "MAX-SCORE";
        }


        System.out.println(BLUE + "Model Evaluation > " + ANSI_RESET + "What kind of score function do you want to use? Type 1 for TF-IDF, 2 for BM25 ");
        System.out.print(BLUE + "Model Evaluation > " + ANSI_RESET);

        input = scanner.nextLine();
        while(!(input.equals("1") || input.equals("2")))
        {
            System.out.println(RED + "Model Evaluation ERR > Invalid input. Try again." + ANSI_RESET);
            System.out.println();
            System.out.print(BLUE + "Model Evaluation > " + ANSI_RESET);
            input = scanner.nextLine();
        }

        BM25 = !input.equals("1");

        System.out.println(BLUE + "Model Evaluation > " + ANSI_RESET + "Choose the number of documents to retrieve for each query");
        System.out.print(BLUE + "Model Evaluation > " + ANSI_RESET);
        
        input = scanner.nextLine();

        // Check if the input is a number
        while (!input.matches("[0-9]+"))
        {
            System.out.println(RED + "Model Evaluation ERR > Invalid input. Try again." + ANSI_RESET);
            System.out.println();
            System.out.print(BLUE + "Model Evaluation > " + ANSI_RESET);
            input = scanner.nextLine();
        }

        TOP_K = Integer.parseInt(input);
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

    /**
     * Print in a csv file, the query id, the time elapsed, tipe of algorithm
     * @param token Query id
     * @param elapsedTime Time elapsed to execute the algorithm
     * @param algorithm Algorithm used
     */
    private static void printDataset(String token, long elapsedTime, String algorithm)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(datasetFile, true)))
        {
            // if the file is empty, write the header
            if (new File(resultsFile).length() == 0)
            {
                String header = "query_id,elapsed_time,algorithm";
                writer.write(header);
                writer.newLine();
            }

            String line = token + "," + elapsedTime + "," + algorithm;
            writer.write(line);
            writer.newLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
