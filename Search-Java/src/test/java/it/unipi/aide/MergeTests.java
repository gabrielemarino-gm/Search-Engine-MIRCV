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
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            when(ConfigReader.compressionEnabled()).thenReturn(false);
            when(ConfigReader.blockDivisionEnabled()).thenReturn(false);
            when(ConfigReader.getK()).thenReturn(1.2f);
            when(ConfigReader.getB()).thenReturn(0.75f);

            // Fool the CollectionInformation
            PowerMockito.mockStatic(CollectionInformation.class);

            when(CollectionInformation.getTotalDocuments()).thenReturn(5L);
            when(CollectionInformation.getTotalTerms()).thenReturn(5L);
            when(CollectionInformation.getAverageDocumentLength()).thenReturn(6L);


            fillTestFiles(0, new ArrayList<>(Arrays.asList(new TermInfo[]{
                    new TermInfo("a",3,1,0L,3,3,5),
                    new TermInfo("b",4,2,4L,2,2,5),
                    new TermInfo("c",5,1,12L,5,5,7)
            })), new ArrayList<>(Arrays.asList(new Integer[] {
                    0, 0, 1, 1
            })), new ArrayList<>(Arrays.asList(new Integer[] {
                    3, 2, 2, 5
            })));

            fillTestFiles(1, new ArrayList<>(Arrays.asList(new TermInfo[]{
                    new TermInfo("b",4,2,0L,3,3,5),
                    new TermInfo("c",3,2,8L,2,2,5),
                    new TermInfo("d",3,1,16L,3,3,5)
            })), new ArrayList<>(Arrays.asList(new Integer[] {
                    2, 3, 2, 3, 3
            })), new ArrayList<>(Arrays.asList(new Integer[] {
                    3, 1, 2, 1, 3
            })));

            fillTestFiles(2, new ArrayList<>(Arrays.asList(new TermInfo[]{
                    new TermInfo("a",2,1,0L,2,2,9),
                    new TermInfo("d",2,1,4L,2,2,9),
                    new TermInfo("e",5,1,8L,5,5,9)
            })), new ArrayList<>(Arrays.asList(new Integer[] {
                    4, 4, 4
            })), new ArrayList<>(Arrays.asList(new Integer[] {
                    2, 2, 5
            })));

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

            long off = 0;
            for(long i = 0; i < 5; i++){
                bufferV = channelV.map(FileChannel.MapMode.READ_ONLY, TermInfo.SIZE_POST_MERGING * i, TermInfo.SIZE_POST_MERGING);
                byte[] termBytes = new byte[TermInfo.SIZE_TERM];

                bufferV.get(termBytes);
                String term = new String(termBytes).trim();

                int frequency = bufferV.getInt();
                int nPosting = bufferV.getInt();
                int numBlocks = bufferV.getInt();
                long offset = bufferV.getLong();

                float TFIDF = bufferV.getFloat();
                float BM25 = bufferV.getFloat();

                System.out.print(String.format("[%s]<%d:%d><%d:%d><%f,%f>",term,frequency,nPosting,numBlocks,offset,TFIDF,BM25));

                for(int j = 0; j < nPosting; j++)
                {
                    bufferD = channelD.map(FileChannel.MapMode.READ_ONLY, off, 4);
                    bufferF = channelF.map(FileChannel.MapMode.READ_ONLY, off, 4);

                    System.out.print(String.format("[%d:%d]", bufferD.getInt(), bufferF.getInt()));

                    off += 4;
                }

                System.out.println();
            }


        }
        catch (IOException e)
        {
            return;
        }
    }

    private void runMerging(){
        Merging m = new Merging(false, 3, false);
        m.mergeBlocks();
    }

    private void fillTestFiles(int i, List<TermInfo> termInfos, List<Integer> docs, List<Integer> freqs){
        try
                (
                        FileChannel channelV = (FileChannel) Files.newByteChannel(
                                Paths.get(String.format("%s/vocabularyBlock-%d", partialPath.getAbsolutePath(), i)),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        FileChannel channelD = (FileChannel) Files.newByteChannel(
                                Paths.get( String.format("%s/docidsBlock-%d", partialPath.getAbsolutePath(), i)),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        FileChannel channelF = (FileChannel) Files.newByteChannel(
                                Paths.get(String.format("%s/frequenciesBlock-%d", partialPath.getAbsolutePath(), i)),
                                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
                        )
        {
            long off = 0;
            byte[] toAdd;
            MappedByteBuffer bufferV, bufferD, bufferF;

            for(int j = 0; j < termInfos.size(); j++) {
                bufferV = channelV.map(FileChannel.MapMode.READ_WRITE, off, TermInfo.SIZE_PRE_MERGING);
                toAdd = getBytes(termInfos.get(j));
                bufferV.put(toAdd);
                off += TermInfo.SIZE_PRE_MERGING;
            }

            off = 0;

            for(int j = 0; j < freqs.size(); j++)
            {
                bufferD = channelD.map(FileChannel.MapMode.READ_WRITE, off, 4);
                bufferF = channelF.map(FileChannel.MapMode.READ_WRITE, off, 4);
                bufferD.putInt(docs.get(j));
                bufferF.putInt(freqs.get(j));
                off += 4;
            }
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
