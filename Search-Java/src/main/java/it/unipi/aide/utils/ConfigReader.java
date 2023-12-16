package it.unipi.aide.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConfigReader
{
    private static String workingDir;
    private static String debugDir;
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
    private static boolean stemmingEnabled;
    private static int compressionBlockSize;
    private static boolean blockDivisionEnabled;
    private static float k;
    private static float b;



    private static String DEFAULT_PATH = "config.json";
    static
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            JsonNode rootNode;
            Path tempFilePath = Paths.get(DEFAULT_PATH);
            // Create the config file if it doesn't exist
            if (!Files.exists(tempFilePath)) {
                Files.createFile(tempFilePath);

                try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_PATH);
                     OutputStream os = Files.newOutputStream(tempFilePath)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            }

            rootNode = objectMapper.readTree(tempFilePath.toFile());

            // Get the values
            workingDir = rootNode.get("workingDir").asText();
            debugDir = rootNode.get("debugDir").asText();

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
            stemmingEnabled = rootNode.get("stemmingEnabled").asBoolean();

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

    private static void setConfigValue(String propertyName, String newValue) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Path tempFilePath = Paths.get(DEFAULT_PATH);

            // Read the existing JSON content from the temporary file
            JsonNode rootNode = objectMapper.readTree(tempFilePath.toFile());

            // Update the property value
            if (rootNode.has(propertyName)) {
                ((ObjectNode) rootNode).put(propertyName, newValue);

                // Write the updated JSON content back to the temporary file
                Files.write(tempFilePath, objectMapper.writeValueAsBytes(rootNode),
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                System.out.println("Property '" + propertyName + "' not found in the configuration.");
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public static boolean isCompressionEnabled() { return compressionEnabled; }
    public static boolean isStemmingEnabled() {return stemmingEnabled; }

    public static int getCompressionBlockSize() { return compressionBlockSize; }

    public static boolean blockDivisionEnabled() { return blockDivisionEnabled; }

    public static float getK() { return k; }

    public static float getB() { return b; }


    public static void setCompression(boolean val)
    {
        compressionEnabled = val;
        setConfigValue("compressionEnabled", String.valueOf(val));
    }
    public static void setStemming(boolean val)
    {
        stemmingEnabled = val;
        setConfigValue("stemmingEnabled", String.valueOf(val));
    }
}
