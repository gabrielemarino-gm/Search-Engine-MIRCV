package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
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

public class MergingM
{
    private final boolean COMPRESSION;
    private final int BLOCKS_COUNT;

    long finalDocidOffset = 0;
    long finalFreqOffset = 0;
    long vFinalOffset = 0;

    private InvertedIndex invertedIndex = new InvertedIndex();

    public MergingM(boolean compression, int blocksCount)
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

            if(!FileManager.checkDir(FdocPath)) FileManager.createFile(FdocPath);
            if(!FileManager.checkDir(FfreqPath)) FileManager.createFile(FfreqPath);
            if(!FileManager.checkDir(FvocPath)) FileManager.createFile(FvocPath);

            try (
                    // Open FileChannels to each file
                    FileChannel finalDocIDChannel = (FileChannel) Files.newByteChannel(Paths.get(FdocPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    FileChannel finalFreqChannel = (FileChannel) Files.newByteChannel(Paths.get(FfreqPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    FileChannel finalVocChannel = (FileChannel) Files.newByteChannel(Paths.get(FvocPath),
                            StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
                    )
            {
                // INIT PHASE
                for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                {
                    String docPath = ConfigReader.getPartialDocsPath() + indexBlock;
                    String freqPath = ConfigReader.getPartialFrequenciesPath() + indexBlock;
                    String vocPath = ConfigReader.getPartialVocabularyPath() + indexBlock;

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

                // Until we have data to analyze
                while(true)
                {
                    // Get the smallest term along all vocabularies
                    String smallestTerm = getSmallestTerm(vocs);

                    // Create a TermInfo to merge those in different blocks
                    TermInfo finalTerm = new TermInfo(smallestTerm);
                    finalTerm.setDocidOffset(finalDocidOffset);
                    finalTerm.setFreqOffset(finalFreqOffset);

                    int finalFreq = 0;
                    int finalNPostings = 0;

                    List<byte[]> docsAcc = new ArrayList<>();
                    List<byte[]> freqAcc = new ArrayList<>();

                    // For each block...
                    for (int indexBlock = 0; indexBlock < BLOCKS_COUNT; indexBlock++)
                    {
                        // ...if current term is equal the smallest (and it's not null)...
                        if(vocs[indexBlock] != null && vocs[indexBlock].getTerm().equals(smallestTerm))
                        {
                            // Compression differs from no compression on different final offset
                            if(COMPRESSION){
                                // ...accumulates bytes from different blocks , for all blocks...
                                docsAcc.add(extractBytes(docIdFileChannel[indexBlock],offsetDocId[indexBlock],vocs[indexBlock].getNumPosting()));
                                freqAcc.add(extractBytes(frequenciesFileChannel[indexBlock],offsetFrequency[indexBlock],vocs[indexBlock].getNumPosting()));

                            }
                            else {
                                // ...transfer docs and freq into final buffer...
                                transferBytes(docIdFileChannel[indexBlock],
                                        offsetDocId[indexBlock],
                                        finalDocIDChannel,
                                        finalDocidOffset,
                                        vocs[indexBlock].getNumPosting()
                                );
                                transferBytes(frequenciesFileChannel[indexBlock],
                                        offsetFrequency[indexBlock],
                                        finalFreqChannel,
                                        finalFreqOffset,
                                        vocs[indexBlock].getNumPosting());

                                // ...update final offset, nPosting and frequency for that term as we merge blocks...
                                finalDocidOffset += 4L * vocs[indexBlock].getNumPosting();
                                finalFreqOffset += 4L * vocs[indexBlock].getNumPosting();
                            }

                            // NumPostings and TotalTermFrequency unchanged
                            finalNPostings += vocs[indexBlock].getNumPosting();
                            finalFreq += vocs[indexBlock].getTotalFrequency();

                            // Update the offsets for current block, unchanged if compression
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

                    if(COMPRESSION)
                    {
                        // ... current term is ended, bytes are accumulated, compress the vectors ...
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

                        // ... compress the entire vectors ...
                        /* TODO -> If we have to divide into blocks, we have to do it here
                         *          and create blocks with correct information inside
                         *          and update TermInfo accordingly
                         */
                        byte[] compressedDocs = Compressor.VariableByteCompression(concatenatedDocsBytes);
                        byte[] compressedFreq = Compressor.UnaryCompression(concatenatedFreqBytes);

                        // ... and write them on the disk
                        writeCompressedToFile(finalDocIDChannel, compressedDocs, finalFreqChannel, compressedFreq);

                        // Update final offsets
                        finalTerm.setBytesOccupiedDocid(compressedDocs.length);
                        finalTerm.setBytesOccupiedFreq(compressedFreq.length);

                        finalDocidOffset += finalTerm.getBytesOccupiedDocid();
                        finalFreqOffset += finalTerm.getBytesOccupiedFreq();
                    }
                    else
                    {
                        // Without compression, bytes occupied are 4L for each number
                        finalTerm.setBytesOccupiedDocid(finalNPostings * 4L);
                        finalTerm.setBytesOccupiedFreq(finalFreq * 4L);

                    }

                    // Vocabulary offset unchanged, finalTerm also
                    finalTerm.setNumPosting(finalNPostings);
                    finalTerm.setTotalFrequency(finalFreq);

                    // Write accumulated term on the disk
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

    /**
     * Write compressed bytes to the disk
     *
     * @param finalDocIDChannel FileChannel to write DocIDs to
     * @param compressedDocs    Compressed DocIDs
     * @param finalFreqChannel FileChannel to write Frequencies to
     * @param compressedFreq    Compressed Frequencies
     */
    private void writeCompressedToFile(FileChannel finalDocIDChannel, byte[] compressedDocs, FileChannel finalFreqChannel, byte[] compressedFreq) throws IOException
    {
        MappedByteBuffer docBuffer = finalDocIDChannel.map(FileChannel.MapMode.READ_WRITE, finalDocidOffset, compressedDocs.length);
        docBuffer.put(compressedDocs);

        MappedByteBuffer freqBuffer = finalFreqChannel.map(FileChannel.MapMode.READ_WRITE, finalFreqOffset, compressedFreq.length);
        freqBuffer.put(compressedFreq);
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
        tempBuffer.putLong(finalTerm.getDocidOffset());
        tempBuffer.putLong(finalTerm.getFreqOffset());
        tempBuffer.putLong(finalTerm.getBytesOccupiedDocid());
        tempBuffer.putLong(finalTerm.getBytesOccupiedFreq());
        tempBuffer.putInt(finalTerm.getNumPosting());

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
        long offset = tempBuffer.getLong();
        int nPosting = tempBuffer.getInt();

        String term = new String(termBytes).trim();

        return new TermInfo(term, frequency, offset, offset, nPosting);
    }

    /**
     * Copy a window of bytes from one channel to another
     * @param fromChannel Source channel
     * @param fromOffset Source starting offset
     * @param toChannel Destination channel
     * @param toOffset Destination starting offset
     * @param nPostings How many elements (4 Bytes each) to copy
     * @throws IOException
     */
    private void transferBytes(FileChannel fromChannel, long fromOffset,
                               FileChannel toChannel, long toOffset, int nPostings) throws IOException
    {
        // Buffer from offset to offset + 4L*nPosting bytes
        MappedByteBuffer tempInBuff = fromChannel.map(FileChannel.MapMode.READ_ONLY,
                fromOffset, 4L*nPostings);
        MappedByteBuffer tempOutBuff = toChannel.map(FileChannel.MapMode.READ_WRITE,
                toOffset, 4L*nPostings);

        byte[] tempBytes = new byte[4 * nPostings];

        tempInBuff.get(tempBytes);
        tempOutBuff.put(tempBytes);
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

