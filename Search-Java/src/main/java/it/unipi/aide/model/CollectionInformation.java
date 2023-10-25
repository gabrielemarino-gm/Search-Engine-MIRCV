package it.unipi.aide.model;

/**
 * The following class contains all the information about the collection
 * used by the whole system to perform certain actions in an optimazed
 * way such as retrieving terms from the vocabulary faster and
 * calculating scores
 */
public class CollectionInformation {

    private static String PATH;

    private static long TOTAL_DOCUMENTS = 0;            // 0
    private static long TOTAL_TERMS = 0;                // 1
    private static long AVARAGE_DOCUMENT_LENGHT = 0;    // 2

    CollectionInformation(String path){
        PATH = path;
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
    public static void setAvarageDocumentLenght(long avarageDocumentLenght){
        AVARAGE_DOCUMENT_LENGHT = avarageDocumentLenght;
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
    public static long getAvarageDocumentLenght(){
        if(AVARAGE_DOCUMENT_LENGHT == 0)
            AVARAGE_DOCUMENT_LENGHT = readFromFile(2);
        return AVARAGE_DOCUMENT_LENGHT;
    }


    /**
     * Write a statistic on a file
     * @param idx Statistic index to write
     */
    private static void writeToFile(int idx){

    }

    /**
     * Read a statistic from a file
     * @param idx Statistic index to read
     */
    private static long readFromFile(int idx){

        return 1;
    }
}
