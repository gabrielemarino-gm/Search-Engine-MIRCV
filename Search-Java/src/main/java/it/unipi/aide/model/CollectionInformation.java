package it.unipi.aide.model;

import it.unipi.aide.utils.ConfigReader;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * The following class contains all the information about the collection
 * used by the whole system to perform certain actions in an optimazed
 * way such as retrieving terms from the vocabulary faster and
 * calculating scores
 */
public class CollectionInformation
{
    private static final String PATH;
    private static long TOTAL_DOCUMENTS = 0;            // 0
    private static long TOTAL_TERMS = 0;                // 1
    private static long AVERAGE_DOCUMENT_LENGTH = 0;    // 2

    static {
        PATH = ConfigReader.getCollectionStatisticsPath();
    }

    /**
     * Those are used to set global values and also write them on a file directly
     */
    public static void setTotalDocuments(long totalDocuments){
        TOTAL_DOCUMENTS = totalDocuments;
        writeToFile(0);
    }
    public static void setTotalTerms(long totalTerms){
        TOTAL_TERMS = totalTerms;
        writeToFile(1);
    }
    public static void setAverageDocumentLength(long averageDocumentLength){
        AVERAGE_DOCUMENT_LENGTH = averageDocumentLength;
        writeToFile(2);
    }

    /**
     * Those are used to retrieve the value (from a file if it's not set yet)
     */
    public static long getTotalDocuments(){
        if(TOTAL_DOCUMENTS == 0)
            TOTAL_DOCUMENTS = readFromFile(0);
        return TOTAL_DOCUMENTS;
    }
    public static long getTotalTerms(){
        if(TOTAL_TERMS == 0)
            TOTAL_TERMS = readFromFile(1);
        return TOTAL_TERMS;
    }
    public static long getAverageDocumentLength(){
        if(AVERAGE_DOCUMENT_LENGTH == 0)
            AVERAGE_DOCUMENT_LENGTH = readFromFile(2);
        return AVERAGE_DOCUMENT_LENGTH;
    }

    /**
     * Write a statistic on a file
     * @param idx Statistic index to write
     */
    private static void writeToFile(int idx)
    {
        try (
                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(PATH),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)
                )
        {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 8L * idx, 8L);
            buffer.putLong((idx == 0) ? TOTAL_DOCUMENTS : (idx == 1) ? TOTAL_TERMS : AVERAGE_DOCUMENT_LENGTH);
        }
        catch (IOException e)
        {
            System.err.printf("Collection information file exception: %s%n", e.getMessage());
        }
     }

    /**
     * Read a statistic from a file
     * @param idx Statistic index to read
     */
    private static long readFromFile(int idx)
    {
        try (
                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(PATH),
                        StandardOpenOption.READ)
                )
        {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 8L * idx, 8L);
            return buffer.getLong();
        }
        catch (IOException e){
            System.err.printf("Collection information file exception: %s - %s%n", e.getClass(), e.getMessage());
            return 1;
        }
    }
}
