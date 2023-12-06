package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.Document;
import it.unipi.aide.model.DocumentIndex;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrecEval
{
    static Preprocesser preprocesser = new Preprocesser(true);
    static MaxScore maxScore = new MaxScore(false, 20);
    static final String queryFile = ConfigReader.getTrecEvalPath() + "/msmarco-test2020-queries.tsv";
    static final String resultsFile = ConfigReader.getTrecEvalPath() + "/resultsTrecEval.txt";

    public static void main(String[] args)
    {
        // Remove the file if already exists, then create it
        FileManager.removeFile(resultsFile);
        FileManager.createFile(resultsFile);

        // Load the documents length in memory
        //List<Integer> docLengths = new ArrayList<>();

        //DocumentIndex documentIndex = new DocumentIndex();
        //Document d = documentIndex.get(0);
        //docLengths.add(d.getTokenCount());

        //System.out.println("DBG:\t\tReading docLengths...");
        //for (int docId = 1; docId < CollectionInformation.getTotalDocuments(); docId++)
        //{
        //    d = documentIndex.get(docId);
        //    docLengths.add(d.getTokenCount());
        //}

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
                System.out.println("LOG:\t\t Execute Algorithm for: " + Collections.singletonList(tokens[1]));
                List<String> queryTerms = preprocesser.process(tokens[1]);
                List<ScoredDocument> resultsMaxScore = maxScore.executeMaxScore(queryTerms);

                // write results to file
                if (!resultsMaxScore.isEmpty())
                    writeResults(tokens[0], resultsMaxScore);

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
