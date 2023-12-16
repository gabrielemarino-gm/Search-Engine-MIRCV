package it.unipi.aide;

import it.unipi.aide.algorithms.ConjunctiveRetrieval;
import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.Cache;
import it.unipi.aide.model.Corpus;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.beautify.MemoryDisplay;
import me.tongfei.progressbar.ProgressBar;

import java.io.*;
import java.util.List;

import static it.unipi.aide.utils.beautify.ColorText.*;

public class MakeDataset
{
    static final String datasetFile =  "data/dataset/resultsDataset.csv";
//    static String queryFile = ConfigReader.getTrecEvalDataPath() + "/msmarco-test2020-queries.tsv";
    static String queryFile = "data/queries/queries.train.tsv";
    static Preprocesser preprocesser = new Preprocesser(ConfigReader.isStemmingEnabled());
    static MaxScore maxScore = new MaxScore();
    static DAAT daat = new DAAT();
    static ConjunctiveRetrieval conjunctiveRetrieval = new ConjunctiveRetrieval();

    static int topK = 10;
    static boolean bm25 = true;
    public static void main(String[] args)
    {
        ProgressBar pb = new ProgressBar(BLUE + "Make Dataset >" + ANSI_RESET, 0);
        pb.start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(queryFile)))
            {

                while (reader.readLine() != null)
                {
                pb.maxHint(pb.getMax() + 1);
                }
            }
            catch (IOException io)
            {
                System.err.println("Error while counting lines");
            }
        }).start();

        FileManager.removeFile(datasetFile);
        FileManager.createFile(datasetFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile)))
        {
            String line;
            int index = 0;
            // Read the file line by line until the end
            while ((line = reader.readLine()) != null)
            {
                pb.step();
                String[] splitLine = line.split("\t");
                String queryId = splitLine[0];
                String query = splitLine[1];

                List<String> queryTerms = preprocesser.process(query);
                List<ScoredDocument> results;
                long startTime = System.currentTimeMillis();
//  ( DAAT
                results = daat.executeDAAT(queryTerms, bm25, topK);
                long endTime = System.currentTimeMillis();
                results.clear();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "DAAT");

                // DAAT WITH CACHE, try to use DAAT with the same query terms in order to use the cache
                index++;
                startTime = System.currentTimeMillis();
                results = daat.executeDAAT(queryTerms, bm25, 10);
                endTime = System.currentTimeMillis();
                results.clear();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "DAAT WITH CACHE");
//  )

                Cache.clearCache();

//  (MAXSCORE
                index++;
                startTime = System.currentTimeMillis();
                results = maxScore.executeMaxScore(queryTerms, bm25, topK);
                endTime = System.currentTimeMillis();
                results.clear();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "MAXSCORE");

                // MAXSCORE WITH CACHE, try to use MAXSCORE with the same query terms in order to use the cache
                index++;
                startTime = System.currentTimeMillis();
                results = maxScore.executeMaxScore(queryTerms, bm25, topK);
                endTime = System.currentTimeMillis();
                results.clear();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "MAXSCORE WITH CACHE");
//  )

                Cache.clearCache();

//  ( CONJUNCTIVE
                index++;
                startTime = System.currentTimeMillis();
                results = conjunctiveRetrieval.executeConjunctive(queryTerms, bm25, topK);
                endTime = System.currentTimeMillis();
                results.clear();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "CONJUNCTIVE");

                // CONJUNCTIVE WITH CACHE, try to use CONJUNCTIVE with the same query terms in order to use the cache
                index++;
                startTime = System.currentTimeMillis();
                results = conjunctiveRetrieval.executeConjunctive(queryTerms, bm25, topK);
                endTime = System.currentTimeMillis();
                results.clear();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "CONJUNCTIVE WITH CACHE");
//  )

                Cache.clearCache();
                index++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            pb.stop();
        }
    }



    /**
     * Print in a csv file, the query id, the time elapsed, tipe of algorithm
     * @param token Query id
     * @param elapsedTime Time elapsed to execute the algorithm
     * @param algorithm Algorithm used
     */
    private static void printDataset(String token, long elapsedTime, int nToken, int index, String algorithm)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(datasetFile, true)))
        {
            // if the file is empty, write the header
            if (new File(datasetFile).length() == 0)
            {
                String header = "query_id,elapsed_time,n_token,time_index,algorithm";
                writer.write(header);
                writer.newLine();
            }

            String line = token + "," + elapsedTime + "," + nToken + "," + index + "," + algorithm;
            writer.write(line);
            writer.newLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
