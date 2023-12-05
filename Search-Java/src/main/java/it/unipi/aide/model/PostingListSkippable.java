package it.unipi.aide.model;

import it.unipi.aide.utils.Compressor;
import it.unipi.aide.utils.ConfigReader;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PostingListSkippable  implements Iterator<Posting>
{

    private static String blocksPath = ConfigReader.getBlockDescriptorsPath();
    private static String docsPath = ConfigReader.getDocidPath();
    private static String freqPath = ConfigReader.getFrequencyPath();



    private TermInfo term;
    private List<BlockDescriptor> blockDescriptors = new ArrayList<>();
    private List<Posting> postingsOfTheCurrentBlock = new ArrayList<>();
    private int currentBlockIndexer = 0;
    boolean noMorePostings = false;

    public PostingListSkippable(TermInfo termInfo)
    {
        this.term = termInfo;
        getBlockDescriptorsFromDisk();
    }

    public String getTerm() { return term.getTerm(); }

    /**
     * Get the blocks descriptors from disk
     */
    private void getBlockDescriptorsFromDisk()
    {
        try (FileChannel blockChannel = (FileChannel) Files.newByteChannel(Paths.get(blocksPath),
                StandardOpenOption.READ))
        {
            // Get all the block descriptors
            MappedByteBuffer blockBuffer = blockChannel.map(FileChannel.MapMode.READ_ONLY,
                    term.getOffset(),
                    BlockDescriptor.BLOCK_SIZE * term.getNumBlocks());

            for(int i = 0; i < term.getNumBlocks(); i++)
            {
                BlockDescriptor block = new BlockDescriptor();

                block.setMaxDocID(blockBuffer.getInt());
                block.setNumPostings(blockBuffer.getInt());
                block.setOffsetDocID(blockBuffer.getLong());
                block.setOffsetFreq(blockBuffer.getLong());
                block.setBytesOccupiedDocid(blockBuffer.getLong());
                block.setBytesOccupiedFreq(blockBuffer.getLong());

                blockDescriptors.add(block);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Get the postings from the block, and increment the block indexer
     */
    private void getPostingsFromBlock()
    {
        try(
                FileChannel docsChannel = (FileChannel) Files.newByteChannel(Paths.get(docsPath),
                        StandardOpenOption.READ);
                FileChannel freqChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                        StandardOpenOption.READ);
        )
        {
            // clear the current posting list
            postingsOfTheCurrentBlock.clear();

            if(currentBlockIndexer < term.getNumBlocks())
            {
                // Get the block descriptor
                BlockDescriptor block = blockDescriptors.get(currentBlockIndexer);

                // Get the postings
                MappedByteBuffer docsBuffer = docsChannel.map(FileChannel.MapMode.READ_ONLY,
                        block.getOffsetDocid(),
                        block.getBytesOccupiedDocid());
                MappedByteBuffer freqBuffer = freqChannel.map(FileChannel.MapMode.READ_ONLY,
                        block.getOffsetFreq(),
                        block.getBytesOccupiedFreq());

                // Using compression
                if(ConfigReader.compressionEnabled())
                {
                    byte[] docsBytes = new byte[(int) block.getBytesOccupiedDocid()];
                    byte[] freqBytes = new byte[(int) block.getBytesOccupiedFreq()];

                    docsBuffer.get(docsBytes);
                    freqBuffer.get(freqBytes);

                    int[] docsInts = Compressor.VariableByteDecompression(docsBytes);
                    int[] freqInts = Compressor.UnaryDecompression(freqBytes, block.getNumPostings());

                    for (int i = 0; i < block.getNumPostings(); i++) {
                        postingsOfTheCurrentBlock.add(new Posting(docsInts[i], freqInts[i]));
                    }
                }

                // Without compression
                else
                {
                    for (int i = 0; i < block.getNumPostings(); i++)
                    {
                        int docID = docsBuffer.getInt();
                        int freq = freqBuffer.getInt();

                        postingsOfTheCurrentBlock.add(new Posting(docID, freq));
                    }
                }
            }
            else
            {
                // No more blocks to read
                postingsOfTheCurrentBlock = new ArrayList<>();

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void reset(){
        currentBlockIndexer = 0;
        postingsOfTheCurrentBlock.clear();
    }

    /**
     * ----------------
     * ITERABLE SECTION
     * ----------------
     */
    private Posting currentPosting = null;
    public Posting getCurrentPosting()
    {
        if(currentPosting == null && hasNext())
            currentPosting = next();

        return currentPosting;
    }
    public boolean hasNext()
    {
        // Last block
        if(currentBlockIndexer == term.getNumBlocks() - 1)
        {
            return !postingsOfTheCurrentBlock.isEmpty();
        }

        // Not last block
        else
        {
            // If no more posting in current block
            if(postingsOfTheCurrentBlock.isEmpty())
            {
                // Try retrieve from next block
                currentBlockIndexer++;
                getPostingsFromBlock();
                return hasNext();
            }
            else
            {
                return true;
            }
        }
    }

    /**
     * Update the current posting with the next
     * @return
     */
    public Posting next()
    {
        // if there is a next posting, keep it as currentPosting
        if(hasNext())
        {
            // remove the first posting from the list, and set it as currentPosting
            currentPosting = postingsOfTheCurrentBlock.remove(0);
        }
        // else, set currentPosting to null
        else
        {
            currentPosting = null;
        }

        return currentPosting;
    }
    
    /**
     * --------------------
     * END ITERABLE SECTION
     * --------------------
     */

    public Posting nextGEQ(int docID)
    {
        int prevBlockIndexer = currentBlockIndexer;

        // Find the block that contains the docID
        while (currentBlockIndexer < term.getNumBlocks() && docID > blockDescriptors.get(currentBlockIndexer).getMaxDocid())
        {
           currentBlockIndexer++;
        }

        // No more blocks, docID doesn't exist in this posting list
        if (currentBlockIndexer == term.getNumBlocks())
            return null;

        if (docID == 8165088 && term.getTerm().equals("will"))
            System.out.println("DEBUG");

        // Get the postings from the block, if the blockIndexer has been increased
        if (currentBlockIndexer > prevBlockIndexer)
            getPostingsFromBlock();


        do
        {
            currentPosting = next();
        }
        while(hasNext() && currentPosting.getDocId() < docID);

//        // Move the current posting to the first posting with docID >= docID
//        if (currentPosting != null)
//        {
//            while (docID > currentPosting.getDocId())
//            {
//                currentPosting = next();
//
//                if (currentPosting == null)
//                    break;
//
//                brekPointNextGEQ++;
//            }
//        }

        return currentPosting;
    }

    @Override
    public String toString()
    {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("PostingListSkippable{\n")
                .append(term.toString()+"\n")
                .append(blockDescriptors.toString())
                .append(", blockIndexer=" + currentBlockIndexer)
                .append("\n}");

        return toReturn.toString();
    }

    public static Comparator<PostingListSkippable> compareToTFIDF() {
        return Comparator.comparing(PostingListSkippable::getTermUpperBoundTFIDF);
    }

    public float getTermUpperBoundTFIDF()
    {
        return term.getTermUpperBoundTFIDF();
    }

    public static Comparator<PostingListSkippable> compareToBM25() {
        return Comparator.comparing(PostingListSkippable::getTermUpperBoundBM25);
    }

    public float getTermUpperBoundBM25()
    {
        return term.getTermUpperBoundBM25();
    }

    public void printPostingList()
    {
        System.out.print("[" + term.getTerm() + "] ");
        for (Posting p : postingsOfTheCurrentBlock)
        {
            System.out.print(p);
        }
    }
    public boolean isNoMorePostings()
    {
        return noMorePostings;
    }
}
