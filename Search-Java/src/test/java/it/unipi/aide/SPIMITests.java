package it.unipi.aide;

import it.unipi.aide.utils.ConfigReader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.junit.runner.RunWith;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.MappedByteBuffer;
import java.nio.file.StandardOpenOption;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigReader.class})
public class SPIMITests {

    File f1;
    String testPhrase = "10\tthe cat is on the table\n11\tthe priest is in the church";

    // JUnit Temporary folder removed after tests
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp(){
        try
        {
            // Create a file (Test Corpus)
            f1 = folder.newFile("file1.tsv");
            FileChannel fc1 = (FileChannel) Files.newByteChannel(Paths.get(f1.getAbsolutePath()),
                    StandardOpenOption.READ, StandardOpenOption.WRITE);
            MappedByteBuffer buffer = fc1.map(FileChannel.MapMode.READ_WRITE, 0, testPhrase.length());

            buffer.put(testPhrase.getBytes());
        }
        catch (IOException e)
        {
            return;
        }
    }

    @Test
    public void testConstructor() throws IOException {
        PowerMockito.mockStatic(ConfigReader.class);
        String expectedPath = "you_have_been_fooled";

        when(ConfigReader.getDocumentIndexPath()).thenReturn(expectedPath);

        // Esecuzione del simil-SPIMI con i file test fittizi
//        MatteFaCose mfc = new MatteFaCose(folder.getRoot().getAbsolutePath());

        // Test che il file di output sia stato creato
//        assertTrue(Files.exists(Paths.get(folder.getRoot().getAbsolutePath()+"/file2")));

        try {
            File wrote = folder.getRoot().toPath().resolve("file2").toFile();

            FileChannel fc2 = (FileChannel) Files.newByteChannel(Paths.get(wrote.getAbsolutePath()),
                    StandardOpenOption.READ);
            MappedByteBuffer buffer = fc2.map(FileChannel.MapMode.READ_ONLY, 0, fc2.size());

            byte[] arr = new byte[(int)fc2.size()];
            buffer.get(arr);

            for(byte b : arr)
                System.out.print((char)b);
            System.out.println();

//            assertArrayEquals(testPhrase.getBytes(), arr);
        }
        catch (IOException e)
        {
            return;
        }
    }

}
