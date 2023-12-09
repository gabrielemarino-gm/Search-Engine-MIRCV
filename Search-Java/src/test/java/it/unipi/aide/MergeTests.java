package it.unipi.aide;

import it.unipi.aide.algorithms.Merging;
import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.ConfigReader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigReader.class, CollectionInformation.class})
public class MergeTests {

    File partialPath, outPath;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp(){
        try {
            // Temp paths
            partialPath = folder.newFolder("partial");
            outPath = folder.newFolder("out");

            // Fool the ConfigReader
            PowerMockito.mockStatic(ConfigReader.class);

            when(ConfigReader.getPartialPath()).thenReturn(partialPath.getAbsolutePath());

            when(ConfigReader.getVocabularyPath()).thenReturn(outPath.getAbsolutePath() + "/vocabulary");
            when(ConfigReader.getDocidPath()).thenReturn(outPath.getAbsolutePath() + "/docids");
            when(ConfigReader.getFrequencyPath()).thenReturn(outPath.getAbsolutePath() + "/frequencies");
            when(ConfigReader.getBlockDescriptorsPath()).thenReturn(outPath.getAbsolutePath() + "/blockDescriptors");

            Path partialV = Paths.get(partialPath.getAbsolutePath() + "/vocabularyBlock-");
            Path partialD = Paths.get(partialPath.getAbsolutePath() + "/docidsBlock-");
            Path partialF = Paths.get(partialPath.getAbsolutePath() + "/frequenciesBlock-");
            Path Stats = Paths.get(outPath.getAbsolutePath() + "/collectionStatistics");
            when(ConfigReader.getPartialVocabularyPath()).thenReturn(partialV.toString());
            when(ConfigReader.getPartialDocsPath()).thenReturn(partialD.toString());
            when(ConfigReader.getPartialFrequenciesPath()).thenReturn(partialF.toString());
            when(ConfigReader.getCollectionStatisticsPath()).thenReturn(Stats.toString());

            when(ConfigReader.getCompressionBlockSize()).thenReturn(512);
            when(ConfigReader.compressionEnabled()).thenReturn(true);
            when(ConfigReader.blockDivisionEnabled()).thenReturn(true);
            when(ConfigReader.getK()).thenReturn(1.2f);
            when(ConfigReader.getB()).thenReturn(0.75f);

            // Fool the CollectionInformation
            PowerMockito.mockStatic(CollectionInformation.class);

            when(CollectionInformation.getTotalDocuments()).thenReturn(3L);
            when(CollectionInformation.getTotalTerms()).thenReturn(4L);
            when(CollectionInformation.getAverageDocumentLength()).thenReturn(1L);

            fillTestFiles();
            runMerging();
        }
        catch (IOException e)
        {
            return;
        }
    }
    @Test
    public void testResults(){
        // Check the content of the files in outPath
        try
                (
                        FileChannel channelV = (FileChannel) Files.newByteChannel(
                                Paths.get(outPath.getAbsolutePath() + "/vocabulary"),
                                StandardOpenOption.READ);
                        FileChannel channelD = (FileChannel) Files.newByteChannel(
                                Paths.get(outPath.getAbsolutePath() + "/docids"),
                                StandardOpenOption.READ);
                        FileChannel channelF = (FileChannel) Files.newByteChannel(
                                Paths.get(outPath.getAbsolutePath() + "/frequencies"),
                                StandardOpenOption.READ);
                        ) {
            MappedByteBuffer bufferV, bufferD, bufferF;
            byte[] bV, bD, bF;

            bufferV = channelV.map(FileChannel.MapMode.READ_ONLY, 0, TermInfo.SIZE_PRE_MERGING);
            bV = new byte[(int) TermInfo.SIZE_PRE_MERGING];
            bufferV.get(bV);
            for(byte b : bV)
                System.out.println((char)b);

            bufferV = channelV.map(FileChannel.MapMode.READ_ONLY, TermInfo.SIZE_PRE_MERGING, TermInfo.SIZE_PRE_MERGING);
            bV = new byte[(int) TermInfo.SIZE_PRE_MERGING];
            bufferV.get(bV);
            for(byte b : bV)
                System.out.println((char)b);

            bufferV = channelV.map(FileChannel.MapMode.READ_ONLY, TermInfo.SIZE_PRE_MERGING * 2, TermInfo.SIZE_PRE_MERGING);
            bV = new byte[(int) TermInfo.SIZE_PRE_MERGING];
            bufferV.get(bV);
            for(byte b : bV)
                System.out.println((char)b);

            bufferV = channelV.map(FileChannel.MapMode.READ_ONLY, TermInfo.SIZE_PRE_MERGING * 3, TermInfo.SIZE_PRE_MERGING);
            bV = new byte[(int) TermInfo.SIZE_PRE_MERGING];
            bufferV.get(bV);
            for(byte b : bV)
                System.out.println((char)b);

        }
        catch (IOException e)
        {
            return;
        }
    }

    private void runMerging(){
        Merging m = new Merging(false, 2,false);
        m.mergeBlocks();
    }

    private void fillTestFiles(){
        try
                (
                        FileChannel channelV0 = (FileChannel) Files.newByteChannel(
                                Paths.get(partialPath.getAbsolutePath() + "/vocabularyBlock-0"),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        FileChannel channelD0 = (FileChannel) Files.newByteChannel(
                                Paths.get(partialPath.getAbsolutePath() + "/docidsBlock-0"),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        FileChannel channelF0 = (FileChannel) Files.newByteChannel(
                                Paths.get(partialPath.getAbsolutePath() + "/frequenciesBlock-0"),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

                        FileChannel channelV1 = (FileChannel) Files.newByteChannel(
                                Paths.get(partialPath.getAbsolutePath() + "/vocabularyBlock-1"),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        FileChannel channelD1 = (FileChannel) Files.newByteChannel(
                                Paths.get(partialPath.getAbsolutePath() + "/docidsBlock-1"),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        FileChannel channelF1 = (FileChannel) Files.newByteChannel(
                                Paths.get(partialPath.getAbsolutePath() + "/frequenciesBlock-1"),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        )
        {
            long off = 0;
            byte[] toAdd;
            MappedByteBuffer bufferV0, bufferV1;
            MappedByteBuffer bufferD0, bufferD1;
            MappedByteBuffer bufferF0, bufferF1;

            bufferV0 = channelV0.map(FileChannel.MapMode.READ_WRITE, off, TermInfo.SIZE_PRE_MERGING);
            toAdd = getBytes(new TermInfo("a", 1, 1, 0, 1, 1, 1));
            bufferV0.put(toAdd);
            off += TermInfo.SIZE_PRE_MERGING;

            bufferV0 = channelV0.map(FileChannel.MapMode.READ_WRITE, off, TermInfo.SIZE_PRE_MERGING);
            toAdd = getBytes(new TermInfo("b", 1, 1, 4, 1, 1, 1));
            bufferV0.put(toAdd);
            off += TermInfo.SIZE_PRE_MERGING;

            bufferV0 = channelV0.map(FileChannel.MapMode.READ_WRITE, off, TermInfo.SIZE_PRE_MERGING);
            toAdd = getBytes(new TermInfo("c", 1, 1, 8, 1, 1, 1));
            bufferV0.put(toAdd);

            off = 0;

            bufferV1 = channelV1.map(FileChannel.MapMode.READ_WRITE, off, TermInfo.SIZE_PRE_MERGING);
            toAdd = getBytes(new TermInfo("b", 1, 1, 0, 1, 1, 1));
            bufferV1.put(toAdd);
            off += TermInfo.SIZE_PRE_MERGING;

            bufferV1 = channelV1.map(FileChannel.MapMode.READ_WRITE, off, TermInfo.SIZE_PRE_MERGING);
            toAdd = getBytes(new TermInfo("c", 1, 1, 4, 1, 1, 1));
            bufferV1.put(toAdd);
            off += TermInfo.SIZE_PRE_MERGING;

            bufferV1 = channelV1.map(FileChannel.MapMode.READ_WRITE, off, TermInfo.SIZE_PRE_MERGING);
            toAdd = getBytes(new TermInfo("d", 1, 1, 8, 1, 1, 1));
            bufferV1.put(toAdd);
            off += TermInfo.SIZE_PRE_MERGING;

            bufferD0 = channelD0.map(FileChannel.MapMode.READ_WRITE, 0, 4);
            bufferF0 = channelF0.map(FileChannel.MapMode.READ_WRITE, 0, 4);
            bufferD0.putInt(0);
            bufferF0.putInt(1);
            bufferD0 = channelD0.map(FileChannel.MapMode.READ_WRITE, 4, 4);
            bufferF0 = channelF0.map(FileChannel.MapMode.READ_WRITE, 4, 4);
            bufferD0.putInt(0);
            bufferF0.putInt(1);
            bufferD0 = channelD0.map(FileChannel.MapMode.READ_WRITE, 8, 4);
            bufferF0 = channelF0.map(FileChannel.MapMode.READ_WRITE, 8, 4);
            bufferD0.putInt(1);
            bufferF0.putInt(1);


            bufferD1 = channelD1.map(FileChannel.MapMode.READ_WRITE, 0, 4);
            bufferF1 = channelF1.map(FileChannel.MapMode.READ_WRITE, 0, 4);
            bufferD1.putInt(0);
            bufferF1.putInt(1);
            bufferD1 = channelD1.map(FileChannel.MapMode.READ_WRITE, 4, 4);
            bufferF1 = channelF1.map(FileChannel.MapMode.READ_WRITE, 4, 4);
            bufferD1.putInt(0);
            bufferF1.putInt(1);
            bufferD1 = channelD1.map(FileChannel.MapMode.READ_WRITE, 8, 4);
            bufferF1 = channelF1.map(FileChannel.MapMode.READ_WRITE, 8, 4);
            bufferD1.putInt(1);
            bufferF1.putInt(1);

        }
        catch (IOException e)
        {
            return;
        }
    }

    private byte[] getBytes(TermInfo ti){
        byte [] toRet = new byte[(int)TermInfo.SIZE_PRE_MERGING];
        ByteBuffer buffer = ByteBuffer.wrap(toRet);
        String paddedTerm = String.format("%-" + TermInfo.SIZE_TERM + "s", ti.getTerm()).substring(0, TermInfo.SIZE_TERM);

        buffer.put(paddedTerm.getBytes());
        buffer.putInt(ti.getTotalFrequency());
        buffer.putInt(ti.getNumPosting());
        buffer.putLong(ti.getOffset());
        buffer.putInt(ti.getMaxTF());
        buffer.putInt(ti.getBM25TF());
        buffer.putInt(ti.getBM25DL());

        return buffer.array();
    }

}
