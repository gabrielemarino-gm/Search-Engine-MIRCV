package it.unipi.aide.utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ConfigReader
{
    private static String rawCollectionPath;
    private static String compressedCollectionPath;
    private static String stopwordsPath;
    private static String documentIndexPath;
    private static String vocabularyPath;
    private static String invertedIndexFreqs;
    private static String invertedIndexDocs;
    private static String partialVocabularyDir;
    private static String frequencyFileName;
    private static String docidsFileName;
    private static String vocabularyFileName;
    private static String frequencyDir;
    private static String docidsDir;
    private static String collectionStatisticsPath;
    private static String blockDescriptorsPath;

    static
    {
        ObjectMapper objectMapper = new ObjectMapper();

        try
        {
            // Read the json file
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            JsonNode rootNode = objectMapper.readTree(classLoader.getResourceAsStream("config.json"));

            // Get the values
            rawCollectionPath = rootNode.get("rawCollectionPath").asText();
            compressedCollectionPath = rootNode.get("compressedCollectionPath").asText();
            stopwordsPath = rootNode.get("stopwordsPath").asText();
            documentIndexPath = rootNode.get("documentIndexPath").asText();
            vocabularyPath = rootNode.get("vocabularyPath").asText();
            invertedIndexFreqs = rootNode.get("invertedIndexFreqs").asText();
            invertedIndexDocs = rootNode.get("invertedIndexDocs").asText();
            partialVocabularyDir = rootNode.get("partialVocabularyDir").asText();
            frequencyFileName = rootNode.get("frequencyFileName").asText();
            docidsFileName = rootNode.get("docidsFileName").asText();
            vocabularyFileName = rootNode.get("vocabularyFileName").asText();
            frequencyDir = rootNode.get("frequencyDir").asText();
            docidsDir = rootNode.get("docidsDir").asText();
            collectionStatisticsPath = rootNode.get("collectionStatisticsPath").asText();
            blockDescriptorsPath = rootNode.get("blockDescriptorsPath").asText();

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

    public static String getRawCollectionPath() {
        return rawCollectionPath;
    }

    public static String getCompressedCollectionPath() {
        return compressedCollectionPath;
    }

    public static String getStopwordsPath() {
        return stopwordsPath;
    }

    public static String getDocumentIndexPath() {
        return documentIndexPath;
    }

    public static String getVocabularyPath() {
        return vocabularyPath;
    }

    public static String getInvertedIndexFreqs() {
        return invertedIndexFreqs;
    }

    public static String getInvertedIndexDocs() {
        return invertedIndexDocs;
    }

    public static String getPartialVocabularyDir() {
        return partialVocabularyDir;
    }

    public static String getFrequencyFileName() {
        return frequencyFileName;
    }

    public static String getDocidsFileName() {
        return docidsFileName;
    }

    public static String getVocabularyFileName() {
        return vocabularyFileName;
    }

    public static String getFrequencyDir() {
        return frequencyDir;
    }

    public static String getDocidsDir() {
        return docidsDir;
    }

    public static String getCollectionStatisticsPath() {
        return collectionStatisticsPath;
    }

    public static String getBlockDescriptorsPath() {
        return blockDescriptorsPath;
    }

}
