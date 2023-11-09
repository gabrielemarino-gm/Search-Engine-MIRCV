package it.unipi.aide.algorithms;

import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.Posting;
import it.unipi.aide.model.PostingList;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.Commons;

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

    private int k;
    private CollectionInformation ci;
    String WORK_DIR_PATH;

    HashMap<String, TermInfo> terms = new HashMap<>();

    public DAAT(String in_path, int k){
        this.k = k;
        WORK_DIR_PATH = in_path;
        this.ci = new CollectionInformation(WORK_DIR_PATH);
    }

    public List<ScoredDocument> executeDAAT(List<String> queryTerms){
        // Retrieve the posting lists of the query terms
        List<PostingList> postingLists= new ArrayList<>();
        for(String t : queryTerms){
            TermInfo toRetrieve = binarySearch(t);
            terms.put(t, toRetrieve);
            postingLists.add(new PostingList(t, getPostingsByTerm(toRetrieve)));
        }

        // If searched terms are not in the vocabulary, return null
        if(postingLists.isEmpty()) return null;

        List<ScoredDocument> scoredDocuments = new ArrayList<>();
        boolean stop = false;

        while(!stop){
            stop = true;
            int firstDoc = getSmallestDocid(postingLists);
            ScoredDocument toAdd = new ScoredDocument(firstDoc, 0);
            for(PostingList pl : postingLists){
                if (pl.getCurrent() != null) {
                    stop = false;
                    if (pl.getCurrent().getDocId() == firstDoc) {
                        toAdd.setScore(
                                (1 + Math.log(pl.getCurrent().getFrequency())) * Math.log(CollectionInformation.getTotalDocuments() / terms.get(pl.getTerm()).getNumPosting())
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

        return scoredDocuments.subList(0, k);
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
                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(WORK_DIR_PATH + "vocabularyBlock"),
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
        long off = buffer.getLong();
        long docBytes = buffer.getLong();
        long freqBytes = buffer.getLong();
        int nPost = buffer.getInt();

        return new TermInfo(term, totFreq, off, docBytes, freqBytes, nPost);
    }

    private List<Posting> getPostingsByTerm(TermInfo toRetrieve)
    {
        if (toRetrieve == null) return null;

        List<Posting> toRet = new ArrayList<>();

        String docsPath = WORK_DIR_PATH + "docIDsBlock";
        String freqPath = WORK_DIR_PATH + "frequenciesBlock";
        try
        {
            FileChannel docsChannel = (FileChannel) Files.newByteChannel(Paths.get(docsPath),
                    StandardOpenOption.READ);
            FileChannel freqChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                    StandardOpenOption.READ);

            long toRead = 4L * toRetrieve.getNumPosting();

            MappedByteBuffer docBuffer = docsChannel.map(FileChannel.MapMode.READ_ONLY, toRetrieve.getOffset(), toRead);
            MappedByteBuffer freqBuffer = freqChannel.map(FileChannel.MapMode.READ_ONLY, toRetrieve.getOffset(), toRead);
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
