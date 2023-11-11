package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.Commons;
import it.unipi.aide.utils.Compressor;
import it.unipi.aide.utils.ConfigReader;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DAAT {

    private int K;
    boolean COMPRESSION;

    HashMap<String, TermInfo> terms = new HashMap<>();

    public DAAT(int k){
        this.K = k;
        COMPRESSION = true;
    }

    public void testRetrievalCompression(){
        TermInfo toRetrieve1 = binarySearch("bomb");
        TermInfo toRetrieve2 = binarySearch("atom");

        System.out.println(toRetrieve1);
        System.out.println(toRetrieve2);

        PostingList pl1 = new PostingList("bomb", getPostingsByTerm(toRetrieve1));
        PostingList pl2 = new PostingList("atom", getPostingsByTerm(toRetrieve2));

        System.out.println(pl1);
        System.out.println(pl2);

    }

    public List<ScoredDocument> executeDAAT(List<String> queryTerms){
        // Retrieve the posting lists of the query terms
        List<PostingList> postingLists= new ArrayList<>();
        for(String t : queryTerms){
            /* TODO -> Search inside a cache before performing binary search */
            TermInfo toRetrieve = binarySearch(t);
            terms.put(t, toRetrieve);
            postingLists.add(new PostingList(t, getPostingsByTerm(toRetrieve)));
        }

        // If searched terms are not in the vocabulary, return null
        if(postingLists.isEmpty()) return null;

        List<ScoredDocument> scoredDocuments = new ArrayList<>();
        boolean stop = false;

        while(!stop){
            // Hypothesis that all the Posting Lists are empty
            stop = true;
            int firstDoc = getSmallestDocid(postingLists);
            ScoredDocument toAdd = new ScoredDocument(firstDoc, 0);
            for(PostingList pl : postingLists){
                // If at least one Posting List has elements, Hypothesis became false
                if (pl.getCurrent() != null) {
                    stop = false;
                    // If Posting List of current term has docId equals to the smallest under consideration, calculate its score
                    if (pl.getCurrent().getDocId() == firstDoc) {
                        // TODO -> Scoring functions apart and well distinct
                        toAdd.setScore(
                                (1 + Math.log(pl.getCurrent().getFrequency())) * Math.log((double) CollectionInformation.getTotalDocuments() / terms.get(pl.getTerm()).getNumPosting())
                        );
                        pl.next();
                    }
                }
            }
            scoredDocuments.add(toAdd);
        }

        // Sort the documents by score
        scoredDocuments.sort((o1, o2) -> {
            if(o1.getScore() > o2.getScore()) return -1;
            else if(o1.getScore() < o2.getScore()) return 1;
            else return 0;
        });

        // Return top-k documents
        return scoredDocuments.subList(0, K);
    }

    private int getSmallestDocid(List<PostingList> postingLists){
        int min = Integer.MAX_VALUE;

        for(PostingList pl : postingLists){
            if(pl.getCurrent() != null && pl.getCurrent().getDocId() < min){
                min = pl.getCurrent().getDocId();
            }
        }
        return min;
    }

    private TermInfo binarySearch(String term)
    {
        try(
                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(ConfigReader.getVocabularyPath()),
                        StandardOpenOption.READ);
        )
        {
            long WIN_DOWN = 0;
            long WIN_UP = CollectionInformation.getTotalTerms();
            while (true)
            {
                long MID_POINT = (WIN_UP - WIN_DOWN)/ 2 + WIN_DOWN;
                if(MID_POINT == 0) return null;
                TermInfo middleTerm = getTerm(channel, MID_POINT);

                int comp = middleTerm.getTerm().compareTo(term);
                if (comp == 0)
                {
                    // Found
                    return middleTerm;
                }
                else if (comp > 0)
                {
                    // Right half
                    WIN_UP = MID_POINT;
                }
                else
                {
                    // Second half
                    WIN_DOWN = MID_POINT;
                }

            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private TermInfo getTerm(FileChannel channel, long from) throws IOException
    {
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,from * TermInfo.SIZE_POST_MERGING, TermInfo.SIZE_POST_MERGING);

        byte[] termBytes = new byte[TermInfo.SIZE_TERM];
        buffer.get(termBytes);

        String term = new String(termBytes).trim();
        int totFreq = buffer.getInt();
        long docOff = buffer.getLong();
        long freqOff = buffer.getLong();
        long docBytes = buffer.getLong();
        long freqBytes = buffer.getLong();
        int nPost = buffer.getInt();

        return new TermInfo(term, totFreq, docOff, freqOff, docBytes, freqBytes, nPost);
    }

    private List<Posting> getPostingsByTerm(TermInfo toRetrieve)
    {
        if (toRetrieve == null) return null;

        List<Posting> toRet = new ArrayList<>();

        String docsPath = ConfigReader.getDocidPath();
        String freqPath = ConfigReader.getFrequencyPath();
        try
        {
            FileChannel docsChannel = (FileChannel) Files.newByteChannel(Paths.get(docsPath),
                    StandardOpenOption.READ);
            FileChannel freqChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                    StandardOpenOption.READ);

            MappedByteBuffer docBuffer = docsChannel.map(FileChannel.MapMode.READ_ONLY, toRetrieve.getDocidOffset(), toRetrieve.getBytesOccupiedDocid());
            MappedByteBuffer freqBuffer = freqChannel.map(FileChannel.MapMode.READ_ONLY, toRetrieve.getFreqOffset(), toRetrieve.getBytesOccupiedFreq());

            byte[] docBytes = new byte[(int)toRetrieve.getBytesOccupiedDocid()];
            byte[] freqBytes = new byte[(int)toRetrieve.getBytesOccupiedFreq()];

            docBuffer.get(docBytes);
            freqBuffer.get(freqBytes);

            if (!COMPRESSION) {
                for (int i = 0; i < toRetrieve.getNumPosting(); i++) {
                    byte[] tempDocBytes = new byte[4];
                    byte[] tempFreqBytes = new byte[4];
                    System.arraycopy(docBytes, i * 4, tempDocBytes, 0, 4);
                    System.arraycopy(freqBytes, i * 4, tempFreqBytes, 0, 4);

                    toRet.add(new Posting(Commons.bytesToInt(tempDocBytes), Commons.bytesToInt(tempFreqBytes)));
                }
            }
            else
            {
                int[] decompressedDocBytes = Compressor.VariableByteDecompression(docBytes);
                int[] decompressedFreqBytes = Compressor.UnaryDecompression(freqBytes);

                for (int i = 0; i < toRetrieve.getNumPosting(); i++) {
                    toRet.add(new Posting(decompressedDocBytes[i], decompressedFreqBytes[i]));
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return toRet;
    }
}
