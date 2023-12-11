package it.unipi.aide;

import it.unipi.aide.algorithms.SPIMI;
import it.unipi.aide.model.TermInfo;
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
public class SPIMITests
{
    File partialPath, corpusFile, workingPath, debugPath, stopwordsFile;
    String testCorpus = "0\tThe cat is on the table\n" +
            "1\tWhat a beautiful day in London\n" +
            "2\tIt is raining cats and dogs\n";

    String stopwords =  "the\n" +
                        "is\n" +
                        "on\n" +
                        "a\n" +
                        "in\n" +
                        "it\n" +
                        "is\n" +
                        "and";

    // After stopword removal we have:
    // 0    cat table
    // 1    beautiful day
    // 2    rain cat dog

    // The rusult of the SPIMI algorithm holds in a single block, so we have:
    // TERM:            DOCIDs,             FREQUENCY
    // beautiful:       1                   1
    // cat:             0, 2                1, 1
    // day:             1                   1
    // dog:             2                   1
    // london:          1                   1
    // rain:            2                   1
    // table:           0                   1

    // In the file of docIds we have, in binary:
    byte[] docIdsFileResult;
    // In the file of frequencies we have, in binary:
    byte[] frequenciesFileResult;

    // In the file of vocabulary we have, in binary:
    byte[] vocabularyFileResul = new byte[518];

    // JUnit Temporary folder removed after tests
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp()
    {
        try
        {
            // Create a file (Test Corpus) in the temporary folder, to be read by SPIMI
            corpusFile = tempFolder.newFile("testCorpus.tsv");
            FileChannel fc1 = (FileChannel) Files.newByteChannel(Paths.get(corpusFile.getAbsolutePath()),
                    StandardOpenOption.READ, StandardOpenOption.WRITE);
            MappedByteBuffer buffer = fc1.map(FileChannel.MapMode.READ_WRITE, 0, testCorpus.length());

            buffer.put(testCorpus.getBytes());

            // Create a file (Stopwords) in the temporary folder, to be read by SPIMI
            stopwordsFile = tempFolder.newFile("stopwords.txt");
            FileChannel fc2 = (FileChannel) Files.newByteChannel(Paths.get(stopwordsFile.getAbsolutePath()),
                    StandardOpenOption.READ, StandardOpenOption.WRITE);
            MappedByteBuffer buffer2 = fc2.map(FileChannel.MapMode.READ_WRITE, 0, stopwords.length());

            buffer2.put(stopwords.getBytes());
        }
        catch (IOException e)
        {
            return;
        }

        try
        {
            // Create the folder that will contain the partial files
            partialPath = tempFolder.newFolder("partial");
            workingPath = tempFolder.newFolder("working");
            debugPath = tempFolder.newFolder("debug");

            // Mock the ConfigReader class
            PowerMockito.mockStatic(ConfigReader.class);
            when(ConfigReader.getPartialDocsPath()).thenReturn(partialPath.getAbsolutePath() + "/docidsBlock-");
            when(ConfigReader.getPartialFrequenciesPath()).thenReturn(partialPath.getAbsolutePath() + "/frequenciesBlock-");
            when(ConfigReader.getPartialVocabularyPath()).thenReturn(partialPath.getAbsolutePath() + "/vocabularyBlock-");
            when(ConfigReader.getDocumentIndexPath()).thenReturn(tempFolder.getRoot().getAbsolutePath() + "/documentIndex");
            when(ConfigReader.getDoclens()).thenReturn(tempFolder.getRoot().getAbsolutePath() + "/doclens");
            when(ConfigReader.getStopwordsPath()).thenReturn(tempFolder.getRoot().getAbsolutePath() + "/stopwords.txt");
            when(ConfigReader.getWorkingDir()).thenReturn(workingPath.getAbsolutePath());
            when(ConfigReader.getDebugDir()).thenReturn(debugPath.getAbsolutePath());

        }
        catch (IOException e)
        {
            return;
        }


        // Create the expected result of the SPIMI algorithm
        docIdsFileResult = new byte[]{(byte) 1, (byte) 0, (byte) 2, (byte) 1, (byte) 2, (byte) 2, (byte) 0};
        frequenciesFileResult = new byte[]{(byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1};

        String[] vocs = {"beautiful", "cat", "day", "dog", "London", "rain", "table"};

        int concatOffset = 0;
        byte[] byteArr;

        for (String term: vocs)
        {
            String paddedTerm = String.format("%-" + TermInfo.SIZE_TERM + "s", term).substring(0, TermInfo.SIZE_TERM); // Pad with spaces up to 46 characters
            // Copy the term
            System.arraycopy(paddedTerm.getBytes(), 0, vocabularyFileResul, concatOffset, TermInfo.SIZE_TERM);
            concatOffset += TermInfo.SIZE_TERM;

            if (term.equals("cat"))
            {

                byteArr = new byte[]
                        {
                                (byte) 2,           // Total Frequency                                  4 bytes
                                (byte) 2,           // Number of posting lists                          4 bytes
                                (byte) (long) 1,    // Offset                                           8 bytes
                                (byte) 1,           // Max term frequency                               4 bytes
                                (byte) 1,           // Term frequency that maximizes the BM25 score     4 bytes
                                (byte) 1,           // Doc length that maximizes the BM25 score         4 bytes
                        };

                System.arraycopy(byteArr, 0, vocabularyFileResul, concatOffset, 6);
                concatOffset += 28;
            }
            else
            {
                byteArr = new byte[]
                        {
                                (byte) 1,           // Total Frequency                                  4 bytes
                                (byte) 1,           // Number of posting lists                          4 bytes
                                (byte) (long) 1,    // Offset                                           8 bytes
                                (byte) 1,           // Max term frequency                               4 bytes
                                (byte) 1,           // Term frequency that maximizes the BM25 score     4 bytes
                                (byte) 1,           // Doc length that maximizes the BM25 score         4 bytes
                        };

                System.arraycopy(byteArr, 0, vocabularyFileResul, concatOffset, 6);
                concatOffset += 28;
            }
        }
    }

    @Test
    public void testConstructor()
    {
        SPIMI spimi = new SPIMI(tempFolder.getRoot().getAbsolutePath(), true);
        assertNotNull(spimi);
    }

    @Test
    public void testAlgorithm()
    {

        // Esecuzione del SPIMI con i file test fittizi
        SPIMI spimi = new SPIMI(tempFolder.getRoot().getAbsolutePath(), true);
        int numBlocks = spimi.algorithm(false);

        try
        {
            for (int blockIndx = 0; blockIndx < numBlocks; blockIndx++)
            {
                String docPath = ConfigReader.getPartialDocsPath() + blockIndx;
                String freqPath = ConfigReader.getPartialFrequenciesPath() + blockIndx;
                String vocPath = ConfigReader.getPartialVocabularyPath() + blockIndx;

                FileChannel docIdFileChannel = (FileChannel) Files.newByteChannel(Paths.get(docPath),
                        StandardOpenOption.READ, StandardOpenOption.CREATE);

                FileChannel frequencyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                        StandardOpenOption.READ, StandardOpenOption.CREATE);

                FileChannel vocabularyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                        StandardOpenOption.READ, StandardOpenOption.CREATE);

                // Lettura su un file
                MappedByteBuffer docIdBuffer = docIdFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, docIdFileChannel.size());
                MappedByteBuffer frequencyBuffer = frequencyFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, frequencyFileChannel.size());
                MappedByteBuffer vocabularyBuffer = vocabularyFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, vocabularyFileChannel.size());

                // Read the content of the file in byte
                byte[] docIdArr = new byte[(int)docIdFileChannel.size()];
                byte[] frequencyArr = new byte[(int)frequencyFileChannel.size()];
                byte[] vocabularyArr = new byte[(int)vocabularyFileChannel.size()];

                docIdBuffer.get(docIdArr);
                frequencyBuffer.get(frequencyArr);
                vocabularyBuffer.get(vocabularyArr);

                // Test if the content of the file is equal to the expected one
                assertArrayEquals(docIdsFileResult, docIdArr);
                assertArrayEquals(frequenciesFileResult, frequencyArr);
                assertArrayEquals(vocabularyFileResul, vocabularyArr);
            }
        }
        catch (IOException e)
        {
            return;
        }
    }
}
