package it.unipi.aide;

import it.unipi.aide.algorithms.DAAT;
import it.unipi.aide.algorithms.MaxScore;
import it.unipi.aide.model.*;
import it.unipi.aide.utils.ConfigReader;

import org.junit.*;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigReader.class, CollectionInformation.class})
public class QueryTest {

    File outPath;

    long totalDocuments, averageDocumentLength, totalTerms;

    Document[] trainDocuments;
    byte[] trainDoclens;

    BlockDescriptor[] trainBlockDescriptors;

    TermInfo[] trainVocabs;
    int[] trainDocIDS, trainFrequencies;

    @Rule
    TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp()
    {
        try {
            outPath = folder.newFolder("out");

            PowerMockito.mockStatic(ConfigReader.class);
            PowerMockito.mockStatic(CollectionInformation.class);

            populateStuff();

            redefineConfigReader();
            redefineCollectionInformation();

            createFakeFiles();
        }
        catch (IOException e)
        {
            return;
        }
    }

    @Test
    public void testDAAT()
    {
        DAAT daat = new DAAT();
        for(ScoredDocument sd : daat.executeDAAT(Arrays.asList(
                new String[]{"brown", "kitti"}), false, 5))
        {
            System.out.print(sd);
        }
    }

    @Test
    public void testMaxScore()
    {
        MaxScore maxScore = new MaxScore();
        for(ScoredDocument sd : maxScore.executeMaxScore(
                Arrays.asList(new String[]{"quick", "speedi"}), false, 5))
        {
            System.out.print(sd);
        }
    }


    /*
    * --------------------------------------------------
    * --------------------------------------------------
    * --------------------------------------------------
    */

    private void populateStuff()
    {
        totalDocuments = 10;
        averageDocumentLength = 6;
        totalTerms = 54;

        /*
        * 0     The quick brown fox jumps over the lazy dog
        * 1     A quick brown rabbit hops over the lazy cat
        * 2     Swift brown wolves leap across the drowsy feline
        * 3     A speedy tan hare vaults over the lethargic kitty
        * 4     Nimble beige rodents dash past the sluggish tabby
        * 5     The fast red squirrel sprints beyond the snoozing kitten
        * 6     Rapid ginger weasels dart over the dozing tomcat
        * 7     A fleet maroon ferret races by the slumbering moggy
        * 8     Quick russet minks scamper around the napping puss
        * 9     Speedy copper martens zoom by the resting kitty
        */
        trainDocuments = new Document[]
                {
                        new Document("0",0,6),
                        new Document("1",1,6),
                        new Document("2",2,6),
                        new Document("3",3,6),
                        new Document("4",4,7),
                        new Document("5",5,6),
                        new Document("6",6,6),
                        new Document("7",7,6),
                        new Document("8",8,6),
                        new Document("9",9,6),
                };
        trainDoclens = new byte[]
                { /* Compressed Variable-Byte */
                        (byte) 6,
                        (byte) 6,
                        (byte) 6,
                        (byte) 6,
                        (byte) 7,
                        (byte) 6,
                        (byte) 6,
                        (byte) 6,
                        (byte) 6,
                        (byte) 6
                };

        /*
        * 0     quick brown fox jump lazi dog
        * 1     quick brown rabbit hop lazi cat
        * 2     swift brown wolv leap drowsi felin
        * 3     speedi tan hare vault letharg kitti
        * 4     nimbl beig rodent dash past sluggish tabbi
        * 5     fast red squirrel sprint snooz kitten
        * 6     rapid ginger weasel dart doze tomcat
        * 7     fleet maroon ferret race slumber moggi
        * 8     quick russet mink scamper nap puss
        * 9     speedi copper marten zoom rest kitti
        *
        *
        * beig [4:1]
        * brown [0:1][1:1][2:1]
        * cat [1:1]
        * copper [9:1]
        * dart [6:1]
        * dash[4:1]
        * dog [0:1]
        * doze [6:1]
        * drowsi [2:1]
        * fast [5:1]
        * felin [2:1]
        * ferret [7:1]
        * fleet [7:1]
        * fox [0:1]
        * ginger [6:1]
        * hare [3:1]
        * hop [1:1]
        * jump [0:1]
        * kitten [5:1]
        * kitti [3:1][9:1]
        * lazi [0:1][1:1]
        * leap [2:1]
        * letharg [3:1]
        * maroon [7:1]
        * marten [9:1]
        * mink [8:1]
        * moggi [7:1]
        * nap [8:1]
        * nimbl [4:1]
        * past [4:1]
        * puss [8:1]
        * quick [0:1][1:1][8:1]
        * rabbit [1:1]
        * race [7:1]
        * rapid [6:1]
        * red [5:1]
        * rest [9:1]
        * rodent [4:1]
        * russet [8:1]
        * scamper [8:1]
        * sluggish [4:1]
        * slumber [7:1]
        * snooz [5:1]
        * speedi [3:1][9:1]
        * sprint [5:1]
        * squirrel [5:1]
        * swift [2:1]
        * tabbi [4:1]
        * tan [3:1]
        * tomcat [6:1]
        * vault [3:1]
        * weasel [6:1]
        * wolv [2:1]
        * zoom [9:1]
        */

        trainVocabs = new TermInfo[]
                {
                        new TermInfo("beig", 1, 1 ,0L, 1, 0.0f, 0.0f),
                        new TermInfo("brown", 3, 3, 40L, 1, 0.0f, 0.0f),
                        new TermInfo("cat", 1, 1, 80L, 1, 0.0f, 0.0f),
                        new TermInfo("copper", 1, 1, 120L, 1, 0.0f, 0.0f),
                        new TermInfo("dart", 1, 1, 160L, 1, 0.0f, 0.0f),
                        new TermInfo("dash", 1, 1, 200L, 1, 0.0f, 0.0f),
                        new TermInfo("dog", 1, 1, 240L, 1, 0.0f, 0.0f),
                        new TermInfo("doze", 1, 1, 280L, 1, 0.0f, 0.0f),
                        new TermInfo("drowsi", 1, 1, 320L, 1, 0.0f, 0.0f),
                        new TermInfo("fast", 1, 1, 360L, 1, 0.0f, 0.0f),
                        new TermInfo("felin", 1, 1, 400L, 1, 0.0f, 0.0f),
                        new TermInfo("ferret", 1, 1, 440L, 1, 0.0f, 0.0f),
                        new TermInfo("fleet", 1, 1, 480L, 1, 0.0f, 0.0f),
                        new TermInfo("fox", 1, 1, 520L, 1, 0.0f, 0.0f),
                        new TermInfo("ginger", 1, 1, 560L, 1, 0.0f, 0.0f),
                        new TermInfo("hare", 1, 1, 600L, 1, 0.0f, 0.0f),
                        new TermInfo("hop", 1, 1, 640L, 1, 0.0f, 0.0f),
                        new TermInfo("jump", 1, 1, 680L, 1, 0.0f, 0.0f),
                        new TermInfo("kitten", 1, 1, 720L, 1, 0.0f, 0.0f),
                        new TermInfo("kitti", 2, 2, 760L, 1, 0.0f, 0.0f),
                        new TermInfo("lazi", 2, 2, 800L, 1, 0.0f, 0.0f),
                        new TermInfo("leap", 1, 1, 840L, 1, 0.0f, 0.0f),
                        new TermInfo("letharg", 1, 1, 880L, 1, 0.0f, 0.0f),
                        new TermInfo("maroon", 1, 1, 920L, 1, 0.0f, 0.0f),
                        new TermInfo("marten", 1, 1, 960L, 1, 0.0f, 0.0f),
                        new TermInfo("mink", 1, 1, 1000L, 1, 0.0f, 0.0f),
                        new TermInfo("moggi", 1, 1, 1040L, 1, 0.0f, 0.0f),
                        new TermInfo("nap", 1, 1, 1080L, 1, 0.0f, 0.0f),
                        new TermInfo("nimbl", 1, 1, 1120L, 1, 0.0f, 0.0f),
                        new TermInfo("past", 1, 1, 1160L, 1, 0.0f, 0.0f),
                        new TermInfo("puss", 1, 1, 1200L, 1, 0.0f, 0.0f),
                        new TermInfo("quick", 3, 3, 1240L, 1, 0.0f, 0.0f),
                        new TermInfo("rabbit", 1, 1, 1280L, 1, 0.0f, 0.0f),
                        new TermInfo("race", 1, 1, 1320L, 1, 0.0f, 0.0f),
                        new TermInfo("rapid", 1, 1, 1360L, 1, 0.0f, 0.0f),
                        new TermInfo("red", 1, 1, 1400L, 1, 0.0f, 0.0f),
                        new TermInfo("rest", 1, 1, 1440L, 1, 0.0f, 0.0f),
                        new TermInfo("rodent", 1, 1, 1480L, 1, 0.0f, 0.0f),
                        new TermInfo("russet", 1, 1, 1520L, 1, 0.0f, 0.0f),
                        new TermInfo("scamper", 1, 1, 1560L, 1, 0.0f, 0.0f),
                        new TermInfo("sluggish", 1, 1, 1600L, 1, 0.0f, 0.0f),
                        new TermInfo("slumber", 1, 1, 1640L, 1, 0.0f, 0.0f),
                        new TermInfo("snooz", 1, 1, 1680L, 1, 0.0f, 0.0f),
                        new TermInfo("speedi", 2, 2, 1720L, 1, 0.0f, 0.0f),
                        new TermInfo("sprint", 1, 1, 1760L, 1, 0.0f, 0.0f),
                        new TermInfo("squirrel", 1, 1, 1800L, 1, 0.0f, 0.0f),
                        new TermInfo("swift", 1, 1, 1840L, 1, 0.0f, 0.0f),
                        new TermInfo("tabbi", 1, 1, 1880L, 1, 0.0f, 0.0f),
                        new TermInfo("tan", 1, 1, 1920L, 1, 0.0f, 0.0f),
                        new TermInfo("tomcat", 1, 1, 1960L, 1, 0.0f, 0.0f),
                        new TermInfo("vault", 1, 1, 2000L, 1, 0.0f, 0.0f),
                        new TermInfo("weasel", 1, 1, 2040L, 1, 0.0f, 0.0f),
                        new TermInfo("wolv", 1, 1, 2080L, 1, 0.0f, 0.0f),
                        new TermInfo("zoom", 1, 1, 2120L, 1, 0.0f, 0.0f),
                };

        trainBlockDescriptors = new BlockDescriptor[]
                {
                        new BlockDescriptor(4, 1, 0L, 0L, 4L, 4L),
                        new BlockDescriptor(2, 3, 4L, 4L, 12L, 12L),
                        new BlockDescriptor(1, 1, 16L, 16L, 4L, 4L),
                        new BlockDescriptor(9, 1, 20L, 20L, 4L, 4L),
                        new BlockDescriptor(6, 1, 24L, 24L, 4L, 4L),
                        new BlockDescriptor(4, 1, 28L, 28L, 4L, 4L),
                        new BlockDescriptor(0, 1, 32L, 32L, 4L, 4L),
                        new BlockDescriptor(6, 1, 36L, 36L, 4L, 4L),
                        new BlockDescriptor(2, 1, 40L, 40L, 4L, 4L),
                        new BlockDescriptor(5, 1, 44L, 44L, 4L, 4L),
                        new BlockDescriptor(2, 1, 48L, 48L, 4L, 4L),
                        new BlockDescriptor(7, 1, 52L, 52L, 4L, 4L),
                        new BlockDescriptor(7, 1, 56L, 56L, 4L, 4L),
                        new BlockDescriptor(0, 1, 60L, 60L, 4L, 4L),
                        new BlockDescriptor(6, 1, 64L, 64L, 4L, 4L),
                        new BlockDescriptor(3, 1, 68L, 68L, 4L, 4L),
                        new BlockDescriptor(1, 1, 72L, 72L, 4L, 4L),
                        new BlockDescriptor(0, 1, 76L, 76L, 4L, 4L),
                        new BlockDescriptor(5, 1, 80L, 80L, 4L, 4L),
                        new BlockDescriptor(9, 2, 84L, 84L, 12L, 12L),
                        new BlockDescriptor(1, 2, 96L, 96L, 12L, 12L),
                        new BlockDescriptor(2, 1, 108L, 108L, 4L, 4L),
                        new BlockDescriptor(3, 1, 112L, 112L, 4L, 4L),
                        new BlockDescriptor(7, 1, 116L, 116L, 4L, 4L),
                        new BlockDescriptor(9, 1, 120L, 120L, 4L, 4L),
                        new BlockDescriptor(8, 1, 124L, 124L, 4L, 4L),
                        new BlockDescriptor(7, 1, 128L, 128L, 4L, 4L),
                        new BlockDescriptor(8, 1, 132L, 132L, 4L, 4L),
                        new BlockDescriptor(4, 1, 136L, 136L, 4L, 4L),
                        new BlockDescriptor(4, 1, 140L, 140L, 4L, 4L),
                        new BlockDescriptor(8, 1, 144L, 144L, 4L, 4L),
                        new BlockDescriptor(8, 3, 148L, 148L, 12L, 12L),
                        new BlockDescriptor(1, 1, 160L, 160L, 4L, 4L),
                        new BlockDescriptor(7, 1, 164L, 164L, 4L, 4L),
                        new BlockDescriptor(6, 1, 168L, 168L, 4L, 4L),
                        new BlockDescriptor(5, 1, 172L, 172L, 4L, 4L),
                        new BlockDescriptor(9, 1, 176L, 176L, 4L, 4L),
                        new BlockDescriptor(4, 1, 180L, 180L, 4L, 4L),
                        new BlockDescriptor(8, 1, 184L, 184L, 4L, 4L),
                        new BlockDescriptor(8, 1, 188L, 188L, 4L, 4L),
                        new BlockDescriptor(4, 1, 192L, 192L, 4L, 4L),
                        new BlockDescriptor(7, 1, 196L, 196L, 4L, 4L),
                        new BlockDescriptor(5, 1, 200L, 200L, 4L, 4L),
                        new BlockDescriptor(9, 2, 204L, 204L, 8L, 8L),
                        new BlockDescriptor(5, 1, 212L, 212L, 4L, 4L),
                        new BlockDescriptor(5, 1, 216L, 216L, 4L, 4L),
                        new BlockDescriptor(2, 1, 220L, 220L, 4L, 4L),
                        new BlockDescriptor(4, 1, 224L, 224L, 4L, 4L),
                        new BlockDescriptor(3, 1, 228L, 228L, 4L, 4L),
                        new BlockDescriptor(6, 1, 232L, 232L, 4L, 4L),
                        new BlockDescriptor(3, 1, 236L, 236L, 4L, 4L),
                        new BlockDescriptor(6, 1, 240L, 240L, 4L, 4L),
                        new BlockDescriptor(2, 1, 244L, 244L, 4L, 4L),
                        new BlockDescriptor(9, 1, 248L, 248L, 4L, 4L)
                };

        trainDocIDS = new int[]
                {
                        4,
                        0,1,2,
                        1,
                        9,
                        6,
                        4,
                        0,
                        6,
                        2,
                        5,
                        2,
                        7,
                        7,
                        0,
                        6,
                        3,
                        1,
                        0,
                        5,
                        3,9,
                        0,1,
                        2,
                        3,
                        7,
                        9,
                        8,
                        7,
                        8,
                        4,
                        4,
                        8,
                        0,1,8,
                        1,
                        7,
                        6,
                        5,
                        9,
                        4,
                        8,
                        8,
                        4,
                        7,
                        5,
                        3,9,
                        5,
                        5,
                        2,
                        4,
                        3,
                        6,
                        3,
                        6,
                        2,
                        9
                };

        trainFrequencies = new int[]
                {
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                        1
                };
    }

    private void createFakeFiles()
    {
        File documentIndexPath = new File(outPath.getAbsolutePath() + "/documentIndex");
        File doclens = new File(outPath.getAbsolutePath() + "/doclens");

        File blockDescriptorsPath = new File(outPath.getAbsolutePath() + "/blockDescriptors");

        File vocabularyPath = new File(outPath.getAbsolutePath() + "/vocabulary");
        File docidPath = new File(outPath.getAbsolutePath() + "/docids");
        File frequencyPath = new File(outPath.getAbsolutePath() + "/frequencies");

        /* -------------- */
        try
        {
            writeDocuments(documentIndexPath);
            writeDoclens(doclens);

            writeBlockDescriptors(blockDescriptorsPath);

            writeVocabulary(vocabularyPath);
            writeDocids(docidPath);
            writeFrequencies(frequencyPath);
        }
        catch (IOException e)
        {
            return;
        }
    }

    /* ------------------------------------------ */
    private void writeDocuments(File file) throws IOException
    {
        FileChannel fc = (FileChannel) Files.newByteChannel(file.toPath(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.READ,
                java.nio.file.StandardOpenOption.WRITE);

        long howMany = Document.SIZE * trainDocuments.length;
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, howMany);
        for (Document d : trainDocuments)
        {
            String pid =  String.format("%-" + Document.PID_SIZE + "s", d.getPid()).substring(0, Document.PID_SIZE);
            buffer.put(pid.getBytes());
            buffer.putInt(d.getDocid());
            buffer.putInt(d.getTokenCount());
        }

        fc.close();
    }

    private void writeDoclens(File file) throws IOException
    {
        FileChannel fc = (FileChannel) Files.newByteChannel(file.toPath(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.READ,
                java.nio.file.StandardOpenOption.WRITE);
        long howMany = trainDoclens.length;
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, howMany);

        buffer.put(trainDoclens);

        fc.close();
    }

    private void writeBlockDescriptors(File file) throws IOException
    {
        FileChannel fc = (FileChannel) Files.newByteChannel(file.toPath(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.READ,
                java.nio.file.StandardOpenOption.WRITE);
        long howMany = BlockDescriptor.BLOCK_SIZE * trainBlockDescriptors.length;
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, howMany);

        for(BlockDescriptor bd : trainBlockDescriptors)
        {
            buffer.putInt(bd.getMaxDocid());
            buffer.putInt(bd.getNumPostings());
            buffer.putLong(bd.getOffsetDocid());
            buffer.putLong(bd.getOffsetFreq());
            buffer.putLong(bd.getBytesOccupiedDocid());
            buffer.putLong(bd.getBytesOccupiedFreq());
        }

        fc.close();
    }

    private void writeVocabulary(File file) throws IOException
    {
        FileChannel fc = (FileChannel) Files.newByteChannel(file.toPath(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.READ,
                java.nio.file.StandardOpenOption.WRITE);
        long howMany = trainVocabs.length * TermInfo.SIZE_POST_MERGING;
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, howMany);

        for (TermInfo ti : trainVocabs)
        {
            buffer.put(
                    getBytes(ti)
            );
        }

        fc.close();
    }

    private void writeDocids(File file) throws IOException
    {
        FileChannel fc = (FileChannel) Files.newByteChannel(file.toPath(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.READ,
                java.nio.file.StandardOpenOption.WRITE);
        long howMany = 4L * trainDocIDS.length;
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, howMany);

        for (int docid : trainDocIDS)
        {
            buffer.putInt(docid);
        }

        fc.close();
    }

    private void writeFrequencies(File file) throws IOException
    {
        FileChannel fc = (FileChannel) Files.newByteChannel(file.toPath(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.READ,
                java.nio.file.StandardOpenOption.WRITE);
        long howMany = 4L * trainFrequencies.length;
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, howMany);

        for (int frequency : trainFrequencies)
        {
            buffer.putInt(frequency);
        }

        fc.close();
    }

    /* ------------------------------------------ */

    private void redefineConfigReader()
    {
        PowerMockito.when(ConfigReader.getWorkingDir()).thenReturn(outPath.getAbsolutePath());
        PowerMockito.when(ConfigReader.getDocumentIndexPath()).thenReturn(outPath.getAbsolutePath() + "/documentIndex");
        PowerMockito.when(ConfigReader.getDoclens()).thenReturn(outPath.getAbsolutePath() + "/doclens");
        PowerMockito.when(ConfigReader.getBlockDescriptorsPath()).thenReturn(outPath.getAbsolutePath() + "/blockDescriptors");
        PowerMockito.when(ConfigReader.getVocabularyPath()).thenReturn(outPath.getAbsolutePath() + "/vocabulary");
        PowerMockito.when(ConfigReader.getDocidPath()).thenReturn(outPath.getAbsolutePath() + "/docids");
        PowerMockito.when(ConfigReader.getFrequencyPath()).thenReturn(outPath.getAbsolutePath() + "/frequencies");
        PowerMockito.when(ConfigReader.compressionEnabled()).thenReturn(false);
        PowerMockito.when(ConfigReader.getK()).thenReturn(1.2f);
        PowerMockito.when(ConfigReader.getB()).thenReturn(0.75f);
    }

    private void redefineCollectionInformation()
    {
        PowerMockito.when(CollectionInformation.getTotalDocuments()).thenReturn(totalDocuments);
        PowerMockito.when(CollectionInformation.getAverageDocumentLength()).thenReturn(averageDocumentLength);
        PowerMockito.when(CollectionInformation.getTotalTerms()).thenReturn(totalTerms);
    }

    private byte[] getBytes(TermInfo ti){
        byte [] toRet = new byte[(int)TermInfo.SIZE_POST_MERGING];
        ByteBuffer buffer = ByteBuffer.wrap(toRet);
        String paddedTerm = String.format("%-" + TermInfo.SIZE_TERM + "s", ti.getTerm()).substring(0, TermInfo.SIZE_TERM);

        buffer.put(paddedTerm.getBytes());

        buffer.putInt(ti.getTotalFrequency());
        buffer.putInt(ti.getNumPosting());
        buffer.putInt(ti.getNumBlocks());
        buffer.putLong(ti.getOffset());
        buffer.putFloat(ti.getTermUpperBoundTFIDF());
        buffer.putFloat(ti.getTermUpperBoundBM25());

        return buffer.array();
    }
}
