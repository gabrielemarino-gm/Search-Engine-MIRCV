package it.unipi.aide;

import it.unipi.aide.algorithms.ConjunctiveRetrieval;
import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;
import me.tongfei.progressbar.ProgressBar;

import java.io.*;
import java.util.List;

import static it.unipi.aide.utils.ColorText.ANSI_RESET;
import static it.unipi.aide.utils.ColorText.BLUE;

public class MakeDataset
{
    static final String datasetFile =  "data/dataset/resultsDataset.csv";
    static String queryFile = ConfigReader.getTrecEvalDataPath() + "/msmarco-test2020-queries.tsv";
    static Preprocesser preprocesser = new Preprocesser(true);
    static MaxScore maxScore = new MaxScore();
    static DAAT daat = new DAAT();
    static ConjunctiveRetrieval conjunctiveRetrieval = new ConjunctiveRetrieval();
    public static void main(String[] args) throws Exception
    {
        ProgressBar pb = new ProgressBar(BLUE + "Make Dataset >" + ANSI_RESET, 200);
        pb.start();

        FileManager.removeFile(datasetFile);
        FileManager.createFile(datasetFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile)))
        {
            String line;
            // Read the file line by line until the end
            while ((line = reader.readLine()) != null)
            {
                pb.step();
                String[] splitLine = line.split("\t");
                String queryId = splitLine[0];
                String query = splitLine[1];

                List<String> queryTerms = preprocesser.process(query);

                long startTime = System.currentTimeMillis();
                // DAAT
                List<ScoredDocument> daatResults = daat.executeDAAT(queryTerms, false, 10);

                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                printDataset(queryId, endTime-startTime, queryTerms.size(), "DAAT");

                // MAXSCORE
                startTime = System.currentTimeMillis();
                List<ScoredDocument> maxScoreResults = maxScore.executeMaxScore(queryTerms, false, 10);
                endTime = System.currentTimeMillis();
                elapsedTime = endTime - startTime;
                printDataset(queryId, endTime-startTime, queryTerms.size(), "MAXSCORE");
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
    private static void printDataset(String token, long elapsedTime, int nToken, String algorithm)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(datasetFile, true)))
        {
            // if the file is empty, write the header
            if (new File(datasetFile).length() == 0)
            {
                String header = "query_id,elapsed_time,n_token,algorithm";
                writer.write(header);
                writer.newLine();
            }

            String line = token + "," + elapsedTime + "," + nToken + "," + algorithm;
            writer.write(line);
            writer.newLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
