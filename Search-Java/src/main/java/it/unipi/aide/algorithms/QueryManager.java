package it.unipi.aide.algorithms;

import it.unipi.aide.model.TermInfo;
import it.unipi.aide.model.Vocabulary;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class QueryManager {

    private final String INPUT_PATH;
    private Vocabulary vocabulary;

    public QueryManager(String in_path){
        INPUT_PATH = in_path;
        loadVocabulary();
    }

    public void makeQuery(){
        System.out.println(vocabulary);
    }

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



