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
    private TermInfo term;
    private List<BlockDescriptor> blockDescriptors = new ArrayList<>();
    private List<Posting> postingsOfTheCurrentBlock = new ArrayList<>();
    private int blockIndexer = 0;
    boolean noMorePostings = false;

    public PostingListSkippable(TermInfo termInfo)
    {
        this.term = termInfo;
        getBlockDescriptorFromDisk();
    }

    public String getTerm() { return term.getTerm(); }

    /**
     * Get the blocks descriptors from disk
     */
    private void getBlockDescriptorFromDisk()
    {
        String blocksPath = ConfigReader.getBlockDescriptorsPath();

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

            getPostingsFromBlock(false);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Get the postings from the block, and increment the block indexer
     */
    private void getPostingsFromBlock(boolean nextGEQ)
    {
        String docsPath = ConfigReader.getDocidPath();
        String freqPath = ConfigReader.getFrequencyPath();

        try(
                FileChannel docsChannel = (FileChannel) Files.newByteChannel(Paths.get(docsPath),
                        StandardOpenOption.READ);
                FileChannel freqChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                        StandardOpenOption.READ);
        )
        {
            if(blockIndexer < term.getNumBlocks())
            {
                // Get the block descriptor
                BlockDescriptor block = blockDescriptors.get(blockIndexer);

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

                // Increase the block indexer
                if (!nextGEQ)
                    blockIndexer++;
            }
            else
            {
                // No more blocks to read
                postingsOfTheCurrentBlock = null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void reset(){ blockIndexer = 0; }

    private Posting currentPosting = null;
    public Posting getCurrentPosting()
    {
        if(currentPosting == null && hasNext())
            currentPosting = next();

        return currentPosting;
    }
    public boolean hasNext()
    {
        // Last block return true if there are still postings
        if(blockIndexer == term.getNumBlocks())
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
                getPostingsFromBlock(false);
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

    public Posting nextGEQ(int docID)
    {
        int brekPointNextGEQ = 0;
        int prevBlockIndexer = blockIndexer;

        // Find the block that contains the docID
        while (docID > blockDescriptors.get(blockIndexer - 1).getMaxDocid())
        {
            // If the blockIndexer is equal to the number of blocks, there are no more blocks to read
            if (blockIndexer == term.getNumBlocks())
                break;

            blockIndexer++;
            brekPointNextGEQ++;
        }

        // Get the postings from the block, if the blockIndexer has been increased
        if (blockIndexer > prevBlockIndexer)
            getPostingsFromBlock(true);

        // Move the current posting to the first posting with docID >= docID
        while (docID > currentPosting.getDocId())
        {
            currentPosting = next();

            if (currentPosting == null)
                break;

            brekPointNextGEQ++;
        }


        return currentPosting;
    }

    @Override
    public String toString()
    {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("PostingListSkippable{\n")
                .append(term.toString()+"\n")
                .append(blockDescriptors.toString())
                .append(", blockIndexer=" + blockIndexer)
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
