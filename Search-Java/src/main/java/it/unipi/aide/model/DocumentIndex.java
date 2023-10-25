package it.unipi.aide.model;

import it.unipi.aide.utils.FileManager;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * This is a fake class. It just writes down directly into the disk a document
 */
public class DocumentIndex {
    private static long OFFSET = 0;
    private final String PATH;

    public DocumentIndex(String path){
        this.PATH = path+"documentIndex";
        if(!FileManager.checkFile(PATH)) FileManager.createFile(PATH);
    }

    /**
     * Append a document to the DocumentIndex file
     * @param document Document to append
     */
    public void add(Document document){
        try(
                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(PATH),
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE)
                )
        {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, OFFSET, Document.SIZE);
            OFFSET += Document.SIZE;

            StringBuilder pattern = new StringBuilder("%-").append(Document.PID_SIZE).append("s");
            String padded = String.format(pattern.toString(), document.getPid()).substring(0, Document.PID_SIZE);

            buffer.put(padded.getBytes());
            buffer.putInt(document.getDocid());
            buffer.putInt(document.getTokenCount());

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Retrieve a Document from the DocumentIndex
     * @param docid DocumentID to retieve
     * @return Needed Document along with all needed information
     */
    public Document get(int docid){
        String tempPid;
        int tempDocid;
        int tempTokenCount;

        try(
                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(PATH),
                        StandardOpenOption.READ)
        )
        {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, docid*Document.SIZE, Document.SIZE);

            byte[] termBytes = new byte[Document.PID_SIZE];
            buffer.get(termBytes);

            tempPid = new String(termBytes).trim();
            tempDocid = buffer.getInt();
            tempTokenCount = buffer.getInt();

            return new Document(tempPid, tempDocid, tempTokenCount);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
}

/*
 * La seguente e' una classe fantasma:
 *  non contiene nessun tipo di lista in quanto i Documenti sono scritti in append al file apposito
 *
 * In fase di inserimento infatti vengono inseriti in maniera incrementale uno dietro l'altro
 *
 * In fase di estrazione, sfruttando il fatto che ogni Documenti si trova all'offset docID*Document.SIZE
 *  sono estratti dal file direttamente a quella posizione
 */
