package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.ConfigReader;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MatteFaCose {


    public static void main(String[] argv) {

//        testing();

        DAAT daat = new DAAT(10);

        daat.testRetrievalCompression();

    }

    private static void testing() {
        try
                (
                        FileChannel fc = (FileChannel) Files.newByteChannel(Paths.get(ConfigReader.getVocabularyPath()),
                                StandardOpenOption.READ)
                        )
        {
            for(int i = 0; i < fc.size(); i += (int)TermInfo.SIZE_POST_MERGING)
            {
                MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY,
                        i, TermInfo.SIZE_POST_MERGING);

                byte[] termBytes = new byte[TermInfo.SIZE_TERM];
                buffer.get(termBytes);

                String term = new String(termBytes).trim();
                int totFreq = buffer.getInt();
                int nPost = buffer.getInt();
                long off = buffer.getLong();
                TermInfo termI = new TermInfo(term, totFreq, nPost, off);

                System.out.println(termI);
            }
        }
        catch (IOException io)
        {
            io.printStackTrace();
        }
    }
}
