package it.unipi.aide;

import it.unipi.aide.utils.ConfigReader;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MatteFaCose {

    // Esempio tipo SPIMI
    public MatteFaCose(String in) {
        // SPIMI legge dei file, elabora e scrive su altri file
        try
                (FileChannel channel1 = (FileChannel) Files.newByteChannel(Paths.get(in+"/file1.tsv"),
                        StandardOpenOption.READ);
                 FileChannel channel2 = (FileChannel) Files.newByteChannel(Paths.get(in+"/file2"),
                         StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                )
        {
            // Lettura su un file
            MappedByteBuffer buffer = channel1.map(FileChannel.MapMode.READ_ONLY, 0, channel1.size());
            byte[] b = new byte[(int)channel1.size()];
            buffer.get(b);

            // Elaborazione...
            System.out.println(ConfigReader.getDocumentIndexPath());

            // Scrittura su altro file
            buffer = channel2.map(FileChannel.MapMode.READ_WRITE, 0, channel1.size());
            buffer.put(b);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
