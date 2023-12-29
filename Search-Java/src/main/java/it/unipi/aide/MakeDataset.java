package it.unipi.aide;

import it.unipi.aide.algorithms.ConjunctiveRetrieval;
import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.Cache;
import it.unipi.aide.model.DocumentIndex;
import it.unipi.aide.model.ScoredDocument;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.beautify.MemoryDisplay;
import me.tongfei.progressbar.ProgressBar;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Timer;

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
    public static void main(String[] args)
    {
        MemoryDisplay md = new MemoryDisplay();

        FileManager.removeFile(datasetFile);
        FileManager.createFile(datasetFile);

        long sleepTime = 1000L;
        ProgressBar pb = new ProgressBar(BLUE + "Make Dataset DAAT>" + ANSI_RESET, 200);
        pb.start();

        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile)))
        {
            String line;
            int index = 0;

            // Read the file line by line until the end run DAAT
            while ((line = reader.readLine()) != null)
            {
                pb.step();
                String[] splitLine = line.split("\t");
                String queryId = splitLine[0];
                String query = splitLine[1];
                List<String> queryTerms = preprocesser.process(query);
                runDAAT(queryId, queryTerms, index);

                // random interval between 3 and 10 seconds
                Thread.sleep(sleepTime);

                index++;
            }
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            pb.stop();
            md.end();
        }

        pb = new ProgressBar(BLUE + "Make Dataset MAXSCORE>" + ANSI_RESET, 200);
        pb.start();

        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile)))
        {
            String line;
            int index = 0;

            // Read the file line by line until the end run MAXSCORE
            while ((line = reader.readLine()) != null)
            {
                pb.step();
                String[] splitLine = line.split("\t");
                String queryId = splitLine[0];
                String query = splitLine[1];

                List<String> queryTerms = preprocesser.process(query);
                runMaxScore(queryId, queryTerms, index);

                Thread.sleep(sleepTime);

                index++;
            }
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            pb.stop();
            md.end();
        }

        pb = new ProgressBar(BLUE + "Make Dataset CONJUNCTIVE>" + ANSI_RESET, 200);
        pb.start();

        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile)))
        {
            String line;
            int index = 0;

            // Read the file line by line until the end run CONJUNCTIVE RETRIEVAL
            while ((line = reader.readLine()) != null)
            {
                pb.step();
                String[] splitLine = line.split("\t");
                String queryId = splitLine[0];
                String query = splitLine[1];

                List<String> queryTerms = preprocesser.process(query);
                runConjunctiveRetrieval(queryId, queryTerms, index);


                // int random = (int) (Math.random() * 10 + 3);
                Thread.sleep(sleepTime);

                index++;
            }
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            pb.stop();
            md.end();
        }
    }

    private static void runDAAT(String queryId, List<String> queryTerms, int index)
    {
        // DAAT
        long startTime = System.currentTimeMillis();
        List<ScoredDocument> results = daat.executeDAAT(queryTerms, bm25, topK);
        long endTime = System.currentTimeMillis();
        results.clear();
        printDataset(queryId, endTime-startTime, queryTerms.size(), index, "DAAT");
    }

    private static void runMaxScore(String queryId, List<String> queryTerms, int index)
    {
        // MAXSCORE
        index++;
        long startTime = System.currentTimeMillis();
        List<ScoredDocument> results = maxScore.executeMaxScore(queryTerms, bm25, topK);
        long endTime = System.currentTimeMillis();
        results.clear();
        printDataset(queryId, endTime-startTime, queryTerms.size(), index, "MAXSCORE");
    }

    private static void runConjunctiveRetrieval(String queryId, List<String> queryTerms, int index)
    {
        // CONJUNCTIVE RETRIEVAL
        index++;
        long startTime = System.currentTimeMillis();
        List<ScoredDocument> results = conjunctiveRetrieval.executeConjunctive(queryTerms, bm25, topK);
        long endTime = System.currentTimeMillis();
        results.clear();
        printDataset(queryId, endTime-startTime, queryTerms.size(), index, "CONJUNCTIVE RETRIEVAL");
    }

    /**
     * Print in a csv file, the query id, the time elapsed, tipe of algorithm
     * @param token Query id
     * @param elapsedTime Time elapsed to execute the algorithm
     * @param algorithm Algorithm used
     */
    private static void printDataset(String token, long elapsedTime, int nToken, int index, String algorithm)
    {
        try (FileChannel channel = FileChannel.open(Paths.get(datasetFile), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
             ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            if (new File(datasetFile).length() == 0)
            {
                baos.write("query_id,elapsed_time,n_token,time_index,algorithm\n".getBytes());
            }

            baos.write(String.format("%s,%d,%d,%d,%s\n",token,elapsedTime,nToken,index,algorithm).getBytes());
            channel.write(ByteBuffer.wrap(baos.toByteArray()));

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
