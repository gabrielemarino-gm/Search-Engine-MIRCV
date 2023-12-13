package it.unipi.aide.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ConfigReader
{
    private static String workingDir;
    private static String debugDir;
    private static String rawCollectionPath;
    private static String compressedCollectionPath;
    private static String stopwordsPath;
    private static String collectionStatisticsPath;
    private static String documentIndexPath;
    private static String doclens;
    private static String blockDescriptorsPath;
    private static String vocabularyPath;
    private static String docidPath;
    private static String frequencyPath;
    private static String partialPath;
    private static String partialVocabularyPath;
    private static String partialDocsPath;
    private static String partialFrequenciesPath;
    private static String trecEvalDataPath;
    private static boolean compressionEnabled;
    private static int compressionBlockSize;
    private static boolean blockDivisionEnabled;
    private static float k;
    private static float b;

    static
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            // Read the json file
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            JsonNode rootNode = objectMapper.readTree(classLoader.getResourceAsStream("config.json"));

            // Get the values
            workingDir = rootNode.get("workingDir").asText();
            debugDir = rootNode.get("debugDir").asText();

            rawCollectionPath = rootNode.get("rawCollectionPath").asText();
            compressedCollectionPath = rootNode.get("compressedCollectionPath").asText();

            stopwordsPath = rootNode.get("stopwordsPath").asText();

            collectionStatisticsPath = rootNode.get("collectionStatisticsPath").asText();
            documentIndexPath = rootNode.get("documentIndexPath").asText();
            doclens = rootNode.get("doclens").asText();
            blockDescriptorsPath = rootNode.get("blockDescriptorsPath").asText();

            vocabularyPath = rootNode.get("vocabularyPath").asText();
            docidPath = rootNode.get("docidPath").asText();
            frequencyPath = rootNode.get("frequencyPath").asText();

            partialPath = rootNode.get("partialPath").asText();
            partialVocabularyPath = rootNode.get("partialVocabularyPath").asText();
            partialDocsPath = rootNode.get("partialDocsPath").asText();
            partialFrequenciesPath = rootNode.get("partialFrequenciesPath").asText();

            trecEvalDataPath = rootNode.get("trecEvalDataPath").asText();

            compressionEnabled = rootNode.get("compressionEnabled").asBoolean();

            compressionBlockSize = rootNode.get("compressionBlockSize").asInt();
            blockDivisionEnabled = rootNode.get("blockDivisionEnabled").asBoolean();

            k = (float) rootNode.get("k").asDouble();
            b = (float) rootNode.get("b").asDouble();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (NullPointerException fnf)
        {
            System.out.println("config.json Not Found");
        }
    }

    public static String getWorkingDir() { return workingDir; }
    public static String getDebugDir() { return debugDir; }
    public static String getStopwordsPath() { return stopwordsPath;}

    public static String getCollectionStatisticsPath() { return collectionStatisticsPath; }
    public static String getDocumentIndexPath() { return documentIndexPath; }
    public static String getDoclens() { return doclens; }
    public static String getBlockDescriptorsPath() { return blockDescriptorsPath; }
    public static String getVocabularyPath() { return vocabularyPath; }

    public static String getDocidPath() { return docidPath; }

    public static String getFrequencyPath() { return frequencyPath; }

    public static String getPartialPath() { return partialPath; }

    public static String getPartialVocabularyPath() { return partialVocabularyPath; }

    public static String getPartialDocsPath() { return partialDocsPath; }

    public static String getPartialFrequenciesPath() { return partialFrequenciesPath; }

    public static String getTrecEvalDataPath() { return trecEvalDataPath; }

    public static boolean compressionEnabled() { return compressionEnabled; }

    public static int getCompressionBlockSize() { return compressionBlockSize; }

    public static boolean blockDivisionEnabled() { return blockDivisionEnabled; }

    public static float getK() { return k; }

    public static float getB() { return b; }
}
