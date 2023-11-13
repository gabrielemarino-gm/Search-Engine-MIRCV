package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.Commons;
import it.unipi.aide.utils.Compressor;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Merging
{
    private final int MIN_POSTING_LIMIT = 128; // Minimum number of postings to split in blocks
    private final boolean COMPRESSION;
    private final int BLOCKS_COUNT; // Number of blocks to merge

    long finalDocidOffset = 0;
    long finalFreqOffset = 0;
    long blockDescriptorOffset = 0;
    long vFinalOffset = 0;

    public Merging(boolean compression, int blocksCount)
    {;
        this.BLOCKS_COUNT = blocksCount;
        this.COMPRESSION = compression;

        System.out.println(String.format(
                "-----MERGING-----\nCOMPRESSION = %b\nBLOCKS_TO_COMPRESS = %d\n-----------------",
                COMPRESSION,
                BLOCKS_COUNT
        ));

    }

    /**
     * Merge partial blocks into one unique block
     */
    public void mergeBlocks(boolean debug)
    {
        long nTerms = 0;
        // Check if the directory with the blocks results exists
        if(FileManager.checkDir(ConfigReader.getPartialPath()))
        {
            // Create a channel for each block, both for docId, frequencies and vocabulary fragments
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

            try (
                    // Open FileChannels to each file
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
                for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                {
                    String vocPath = ConfigReader.getPartialVocabularyPath() + indexBlock;
                    String docPath = ConfigReader.getPartialDocsPath() + indexBlock;
                    String freqPath = ConfigReader.getPartialFrequenciesPath() + indexBlock;

                    // Open FileChannels to each file
                    docIdFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(docPath),
                            StandardOpenOption.READ);
                    frequenciesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                            StandardOpenOption.READ);
                    vocabulariesFileChannel[indexBlock] = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                            StandardOpenOption.READ);

                    dimVocabularyFile[indexBlock] = vocabulariesFileChannel[indexBlock].size();

                    // Get first term for each block's vocabulary
                    vocs[indexBlock] = getNextVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
                    offsetVocabulary[indexBlock] += TermInfo.SIZE_PRE_MERGING;
                }

                // Until we have data to analyze inside blocks...
                while(true)
                {
                    // ...get the smallest term along all vocabularies...
                    String smallestTerm = getSmallestTerm(vocs);

                    // ...create a TermInfo to merge those in different blocks...
                    TermInfo finalTerm = new TermInfo(smallestTerm);

                    int finalTotalFreq = 0;
                    int totalTermPostings = 0;

                    // ...create two lists to accumulate bytes from different blocks...
                    // TODO -> Evaluate if it's better to create those lists outside the loop and clear each term

                    List<byte[]> docsAcc = new ArrayList<>();
                    List<byte[]> freqAcc = new ArrayList<>();

                    // For each block...
                    for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                    {
                        // ...if current term is equal the smallest (and it's not null)...
                        if(vocs[indexBlock] != null && vocs[indexBlock].getTerm().equals(smallestTerm))
                        {

                            // ...accumulates bytes from different blocks , for all blocks...
                            docsAcc.add(extractBytes(docIdFileChannel[indexBlock],offsetDocId[indexBlock],vocs[indexBlock].getNumPosting()));
                            freqAcc.add(extractBytes(frequenciesFileChannel[indexBlock],offsetFrequency[indexBlock],vocs[indexBlock].getNumPosting()));


                            // finalPostings and totalFrequency are jsut the sum of partial blocks
                            totalTermPostings += vocs[indexBlock].getNumPosting();
                            finalTotalFreq += vocs[indexBlock].getTotalFrequency();

                            // Update the offsets for current block
                            offsetDocId[indexBlock] += 4L * vocs[indexBlock].getNumPosting();
                            offsetFrequency[indexBlock] += 4L * vocs[indexBlock].getNumPosting();
                            /*
                                If this block is finished, set its vocs to null and skip
                                 This happen because last time we extracted a term for this
                                 block, it was the last term in the list
                                Null is used as break condition
                             */
                            if (offsetVocabulary[indexBlock] >= dimVocabularyFile[indexBlock]) {
                                System.err.println("LOG:\t\tBlock #" + indexBlock + " exhausted.");
                                vocs[indexBlock] = null;
                                continue;
                            }
                            // Vocabulary shift
                            vocs[indexBlock] = getNextVoc(vocabulariesFileChannel[indexBlock], offsetVocabulary[indexBlock]);
                            offsetVocabulary[indexBlock] += TermInfo.SIZE_PRE_MERGING;
                        }
                    }

                    // ... current term is ended, bytes are accumulated ...
                    int totalBytesSummed = docsAcc.stream().mapToInt(vec -> vec.length).sum();

                    byte[] concatenatedDocsBytes = new byte[totalBytesSummed];
                    byte[] concatenatedFreqBytes = new byte[totalBytesSummed];

                    int os = 0;
                    for(byte[] v: docsAcc){
                        System.arraycopy(v,0, concatenatedDocsBytes, os, v.length);
                        os += v.length;
                    }
                    os = 0;
                    for(byte[] v: freqAcc){
                        System.arraycopy(v,0, concatenatedFreqBytes, os, v.length);
                        os += v.length;
                    }

                    /* Now that we cumulated docids and frequencies for that term, split them in Blocks */

                    // ... write sqrt(n) postings in each block ...
                    int postingsInsideEachBlock = (int) Math.sqrt(totalTermPostings);
                    int numberOfBlocksToCreate = (int) Math.ceil((double) totalTermPostings / postingsInsideEachBlock);

                    // ... divide the accumulated bytes in sqrt(n) blocks ...
                    List<byte[]> docidBlocks = new ArrayList<>();
                    List<byte[]> freqBlocks = new ArrayList<>();
                    List<BlockDescriptor> blockDescriptors = new ArrayList<>();

                    // ... if totalTerms is less than 512, just write one block ...
                    if (totalTermPostings < MIN_POSTING_LIMIT) {
                        BlockDescriptor blockDescriptor = new BlockDescriptor();
                        blockDescriptor.setNumPostings(totalTermPostings);


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
                        blockDescriptor.setMaxDocID(getMaxDocid(concatenatedDocsBytes));
                        blockDescriptors.add(blockDescriptor);
                        finalTerm.setNumBlocks(1);
                    }

                    // ... otherwise, divide the block in sqrt(n) blocks ...
                    else {

                    /* From there we can use postingsInsideEachBlock as the number of postings in each block
                     * and numberOfBlocksToCreate as the number of blocks to create
                     */

                        for (int i = 0; i < numberOfBlocksToCreate; i++) {

                            // Last block may contain fewer elements than blockSize
                            int postingsInCurrentBlock = Math.min(
                                    postingsInsideEachBlock,
                                    totalTermPostings - i * postingsInsideEachBlock
                            );

                            // Block descriptor for current block
                            BlockDescriptor blockDescriptor = new BlockDescriptor();
                            blockDescriptor.setNumPostings(postingsInCurrentBlock);

                            byte[] tempDocs = new byte[postingsInCurrentBlock * 4];
                            byte[] tempFreq = new byte[postingsInCurrentBlock * 4];

                            // Get the bytes for the current block from the accumulated bytes
                            System.arraycopy(concatenatedDocsBytes, i * postingsInsideEachBlock * 4,
                                    tempDocs, 0,
                                    postingsInCurrentBlock * 4);

                            System.arraycopy(concatenatedFreqBytes, i * postingsInsideEachBlock * 4,
                                    tempFreq, 0,
                                    postingsInCurrentBlock * 4);

                            if(COMPRESSION){
                                tempDocs = Compressor.VariableByteCompression(tempDocs);
                                tempFreq = Compressor.UnaryCompression(tempFreq);
                            }

                            // Update bytes occupied in that block
                            blockDescriptor.setBytesOccupiedDocid(tempDocs.length);
                            blockDescriptor.setBytesOccupiedFreq(tempFreq.length);

                            // Add to the list of blocks
                            docidBlocks.add(tempDocs);
                            freqBlocks.add(tempFreq);

                            // Update current blockDescriptor and add to the list
                            blockDescriptor.setMaxDocID(getMaxDocid(docidBlocks.get(i)));
                            blockDescriptors.add(blockDescriptor);

                        finalTerm.setNumBlocks(numberOfBlocksToCreate);
                        }
                    }

                    // ... update the final term ... (Not dependent by compression or blocks splitting)
                    finalTerm.setNumPosting(totalTermPostings);
                    finalTerm.setTotalFrequency(finalTotalFreq);

                    // Write everything as separated blocks
                    writeBlocks(finalDocIDChannel, finalFreqChannel, blockDescriptorsChannel,
                            docidBlocks,freqBlocks, blockDescriptors,
                            finalTerm);

                    // First offset of first term Block is updated inside the previous function

                    writeTermToDisk(finalVocChannel, finalTerm);

                    nTerms++;

                    if(nTerms % 100_000 == 0){
                        System.out.println(String.format("LOG:\t\t%d terms have been processed", nTerms));
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
                        if(t != null )
                        {
                            allNull = false;
                            break;
                        }
                    }

                    if(allNull)
                        break;

                }

                // Delete temporary blocks
//                FileManager.deleteDir(ConfigReader.getPartialPath());
                System.out.println(String.format("LOG:\t\tTotal terms in the Lexicon is %d", nTerms));
                CollectionInformation.setTotalTerms(nTerms);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("ERR\t\tMerge error, directory " + ConfigReader.getPartialPath() + " doesn't exists!");
        }
    }

    private void writeBlocks(FileChannel finalDocIDChannel,
                             FileChannel finalFreqChannel,
                             FileChannel blockDescriptorsChannel,
                             List<byte[]> docsBlocks,
                             List<byte[]> freqBlocks,
                             List<BlockDescriptor> blockDescriptors,
                             TermInfo finalTerm) {

        // Write first blockDescriptor offset for that term
        finalTerm.setOffset(blockDescriptorOffset);

        try
        {
            // ... for each block ...
            for(int i = 0; i < blockDescriptors.size(); i++)
            {
                //
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
                if (finalTerm.getTerm().equals("bomb"))
                    System.out.println(blockDescriptors.get(i).toString());
            }

        }
        catch (IOException io)
        {
            io.printStackTrace();
        }

    }

    /**
     * Write a TermInfo on the disk
     * @param finalVocChannel Vocabulary FileChannel
     * @param finalTerm Term to write
     * @throws IOException
     */
    private void writeTermToDisk(FileChannel finalVocChannel, TermInfo finalTerm) throws IOException
    {
        MappedByteBuffer tempBuffer = finalVocChannel.map(FileChannel.MapMode.READ_WRITE, vFinalOffset, TermInfo.SIZE_POST_MERGING);

        StringBuilder pattern = new StringBuilder("%-").append(TermInfo.SIZE_TERM).append("s");
        String paddedTerm = String.format(pattern.toString(), finalTerm.getTerm()).substring(0, TermInfo.SIZE_TERM); // Pad with spaces up to 64 characters

        // Write
        tempBuffer.put(paddedTerm.getBytes());
        tempBuffer.putInt(finalTerm.getTotalFrequency());
        tempBuffer.putInt(finalTerm.getNumPosting());
        tempBuffer.putInt(finalTerm.getNumBlocks());
        tempBuffer.putLong(finalTerm.getOffset());

        vFinalOffset += TermInfo.SIZE_POST_MERGING;
    }

    /**
     * First term in lexicographic order between all terms
     * @param vocs TermInfo array to pick the terms from
     * @return Smallest term in lexicographic order
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
     * Get next TermInfo from that channel
     * @param fileChannel FileChannel to retrieve the Term from
     * @param offsetVocabulary Offset at which the term is
     * @return Next TermInfo in line
     * @throws IOException
     */
    private TermInfo getNextVoc(FileChannel fileChannel, long offsetVocabulary) throws IOException
    {
        MappedByteBuffer tempBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, offsetVocabulary, TermInfo.SIZE_PRE_MERGING);

        byte[] termBytes = new byte[TermInfo.SIZE_TERM];
        tempBuffer.get(termBytes);

        int frequency = tempBuffer.getInt();
        int nPosting = tempBuffer.getInt();
        long offset = tempBuffer.getLong();

        String term = new String(termBytes).trim();

        return new TermInfo(term, frequency, nPosting, 0, offset);
    }
    /**
     * Extract bytes from given channel at given offset and returns them
     *
     * @param fromChannel File Channel to extract Bytes from
     * @param offset At which offset to extract bytes
     * @param nPosting How many 4-bytes to extract
     * @return Array of Bytes extracted
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

    private int getMaxDocid(byte[] list){
        int max = 0;
        int[] ints = Commons.bytesToIntArray(list);
        for(Integer i: ints){
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
 * Alla fine dell'algoritmo, viene aggiornato il valore globale di CollectionInformation:
 *  Total (unique) Terms nella collezione
 *
 * Non viene fatto uso della classe Vocabolario, in quanto si assume che i termini siano tutti ordinati lessicograficamente
 *  nei vari blocchi, e potento aggiornare le informazioni di ogni termine on-the-fly, non e' necessario mantenere tali
 *  strutture in memoria
 */

