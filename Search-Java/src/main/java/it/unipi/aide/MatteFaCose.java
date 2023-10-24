package it.unipi.aide;

import it.unipi.aide.model.Posting;
import it.unipi.aide.model.PostingList;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.model.Vocabulary;
import it.unipi.aide.utils.Commons;
import it.unipi.aide.utils.Compressor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatteFaCose {

    private static Vocabulary vocabulary;
    private static final String INPUT_PATH = "data/out/complete/";

        public static void main(String[] argv){
//        loadVocabulary();
//
//        List<Posting> pl = getPostingsByTerm("viru");
//        System.out.println(pl);
//        ByteBuffer bb = ByteBuffer.allocate(pl.size()*4);
//        for(Posting p : pl) {
//            bb.putInt(p.getFrequency());
//        }
//
//        byte[] compressed = Compressor.UnaryCompression(bb.array());
////        for(byte b: compressed) System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
//        int[] decompressed = Compressor.UnaryDecompression(compressed);
//        for(int i: decompressed) System.out.println(i);

            boolean talk = true;
            ArrayList<Integer> t = new ArrayList<>();
            for(int i = 0; i <80_000_000; i++){
                t.add(i);
                if(i % 10_000_000 == 0){
                    System.out.println(String.format("--VIR--\nMax: %.2f Mb\nTotal: %.2f Mb\nFree: %.2f Mb",Runtime.getRuntime().maxMemory()/Math.pow(10,6),Runtime.getRuntime().totalMemory()/Math.pow(10,6),Runtime.getRuntime().freeMemory()/Math.pow(10,6)));
                    OperatingSystemMXBean os = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
                    double totalMem = os.getTotalPhysicalMemorySize()/Math.pow(10,6);
                    double freeMem = os.getFreePhysicalMemorySize()/Math.pow(10,6);
                    double occMem = totalMem - freeMem;

                    System.out.println(String.format("--PHY--\nTotal: %.2f Mb\nFree: %.2f Mb\nOccupied: %.2f Mb\n-------",totalMem,freeMem,occMem));
                }
                if((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) > Runtime.getRuntime().maxMemory() * 5/10){
//                    if(talk){
                        talk = false;
                        System.out.println("Cleaning array");
                        t = new ArrayList<>();
                        System.gc();
//                    }
                }
                if(Runtime.getRuntime().totalMemory() > Runtime.getRuntime().maxMemory()*8/10) System.exit(9);
            }
    }

    /**
     * Load the entire vocabulary from the disk
     */
    private static void loadVocabulary() {
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
    private static TermInfo getNextVoc(FileChannel fileChannel, long offsetVocabulary) throws IOException{
        MappedByteBuffer tempBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offsetVocabulary, TermInfo.SIZE);

        byte[] termBytes = new byte[64];
        tempBuffer.get(termBytes);

        int frequency = tempBuffer.getInt();
        long offset = tempBuffer.getLong();
        int nPosting = tempBuffer.getInt();

        String term = new String(termBytes).trim();

        return new TermInfo(term, frequency, offset, nPosting);
    }

    /**
     * Get the posting list from bin blocks
     * @param term Term to retrieve posting list
     * @return Posting List of given term
     */
    private static List<Posting> getPostingsByTerm(String term)
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

}
