package it.unipi.aide.algorithms;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import it.unipi.aide.model.Posting;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.model.Vocabulary;
import it.unipi.aide.utils.Commons;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class QueryManager {

    private final String INPUT_PATH;
    private Vocabulary vocabulary;

    public QueryManager(String in_path){
        INPUT_PATH = in_path;
        loadVocabulary();
    }

    public void makeQuery(){
        System.out.println(vocabulary);
        System.out.println("[bomb]" + getPostingsByTerm("bomb"));
        System.out.println("[manhattan]" + getPostingsByTerm("manhattan"));
        System.out.println("[project]" + getPostingsByTerm("project"));
        System.out.println("[rich]" + getPostingsByTerm("rich"));
        System.out.println("[war]" + getPostingsByTerm("war"));
    }

    /**
     * Get the posting list from bin blocks
     * @param term Term to retrieve posting list
     * @return Posting List of given term
     */
    private List<Posting> getPostingsByTerm(String term)
    {
        TermInfo toRetrieve = vocabulary.get(term);
        if (toRetrieve == null) return null;

        List<Posting> toRet =new ArrayList<>();

        String docsPath = INPUT_PATH + "docIDsBlock";
        String freqPath = INPUT_PATH + "frequenciesBlock";
        try
        {
            FileChannel docsChannel = (FileChannel) Files.newByteChannel(Paths.get(docsPath),
                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
            FileChannel freqChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

            long toRead = 4L * toRetrieve.getNumPosting();

            MappedByteBuffer docBuffer = docsChannel.map(FileChannel.MapMode.READ_WRITE, toRetrieve.getOffset(), toRead);
            MappedByteBuffer freqBuffer = freqChannel.map(FileChannel.MapMode.READ_WRITE, toRetrieve.getOffset(), toRead);
            byte[] docBytes = new byte[toRetrieve.getNumPosting() * 4];
            byte[] freqBytes = new byte[toRetrieve.getNumPosting() * 4];
            docBuffer.get(docBytes);
            freqBuffer.get(freqBytes);

            for (int i = 0; i < toRetrieve.getNumPosting(); i++) {
                byte[] tempDocBytes = new byte[4];
                byte[] tempFreqBytes = new byte[4];
                System.arraycopy(docBytes, i*4, tempDocBytes, 0 ,4);
                System.arraycopy(freqBytes, i*4, tempFreqBytes, 0 ,4);

                toRet.add(new Posting(Commons.bytesToInt(tempDocBytes), Commons.bytesToInt(tempFreqBytes)));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return toRet;
    }

    /**
     * Load the entire vocabulary from the disk
     */
    private void loadVocabulary() {
        vocabulary = new Vocabulary();

        String vocPath = INPUT_PATH + "vocabularyBlock";
        try {
            FileChannel vocChannel = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

            for(long offset = 0; offset < vocChannel.size(); offset+= TermInfo.SIZE){
                TermInfo nextTerm = getNextVoc(vocChannel, offset);
                vocabulary.set(nextTerm);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get next TermInfo from that channel
     * @param fileChannel FileChannel to retrieve the Term from
     * @param offsetVocabulary Offset at which the term is
     * @return Next TermInfo in line
     * @throws IOException
     */
    private TermInfo getNextVoc(FileChannel fileChannel, long offsetVocabulary) throws IOException{
        MappedByteBuffer tempBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offsetVocabulary, TermInfo.SIZE);

        byte[] termBytes = new byte[64];
        tempBuffer.get(termBytes);

        int frequency = tempBuffer.getInt();
        long offset = tempBuffer.getLong();
        int nPosting = tempBuffer.getInt();

        String term = new String(termBytes).trim();

        return new TermInfo(term, frequency, offset, nPosting);
    }
}



