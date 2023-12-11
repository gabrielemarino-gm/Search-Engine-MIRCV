package it.unipi.aide.model;

import it.unipi.aide.utils.Compressor;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DocumentIndex is a class that manages the DocumentIndex file
 * This is a fake class. It just writes down directly into the disk a document
 */
public class DocumentIndex
{
    private static long OFFSET = 0;
    private static long LENS_OFFSET = 0;
    private static final String DOCINDEX_PATH;
    private static final String DOCLENS_PATH;
    private static final List<Document> docList = new ArrayList<>();
    private static List<Integer> lengths;

    static
    {
        DOCINDEX_PATH = ConfigReader.getDocumentIndexPath();
        DOCLENS_PATH = ConfigReader.getDoclens();
    }

    public DocumentIndex()
    {
        if(!FileManager.checkFile(DOCINDEX_PATH)) FileManager.createFile(DOCINDEX_PATH);
        if(!FileManager.checkFile(DOCLENS_PATH)) FileManager.createFile(DOCLENS_PATH);
    }

    public DocumentIndex(boolean loadLengths){
        this();
        if(loadLengths) loadLengths();
    }

    /**
     * Append a document to the DocumentIndex file
     * @param document Document to append
     */
    public void add(Document document)
    {
        docList.add(document);
    }

    public void bulkWrite(){
        try(
                FileChannel index_channel = (FileChannel) Files.newByteChannel(Paths.get(DOCINDEX_PATH),
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE);

                FileChannel doclens_channel = (FileChannel) Files.newByteChannel(Paths.get(DOCLENS_PATH),
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE)
        )
        {
            for(Document document : docList)
            {
                MappedByteBuffer buffer = index_channel.map(FileChannel.MapMode.READ_WRITE, OFFSET, Document.SIZE);
                OFFSET += Document.SIZE;

                String padded = String.format("%-" + Document.PID_SIZE + "s", document.getPid()).substring(0, Document.PID_SIZE);

                buffer.put(padded.getBytes());
                buffer.putInt(document.getDocid());
                buffer.putInt(document.getTokenCount());

                byte[] doclen = Compressor.compressIntToVariableByte(document.getTokenCount());
                MappedByteBuffer doclens_buffer = doclens_channel.map(FileChannel.MapMode.READ_WRITE, LENS_OFFSET, doclen.length);

                doclens_buffer.put(doclen);
                LENS_OFFSET += doclen.length;
            }
            docList.clear();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void loadLengths()
    {
        try(
                FileChannel index_channel = (FileChannel) Files.newByteChannel(Paths.get(DOCLENS_PATH),
                        StandardOpenOption.READ)
        )
        {
            MappedByteBuffer buffer = index_channel.map(FileChannel.MapMode.READ_ONLY, 0, index_channel.size());

            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);

            int[] decomp = Compressor.VariableByteDecompression(byteArray);

            lengths = Arrays.stream(decomp).boxed().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int getLen(int docid){
        return lengths.get(docid);
    }

    /**
     * Retrieve a Document from the DocumentIndex
     * @param docid DocumentID to retieve
     * @return Needed Document along with all needed information
     */
    public Document get(int docid)
    {
        String tempPid;
        int tempDocid;
        int tempTokenCount;

        try(FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(DOCINDEX_PATH), StandardOpenOption.READ))
        {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, docid*Document.SIZE, Document.SIZE);

            byte[] termBytes = new byte[Document.PID_SIZE];
            buffer.get(termBytes);

            tempPid = new String(termBytes).trim();
            tempDocid = buffer.getInt();
            tempTokenCount = buffer.getInt();

            return new Document(tempPid, tempDocid, tempTokenCount);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
