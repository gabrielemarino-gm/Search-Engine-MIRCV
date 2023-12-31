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

    private static final String blocksPath = ConfigReader.getBlockDescriptorsPath();
    private static final String docsPath = ConfigReader.getDocidPath();
    private static final String freqPath = ConfigReader.getFrequencyPath();


    private final TermInfo term;
    private final List<BlockDescriptor> blockDescriptors = new ArrayList<>();
    private final List<Posting> postingsOfTheCurrentBlock = new ArrayList<>();
    private int currentBlockIndexer = -1;

    private FileChannel docsChannel;
    private FileChannel freqChannel;

    public PostingListSkippable(TermInfo termInfo)
    {
        this.term = termInfo;
        getBlockDescriptorsFromDisk();
        openChannels();
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
        try
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
                if(ConfigReader.isCompressionEnabled())
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public PostingListSkippable reset(){
        currentBlockIndexer = -1;
        postingsOfTheCurrentBlock.clear();
        currentPosting = null;
        openChannels();
        return this;
    }

    /*
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
        // If its empty and can still retrieve from the disk
        if (currentBlockIndexer < term.getNumBlocks() &&
                postingsOfTheCurrentBlock.isEmpty())
        {
                // Try retrieve from next block
                currentBlockIndexer++;
                getPostingsFromBlock();
        }
        // Always return status of the list
        return !postingsOfTheCurrentBlock.isEmpty();
    }

    /**
     * Update the current posting with the next
     * @return the next posting
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
            closeChannels();
        }

        return currentPosting;
    }
    
    /*
     * --------------------
     * END ITERABLE SECTION
     * --------------------
     */

    /**
     * Get the next posting with docID >= docID
     * @param docID docID to search
     * @return Posting with docID >= docID
     */
    public Posting nextGEQ(int docID)
    {
        if(!hasNext())
            return currentPosting = null;

        int prevBlockIndexer = currentBlockIndexer;

        // Find the block that may contain the docID
        while (currentBlockIndexer < (term.getNumBlocks()  - 1) &&
                docID > blockDescriptors.get(currentBlockIndexer).getMaxDocid())
        {
           currentBlockIndexer++;
        }

        // Get the postings from the block, if the blockIndexer has been increased
        if (currentBlockIndexer > prevBlockIndexer)
        {
            getPostingsFromBlock();
            // currentPosting = next();
        }

        // No more blocks, docID doesn't exist in this posting list
        if (currentBlockIndexer == term.getNumBlocks())
            return null;

        // While current posting has docID less than the one need for
        while(hasNext() && currentPosting.getDocId() < docID)
        {
            currentPosting = next();
        }
        return currentPosting;
    }

    @Override
    public String toString()
    {
        String toReturn = "PostingListSkippable{\n" +
                term.toString() + "\n" +
//                blockDescriptors +
                ", blockIndexer=" + currentBlockIndexer +
                "\n}";

        return toReturn;
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

    private void openChannels()
    {
        closeChannels(); // PATCH: avoid too many open files
        try
        {
            docsChannel = (FileChannel) Files.newByteChannel(Paths.get(docsPath),
                    StandardOpenOption.READ);
            freqChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                    StandardOpenOption.READ);
        }
        catch (IOException io)
        {
            io.printStackTrace();
        }
    }

    public void closeChannels()
    {
        try
        {
            if(docsChannel != null && docsChannel.isOpen()) {
                docsChannel.close();
                docsChannel = null;
            }
            if(freqChannel != null && freqChannel.isOpen()) {
                freqChannel.close();
                freqChannel = null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public TermInfo getTermInfo()
    {
        return term;
    }
}
