package it.unipi.aide.algorithms;

import it.unipi.aide.model.BlockDescriptor;
import it.unipi.aide.model.CollectionInformation;
import it.unipi.aide.model.TermInfo;
import it.unipi.aide.utils.ScoreFunction;
import it.unipi.aide.utils.Commons;
import it.unipi.aide.utils.Compressor;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import me.tongfei.progressbar.ProgressBar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.unipi.aide.utils.ColorText.*;

public class Merging
{
    private boolean COMPRESSION = false;
    private final int BLOCKS_COUNT; // Number of blocks to merge
    private boolean DEBUG = false;
    long finalDocidOffset = 0;
    long finalFreqOffset = 0;
    long blockDescriptorOffset = 0;
    long vFinalOffset = 0;

    /**
     * --------------------------------------------------------------------------
     *
     * @param compression
     * @param blocksCount
     * @param debug
     * --------------------------------------------------------------------------
     */
    public Merging(boolean compression, int blocksCount, boolean debug)
    {
        this.BLOCKS_COUNT = blocksCount;
        this.COMPRESSION = compression;
        this.DEBUG = debug;
    }

    /**
     * --------------------------------------------------------------------------
     * Merge partial blocks into one unique block
     * --------------------------------------------------------------------------
     */
    public void mergeBlocks()
    {
        ProgressBar pb = new ProgressBar(BLUE + "MERGING > " + ANSI_RESET, 912028);
        pb.start();
        long nTerms = 0;

        // Check if the directory with the blocks results exists
        if(FileManager.checkDir(ConfigReader.getPartialPath()))
        {
            // Create FileChannels for each block for docId, frequencies and vocabulary fragments
            FileChannel[] docIdFileChannel = new FileChannel[BLOCKS_COUNT];
            FileChannel[] frequenciesFileChannel = new FileChannel[BLOCKS_COUNT];
            FileChannel[] vocabulariesFileChannel = new FileChannel[BLOCKS_COUNT];

            // Create one offset for each block, both for docId, frequencies and vocabulary fragments
            long[] offsetDocId = new long[BLOCKS_COUNT];
            long[] offsetFrequency = new long[BLOCKS_COUNT];
            long[] offsetVocabulary = new long[BLOCKS_COUNT];

            long[] dimVocabularyFile = new long[BLOCKS_COUNT];
            TermInfo[] vocs = new TermInfo[BLOCKS_COUNT];

            String FdocPath = ConfigReader.getDocidPath();
            String FfreqPath = ConfigReader.getFrequencyPath();
            String FvocPath = ConfigReader.getVocabularyPath();
            String FblockPath = ConfigReader.getBlockDescriptorsPath();

            if(!FileManager.checkDir(FdocPath)) FileManager.createFile(FdocPath);
            if(!FileManager.checkDir(FfreqPath)) FileManager.createFile(FfreqPath);
            if(!FileManager.checkDir(FvocPath)) FileManager.createFile(FvocPath);
            if(!FileManager.checkDir(FblockPath)) FileManager.createFile(FblockPath);

            long nDoc = CollectionInformation.getTotalDocuments();

            try (
                    // Open FileChannels to each file (Vocabulary, DocID, Frequencies, BlockDescriptors)
                    FileChannel finalVocChannel = (FileChannel) Files.newByteChannel(Paths.get(FvocPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    FileChannel finalDocIDChannel = (FileChannel) Files.newByteChannel(Paths.get(FdocPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    FileChannel finalFreqChannel = (FileChannel) Files.newByteChannel(Paths.get(FfreqPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    FileChannel blockDescriptorsChannel = (FileChannel) Files.newByteChannel(Paths.get(FblockPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)
                    )
            {
                // INIT PHASE
                for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++) // For each block...
                {
                    // Get the path for the 3 files relatively to all the blocks
                    String partialVocPath = ConfigReader.getPartialVocabularyPath() + indexBlock;
                    String partialDocPath = ConfigReader.getPartialDocsPath() + indexBlock;
                    String partialFreqPath = ConfigReader.getPartialFrequenciesPath() + indexBlock;

                    // Open FileChannels to each file
                    docIdFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(partialDocPath),
                            StandardOpenOption.READ);
                    frequenciesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(partialFreqPath),
                            StandardOpenOption.READ);
                    vocabulariesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(partialVocPath),
                            StandardOpenOption.READ);

                    // Store in this list at position 'indexBlock', relative to the block with that index,
                    // the size of the vocabulary correspondent to that block.
                    dimVocabularyFile[indexBlock] = vocabulariesFileChannel[indexBlock].size();

                    // Get first term for each block's vocabulary
                    vocs[indexBlock] = getTermFromVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
                    offsetVocabulary[indexBlock] += TermInfo.SIZE_PRE_MERGING;
                }

                // Lists to accumulate bytes from different blocks
                List<byte[]> docsAcc = new ArrayList<>();
                List<byte[]> freqAcc = new ArrayList<>();

                // Until we have data to analyze inside blocks...
                while(true)
                {
                    // ...get the smallest term along all vocabularies...
                    String smallestTerm = getSmallestTerm(vocs);

                    // ...create a TermInfo to merge those in different blocks...
                    TermInfo finalTerm = new TermInfo(smallestTerm);

                    int finalTotalFreq = 0;
                    int totalTermPostings = 0;

                    // ...clear accumulator arrays...
                    docsAcc.clear();
                    freqAcc.clear();

                    // For each block...
                    for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                    {
                        // ...if current term is equal the smallest (and it's not null)...
                        if(vocs[indexBlock] != null && vocs[indexBlock].getTerm().equals(smallestTerm))
                        {
                            // ...accumulates bytes from different blocks, for both docId and frequencies...
                            docsAcc.add(extractBytes(docIdFileChannel[indexBlock],
                                    offsetDocId[indexBlock],
                                    vocs[indexBlock].getNumPosting()));

                            freqAcc.add(extractBytes(frequenciesFileChannel[indexBlock],
                                    offsetFrequency[indexBlock],
                                    vocs[indexBlock].getNumPosting()));

                            // finalPostings and totalFrequency are just the sum of partial blocks
                            totalTermPostings += vocs[indexBlock].getNumPosting();
                            finalTotalFreq += vocs[indexBlock].getTotalFrequency();

                            // Update MaxTF for current term. The method getMaxTF() checks if the current term has the maximum TF
                            finalTerm.setMaxTF(vocs[indexBlock].getMaxTF());
                            finalTerm.setMaxBM25(vocs[indexBlock].getBM25TF(), vocs[indexBlock].getBM25DL());

                            // Update the offsets for current block
                            offsetDocId[indexBlock] += 4L * vocs[indexBlock].getNumPosting();
                            offsetFrequency[indexBlock] += 4L * vocs[indexBlock].getNumPosting();

                            // If this block is finished, set its vocs to null and skip.
                            // This happens because last time we extracted a term for this
                            // block, it was the last term in the list.
                            // Null is used as break condition.
                            if (offsetVocabulary[indexBlock] >= dimVocabularyFile[indexBlock])
                            {
                                // System.err.println("MERGING > Block #" + indexBlock + " exhausted.");
                                vocs[indexBlock] = null;
                                continue;
                            }

                            // Vocabulary shift: we are going to read the next term
                            vocs[indexBlock] = getTermFromVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
                            offsetVocabulary[indexBlock] += TermInfo.SIZE_PRE_MERGING;
                        }
                    }

                    // ... current term is ended, bytes are accumulated ...
                    int totalBytesSummed = docsAcc.stream().mapToInt(vec -> vec.length).sum();

                    byte[] concatenatedDocsBytes = new byte[totalBytesSummed];
                    byte[] concatenatedFreqBytes = new byte[totalBytesSummed];

                    // ... and now we can concatenate them ...
                    int concatenationOffset = 0;
                    for(byte[] v: docsAcc)
                    {
                        System.arraycopy(v,0, concatenatedDocsBytes, concatenationOffset, v.length);
                        concatenationOffset += v.length;
                    }
                    concatenationOffset = 0;
                    for(byte[] v: freqAcc)
                    {
                        System.arraycopy(v,0, concatenatedFreqBytes, concatenationOffset, v.length);
                        concatenationOffset += v.length;
                    }

                    /* Now that we cumulated docids and frequencies for that term, split them in Blocks */

                    // ... write sqrt(n) postings in each block ...
                    int postingsInsideEachBlock = (int) Math.sqrt(totalTermPostings);
                    // ... compute the number of blocks to create ...
                    int numberOfBlocksToCreate = (int) Math.ceil((double) totalTermPostings / postingsInsideEachBlock);

                    // ... divide the accumulated bytes in sqrt(n) blocks ...
                    List<byte[]> docidBlocks = new ArrayList<>();
                    List<byte[]> freqBlocks = new ArrayList<>();

                    List<BlockDescriptor> blockDescriptors = new ArrayList<>();

                    // ... if totalTerms is less than 512, just write one block ...
                    if (totalTermPostings <= ConfigReader.getCompressionBlockSize()
                        ||
                        !ConfigReader.blockDivisionEnabled())
                    {
                        BlockDescriptor blockDescriptor = new BlockDescriptor();
                        blockDescriptor.setNumPostings(totalTermPostings);
                        blockDescriptor.setMaxDocID(getMaxDocid(concatenatedDocsBytes));

                        // Manage compression
                        if (COMPRESSION)
                        {
                            byte[] tempDoc = Compressor.VariableByteCompression(concatenatedDocsBytes);
                            byte[] tempFreq = Compressor.UnaryCompression(concatenatedFreqBytes);

                            // Update blockDescriptor (Compression uses fewer bytes)
                            blockDescriptor.setBytesOccupiedDocid(tempDoc.length);
                            blockDescriptor.setBytesOccupiedFreq(tempFreq.length);
                            docidBlocks.add(tempDoc);
                            freqBlocks.add(tempFreq);
                        }
                        else
                        {
                            // Update blockDescriptor
                            blockDescriptor.setBytesOccupiedDocid(concatenatedDocsBytes.length);
                            blockDescriptor.setBytesOccupiedFreq(concatenatedFreqBytes.length);
                            docidBlocks.add(concatenatedDocsBytes);
                            freqBlocks.add(concatenatedFreqBytes);
                        }

                        // Update MaxDocID for current block and add to the list

                        blockDescriptors.add(blockDescriptor);
                        finalTerm.setNumBlocks(1);
                    }

                    // ... otherwise, divide the block in sqrt(n) blocks ...
                    else
                    {
                    /* From there we can use postingsInsideEachBlock as the number of postings in each block
                     * and numberOfBlocksToCreate as the number of blocks to create
                     */

                        // ... for each block ...
                        for (int indxCurrentBlock = 0; indxCurrentBlock < numberOfBlocksToCreate; indxCurrentBlock++)
                        {
                            // Last block may contain fewer elements than blockSize
                            int postingsInCurrentBlock = Math.min(
                                    postingsInsideEachBlock,
                                    totalTermPostings - indxCurrentBlock * postingsInsideEachBlock
                            );

                            // Block descriptor for current block
                            BlockDescriptor blockDescriptor = new BlockDescriptor();
                            blockDescriptor.setNumPostings(postingsInCurrentBlock);

                            byte[] tempDocsBlock = new byte[postingsInCurrentBlock * 4];
                            byte[] tempFreqBlock = new byte[postingsInCurrentBlock * 4];

                            // Get the bytes for the current block from the accumulated bytes
                            System.arraycopy(
                                    concatenatedDocsBytes, // Source
                                    indxCurrentBlock * postingsInsideEachBlock * 4, // Source offset
                                    tempDocsBlock, // Destination
                                    0, // Destination offset
                                    postingsInCurrentBlock * 4 // Length
                            );

                            System.arraycopy(concatenatedFreqBytes, // Source
                                    indxCurrentBlock * postingsInsideEachBlock * 4, // Source offset
                                    tempFreqBlock, // Destination
                                    0, // Destination offset
                                    postingsInCurrentBlock * 4 // Length
                            );

                            blockDescriptor.setMaxDocID(getMaxDocid(tempDocsBlock));

                            if(COMPRESSION)
                            {
                                tempDocsBlock = Compressor.VariableByteCompression(tempDocsBlock);
                                tempFreqBlock = Compressor.UnaryCompression(tempFreqBlock);
                            }

                            // Update bytes occupied in that block
                            blockDescriptor.setBytesOccupiedDocid(tempDocsBlock.length);
                            blockDescriptor.setBytesOccupiedFreq(tempFreqBlock.length);

                            // Add to the list of blocks
                            docidBlocks.add(tempDocsBlock);
                            freqBlocks.add(tempFreqBlock);

                            // Update current blockDescriptor and add to the list
                            blockDescriptors.add(blockDescriptor);

                            finalTerm.setNumBlocks(numberOfBlocksToCreate);
                        }
                    }

                    // ... update the final term ... (Not dependent by compression or blocks splitting)
                    finalTerm.setNumPosting(totalTermPostings);
                    finalTerm.setTotalFrequency(finalTotalFreq);


                    // Write everything as separated blocks
                    writeBlocks(finalDocIDChannel, finalFreqChannel, blockDescriptorsChannel,
                            docidBlocks, freqBlocks, blockDescriptors,
                            finalTerm);

                    // First offset of first term Block is updated inside the previous function
                    writeTermToDisk(finalVocChannel, finalTerm);

                    // ... print in txt format for debug ...
                    printIndexDebugInTXT(concatenatedDocsBytes, concatenatedFreqBytes, smallestTerm);

                    nTerms++;

                    if(nTerms % 100 == 0) {
                        pb.stepBy(100);
                    }

                    /*
                        STOPPING CONDITION
                         if all the entry of vocs are null, we have finished
                         as we exhausted all blocks
                     */
                    boolean allNull = true;
                    for(TermInfo t: vocs)
                    {
                        // If at least one is not null, just continue
                        if(t != null)
                        {
                            allNull = false;
                            break;
                        }
                    }

                    if(allNull)
                        break;

                }

                pb.stepTo(912028);
                pb.stop();

                // Delete temporary blocks
//              FileManager.deleteDir(ConfigReader.getPartialPath());
                System.out.printf(BLUE + "MERGING > Total terms in the Lexicon is %d%n" + ANSI_RESET, nTerms);
                CollectionInformation.setTotalTerms(nTerms);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            pb.stop();
            System.out.println(RED + "MERGING ERROR > Merge error, directory " + ConfigReader.getPartialPath() + " doesn't exists!" + ANSI_RESET);
        }
    }

    /**
     * --------------------------------------------------------------------------
     * Print the current term in a txt file
     * @param concatenatedDocsBytes Bytes of docIDs
     * @param concatenatedFreqBytes Bytes of frequencies
     * @param term Term to print
     * --------------------------------------------------------------------------
     */
    private void printIndexDebugInTXT(byte[] concatenatedDocsBytes, byte[] concatenatedFreqBytes, String term)
    {
        if(DEBUG)
        {
            try (BufferedWriter writerIdex = new BufferedWriter(new FileWriter(ConfigReader.getDebugDir() + "invertedIndex.txt", true)))
            {
                // Write term in the index file txt
                writerIdex.write(String.format("%s: ", term));

                // For each integer in the list
                for (int i = 0; i < concatenatedDocsBytes.length; i += 4)
                {
                    int docid = Commons.bytesToInt(Arrays.copyOfRange(concatenatedDocsBytes, i, i + 4));
                    int freq = Commons.bytesToInt(Arrays.copyOfRange(concatenatedFreqBytes, i, i + 4));

                    // Write it in the file txt
                    writerIdex.write(String.format("(%d, %d) ", docid, freq));
                }
                writerIdex.newLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    void printVocabularyDebugInTXT(TermInfo finalTerm)
    {
        if(DEBUG)
        {
            try (BufferedWriter writerVocs = new BufferedWriter(new FileWriter(ConfigReader.getDebugDir() + "vocabulary.txt", true)))
            {
                // Write Vocabulary term
                writerVocs.write(String.format("%s: ", finalTerm.toString()));
                writerVocs.newLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * --------------------------------------------------------------------------
     * Write the final docID, frequencies and block descriptors on the disk
     * @param finalDocIDChannel FileChannel for docID
     * @param finalFreqChannel FileChannel for frequencies
     * @param blockDescriptorsChannel FileChannel for block descriptors
     * @param docsBlocks List of docID blocks
     * @param freqBlocks List of frequency blocks
     * @param blockDescriptors List of block descriptors
     * @param finalTerm TermInfo to update
     * --------------------------------------------------------------------------
     */
    private void writeBlocks(FileChannel finalDocIDChannel,
                             FileChannel finalFreqChannel,
                             FileChannel blockDescriptorsChannel,
                             List<byte[]> docsBlocks,
                             List<byte[]> freqBlocks,
                             List<BlockDescriptor> blockDescriptors,
                             TermInfo finalTerm)
    {
        // Write first blockDescriptor offset for that term
        finalTerm.setOffset(blockDescriptorOffset);

        try
        {
            // ... for each block ...
            for(int i = 0; i < blockDescriptors.size(); i++)
            {
                MappedByteBuffer tempBuffer = finalDocIDChannel.map(FileChannel.MapMode.READ_WRITE,
                        finalDocidOffset, blockDescriptors.get(i).getBytesOccupiedDocid());
                tempBuffer.put(docsBlocks.get(i));
                blockDescriptors.get(i).setOffsetDocID(finalDocidOffset);

                finalDocidOffset += blockDescriptors.get(i).getBytesOccupiedDocid();

                // Write block frequencies
                tempBuffer = finalFreqChannel.map(FileChannel.MapMode.READ_WRITE,
                        finalFreqOffset, blockDescriptors.get(i).getBytesOccupiedFreq());
                tempBuffer.put(freqBlocks.get(i));

                blockDescriptors.get(i).setOffsetFreq(finalFreqOffset);

                finalFreqOffset += blockDescriptors.get(i).getBytesOccupiedFreq();

                // Write block descriptors
                tempBuffer = blockDescriptorsChannel.map(FileChannel.MapMode.READ_WRITE,
                        blockDescriptorOffset, BlockDescriptor.BLOCK_SIZE);

                tempBuffer.putInt(blockDescriptors.get(i).getMaxDocid());
                tempBuffer.putInt(blockDescriptors.get(i).getNumPostings());
                tempBuffer.putLong(blockDescriptors.get(i).getOffsetDocid());
                tempBuffer.putLong(blockDescriptors.get(i).getOffsetFreq());
                tempBuffer.putLong(blockDescriptors.get(i).getBytesOccupiedDocid());
                tempBuffer.putLong(blockDescriptors.get(i).getBytesOccupiedFreq());

                blockDescriptorOffset += BlockDescriptor.BLOCK_SIZE;
            }
        }
        catch (IOException io)
        {
            io.printStackTrace();
        }
    }

    /**
     * --------------------------------------------------------------------------
     * Write a TermInfo on the disk, into the final vocabulary file
     * @param finalVocChannel Vocabulary FileChannel
     * @param finalTerm Term to write
     * @throws IOException
     * --------------------------------------------------------------------------
     */
    private void writeTermToDisk(FileChannel finalVocChannel, TermInfo finalTerm) throws IOException
    {
        MappedByteBuffer tempBuffer = finalVocChannel.map(FileChannel.MapMode.READ_WRITE, vFinalOffset, TermInfo.SIZE_POST_MERGING);

        String paddedTerm = String.format("%-" + TermInfo.SIZE_TERM + "s", finalTerm.getTerm()).substring(0, TermInfo.SIZE_TERM); // Pad with spaces up to 64 characters

        // Write
        tempBuffer.put(paddedTerm.getBytes());                      // 46
        tempBuffer.putInt(finalTerm.getTotalFrequency());           // 4
        tempBuffer.putInt(finalTerm.getNumPosting());               // 4
        tempBuffer.putInt(finalTerm.getNumBlocks());                // 4
        tempBuffer.putLong(finalTerm.getOffset());                  // 8

        // Evaluate term upper bound fof TFIDF and BM25
        float tfidf = ScoreFunction.computeTFIDF(finalTerm.getMaxTF(),finalTerm.getNumPosting());
        tempBuffer.putFloat(tfidf);   // 4
        float bm25 = ScoreFunction.computeBM25(finalTerm.getBM25TF(), finalTerm.getNumPosting(), finalTerm.getBM25DL());
        tempBuffer.putFloat(bm25);    // 4

        vFinalOffset += TermInfo.SIZE_POST_MERGING;

        if (DEBUG)
            printVocabularyDebugInTXT(finalTerm);
    }

    /**
     * --------------------------------------------------------------------------
     * First term in lexicographic order between all terms
     * @param vocs TermInfo array to pick the terms from
     * @return Smallest term in lexicographic order
     * --------------------------------------------------------------------------
     */
    private String getSmallestTerm(TermInfo[] vocs)
    {
        String toRet = null;
        for(TermInfo t: vocs)
        {
            if(t == null) continue;
            if(toRet == null) toRet = t.getTerm();
            if(t.getTerm().compareTo(toRet) < 0) toRet = t.getTerm();
        }
        return toRet;
    }

    /**
     * --------------------------------------------------------------------------
     * Get next TermInfo from that channel
     * @param fileChannel FileChannel to retrieve the Term from
     * @param offsetVocabulary Offset at which the term is
     * @return Next TermInfo in line
     * @throws IOException
     * --------------------------------------------------------------------------
     */
    private TermInfo getTermFromVoc(FileChannel fileChannel, long offsetVocabulary) throws IOException
    {
        // vocs[indexBlock] = getNextVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
        MappedByteBuffer tempBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, offsetVocabulary, TermInfo.SIZE_PRE_MERGING);

        byte[] termBytes = new byte[TermInfo.SIZE_TERM];
        tempBuffer.get(termBytes);

        int frequency = tempBuffer.getInt();
        int nPosting = tempBuffer.getInt();
        long offset = tempBuffer.getLong();

        int maxTF = tempBuffer.getInt();
        int BM25TF = tempBuffer.getInt();
        int BM25DL = tempBuffer.getInt();

        String term = new String(termBytes).trim();

        return new TermInfo(term, frequency, nPosting, offset, maxTF, BM25TF, BM25DL);
    }
    /**
     * --------------------------------------------------------------------------
     * Extract bytes from given channel at given offset and returns them
     *
     * @param fromChannel File Channel to extract Bytes from
     * @param offset At which offset to extract bytes
     * @param nPosting How many 4-bytes to extract
     * @return Array of Bytes extracted
     * --------------------------------------------------------------------------
     */
    private byte[] extractBytes(FileChannel fromChannel, long offset, int nPosting) throws IOException
    {
        // Buffer to extract bytes from
        MappedByteBuffer tempBuff = fromChannel.map(FileChannel.MapMode.READ_ONLY,
                offset, 4L*nPosting);
        // Where to place those bytes
        byte[] tempBytes = new byte[4 * nPosting];

        tempBuff.get(tempBytes);
        return tempBytes;
    }

    /**
     * --------------------------------------------------------------------------
     *
     * @param list
     * @return
     * --------------------------------------------------------------------------
     */
    private int getMaxDocid(byte[] list)
    {
        int max = 0;
        int[] ints = Commons.bytesToIntArray(list);
        for(Integer i: ints)
        {
            if(i > max) max = i;
        }
        return max;
    }
}

/*
 * La seguente classe si occupa di riunire i blocchi parziali creati dall'algoritmo SPIMI.
 *  Tale algoritmo sfrutta il fatto che i document ID di un blocco i, non possono essere maggiori
 *  di un blocco i+1, il merge consiste di unire le posting list dei blocchi con lo stesso termine,
 *  semplicemente accodandole, dai blocchi piu piccoli a quelli piu grandi
 *
 * In particolare: all'inizio vengono caricati tutti i primi termini dei vocabolari parziali di ogni blocco
 *  Tra questi viene estratto il termine lessicograficamente piu piccolo, e viene usato per accodare le Liste:
 *  se un vocabolario non contiene quel termine, allora esso ha un termine maggiore e puo essere semplicemente skippato,
 *  se invece contiene quel termine, viene estratta la posting list parziale, e accodata al file dei posting finale.
 *  Inoltre, tale vocabolario e' incrementato al termine successivo: se non esiste, sara' settato a null.
 *
 * Si continua cosi finche tutti i vocabolari parziali sono null, ossia quando abbiamo finito tutti i termini in tutti i vocabolari.
 *  Durante il merging vengono aggiornati anche i termini finali, sommando frequenze totali  e numero di posting delle liste parziali.
 *
 * L'algoritmo presenta una leggera differenza se si decide di utilizzare la Compressione:
 *  Senza compressione, una posting list occupera' 4byte per ogni elemento della lista
 *  ie. Se una lista ha 5 Posting, tale lista occupera' 5*4=20byte
 *  Con compressione, le liste parziali sono accumulate in un vettore di byte.
 *  Alla fine di ogni termine, tale vettore conterra' sempre 4*numero di Posting per quel termine
 *  ie. Esattamente come prima, avro un vettore di 20byte
 *  A questo punto pero, la compressione ha la possibilita di generare un vettore con meno di 20byte, occupando meno spazio
 *
 * Si noti che effettuare tale operazione, aumenta sensibilmente il tempo di esecuzione dell'algoritmo
 *
 * In entrambi i casi sono aggiornati il numero di byte occupati dai DocId e Frequenze
 *
 * Alla fine dell'algoritmo, viene aggiornato il valore globale di collectionStatistics:
 *  Total (unique) Terms nella collezione
 *
 * Non viene fatto uso della classe Vocabolario, in quanto si assume che i termini siano tutti ordinati lessicograficamente
 *  nei vari blocchi, e potento aggiornare le informazioni di ogni termine on-the-fly, non e' necessario mantenere tali
 *  strutture in memoria
 */

