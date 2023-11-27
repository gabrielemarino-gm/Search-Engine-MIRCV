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
    private List<Posting> postings = new ArrayList<>();
    private int blockIndexer = 0;


    public PostingListSkippable(TermInfo termInfo)
    {
        this.term = termInfo;
        getFromDisk();
    }

    public String getTerm() { return term.getTerm(); }

    /**
     * Get the blocks descriptors from disk
     */
    private void getFromDisk()
    {
        String blocksPath = ConfigReader.getBlockDescriptorsPath();

        try
                (
                    FileChannel blockChannel = (FileChannel) Files.newByteChannel(Paths.get(blocksPath),
                            StandardOpenOption.READ);
                )
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

            getFromNextBlock();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Get the postings from the next block
     */
    private void getFromNextBlock()
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

                if(ConfigReader.compressionEnabled())
                {
                    byte[] docsBytes = new byte[(int) block.getBytesOccupiedDocid()];
                    byte[] freqBytes = new byte[(int) block.getBytesOccupiedFreq()];

                    docsBuffer.get(docsBytes);
                    freqBuffer.get(freqBytes);

                    int[] docsInts = Compressor.VariableByteDecompression(docsBytes);
                    int[] freqInts = Compressor.UnaryDecompression(freqBytes, block.getNumPostings());

                    for (int i = 0; i < block.getNumPostings(); i++) {
                        postings.add(new Posting(docsInts[i], freqInts[i]));
                    }
                }
                else
                {
                    for (int i = 0; i < block.getNumPostings(); i++) {
                        int docID = docsBuffer.getInt();
                        int freq = freqBuffer.getInt();

                        postings.add(new Posting(docID, freq));
                    }
                }

                blockIndexer++;
            }
            else
            {
                // No more blocks to read
                postings = null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void reset(){ blockIndexer = 0; }

    private Posting current = null;
    public Posting getCurrent()
    {
        if(current == null && hasNext())
            current = next();

        return current;
    }
    public boolean hasNext()
    {
        // Last block
        if(blockIndexer == term.getNumBlocks()) { return !postings.isEmpty(); }
        // Not last block
        else
        {
            // If no more posting in current block
            if(postings.isEmpty())
            {
                // Try retrieve from next block
                getFromNextBlock();
                return hasNext();
            }
            else
            {
                return true;
            }
        }
    }

    public Posting next()
    {

        if(hasNext())
        {
            current = postings.remove(0);
        }
        else
        {
            current = null;
        }
        return current;
    }

    public Posting nextGEQ(int docID)
    {
        if (blockDescriptors.get(blockIndexer - 1).getMaxDocid() < docID)
        {
            postings.clear();
            getFromNextBlock();
        }

        if (hasNext())
        {
            while (getCurrent().getDocId() < docID)
            {
                current = next();
            }
        }
        else
        {
            current = null;
        }

        return current;
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

}
