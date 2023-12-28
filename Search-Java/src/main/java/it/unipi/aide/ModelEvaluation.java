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
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import static it.unipi.aide.utils.beautify.ColorText.*;

public class ModelEvaluation
{
    static String ALGORITHM = "DAAT";
    static boolean CONJUNCTIVE = false;
    static int TOP_K = 10;
    static boolean BM25 = false;
    static Preprocesser preprocesser = new Preprocesser(ConfigReader.isStemmingEnabled());
    static MaxScore maxScore = new MaxScore();
    static DAAT daat = new DAAT();
    static ConjunctiveRetrieval conjunctiveRetrieval = new ConjunctiveRetrieval();
    static String queryFile = null;
    static String resultsFile = null;
    static String queryResFile = null;
    static String YEAR = null;
    static Scanner scanner /*= new Scanner(System.in)*/;

    public static void main(String[] args, Scanner s)
    {
        scanner = s;
        // evaluatePerformance -in ../../Trec-Eval/trec_eval-main -y 2020
        int maxArgs = args.length;
        if (maxArgs == 1)
        {
            System.out.println(RED + "Invalid usage: evaluatePerformance -in <queries_folder> [-y {2019, 2020}]"+ ANSI_RESET);
        }

        int i = 1;
        while(i < maxArgs)
        {
            switch (args[i])
            {
                case "-in":
                    queryFile = args[i+1];
                    resultsFile = args[i+1];
                    queryResFile = args[i+1];
                    i += 2;
                    break;
                case "-y":
                    YEAR = args[i+1];
                    i += 2;
                    break;
                default:
                    System.out.println(RED + "Invalid usage: evaluatePerformance -in <evaluation_files_folder> [-y {2019, 2020}]"+ ANSI_RESET);
                    return;
            }
        }

        if (queryFile == null)
        {
            System.out.println(RED + "MODEL EVALUATION WARN > Using default path "+ ANSI_RESET);
            queryFile = ConfigReader.getTrecEvalDataPath() + "/msmarco-test2020-queries.tsv";
            resultsFile = ConfigReader.getTrecEvalDataPath() + "/resultsTrecEval.txt";
            queryResFile = ConfigReader.getTrecEvalDataPath() + "/2020qrels-pass.txt";
        }
        else if (YEAR == null)
        {
            System.out.println(YELLOW + "MODEL EVALUATION WARN > Using default year <2020>"+ ANSI_RESET);
            YEAR = "2020";
        }
        else
        {
            if (!YEAR.equals("2019") && !YEAR.equals("2020")) {
                System.out.println(RED + "Invalid year: evaluatePerformance -in <queries_folder> [-y {2019, 2020}]" + ANSI_RESET);
                return;
            }
        }

        queryFile = queryFile + "/msmarco-test" + YEAR + "-queries.tsv";
        resultsFile = resultsFile + "/resultsTrecEval.txt";
        queryResFile = queryResFile + "/"+ YEAR + "qrels-pass.txt";

        // Setup the system
        setupEvaluation();

        ProgressBar pb = new ProgressBar(BLUE + "Model Evaluation >" + ANSI_RESET, 200);
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

                if (CONJUNCTIVE)
                {
                    List<ScoredDocument> resultsCONJ = conjunctiveRetrieval.executeConjunctive(queryTerms, BM25, TOP_K);
                    // write results to file
                    if (!resultsCONJ.isEmpty())
                        writeResults(tokens[0], resultsCONJ);
                }
                else
                {
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

            }

            pb.stepTo(200);
            pb.stop();

            // Setup path to input files

            // TODO: change this to the correct path

            String trecEvalPath = ConfigReader.getTrecEvalDataPath();
            Process out;
            try
            {
                out = Runtime.getRuntime().exec(trecEvalPath
                        + "/trec_eval -m all_trec "
                        + queryResFile + " "
                        + resultsFile);
            }
            catch (IOException e)
            {
                System.err.println("Unable to find trec_eval script. Leaving.");
                return;
            }

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
                System.err.println("Unable to write results");
            }
        }
        catch (FileNotFoundException fnf)
        {
            System.out.println(RED + "Model Evaluation ERR > Unable to find the file " + queryFile + ANSI_RESET);
            pb.stop();
        }
        catch (IOException e)
        {
            System.out.println(RED + "Model Evaluation ERR > Some error occurred while processing queries" + ANSI_RESET);
            pb.stop();
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

        CONJUNCTIVE = input.equals("2");

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
            System.err.println("Unable to write results");
        }
    }
}
