package it.unipi.aide;

import it.unipi.aide.algorithms.ConjunctiveRetrieval;
import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.Cache;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;
import me.tongfei.progressbar.ProgressBar;

import java.io.*;
import java.util.List;

import static it.unipi.aide.utils.beautify.ColorText.*;

public class MakeDataset
{
    static final String datasetFile =  "data/dataset/resultsDataset.csv";
    static String queryFile = ConfigReader.getTrecEvalDataPath() + "/msmarco-test2020-queries.tsv";
    static Preprocesser preprocesser = new Preprocesser(ConfigReader.isStemmingEnabled());
    static MaxScore maxScore = new MaxScore();
    static DAAT daat = new DAAT();
    static ConjunctiveRetrieval conjunctiveRetrieval = new ConjunctiveRetrieval();

    static int topK = 10;
    static boolean bm25 = true;
    public static void main(String[] args) throws Exception
    {
        ProgressBar pb = new ProgressBar(BLUE + "Make Dataset >" + ANSI_RESET, 200);
        pb.start();

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

                long startTime = System.currentTimeMillis();
//  ( DAAT
                daat.executeDAAT(queryTerms, bm25, topK);
                long endTime = System.currentTimeMillis();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "DAAT");

                // DAAT WITH CACHE, try to use DAAT with the same query terms in order to use the cache
                index++;
                startTime = System.currentTimeMillis();
                daat.executeDAAT(queryTerms, true, 10);
                endTime = System.currentTimeMillis();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "DAAT WITH CACHE");
//  )

                Cache.clearCache();

//  (MAXSCORE
                index++;
                startTime = System.currentTimeMillis();
                maxScore.executeMaxScore(queryTerms, bm25, topK);
                endTime = System.currentTimeMillis();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "MAXSCORE");

                // MAXSCORE WITH CACHE, try to use MAXSCORE with the same query terms in order to use the cache
                index++;
                startTime = System.currentTimeMillis();
                maxScore.executeMaxScore(queryTerms, bm25, topK);
                endTime = System.currentTimeMillis();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "MAXSCORE WITH CACHE");
//  )

                Cache.clearCache();

//  ( CONJUNCTIVE
                index++;
                startTime = System.currentTimeMillis();
                conjunctiveRetrieval.executeConjunctive(queryTerms, bm25, topK);
                endTime = System.currentTimeMillis();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "CONJUNCTIVE");

                // CONJUNCTIVE WITH CACHE, try to use CONJUNCTIVE with the same query terms in order to use the cache
                index++;
                startTime = System.currentTimeMillis();
                conjunctiveRetrieval.executeConjunctive(queryTerms, bm25, topK);
                endTime = System.currentTimeMillis();
                printDataset(queryId, endTime-startTime, queryTerms.size(), index, "CONJUNCTIVE WITH CACHE");
//  )

                Cache.clearCache();
                index++;
            }

            pb.stop();
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
