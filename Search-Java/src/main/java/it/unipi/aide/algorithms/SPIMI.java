package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;
import me.tongfei.progressbar.ProgressBar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Class that implement the SPIMI algorithm
 *
 * Creates Inverted Indexes for each split of a corpus
 * based on available memory
 *
 * NB: Final merging is temporarily made by another class
 */
public class SPIMI 
{
    private final String INPUT_PATH;
    private final Vocabulary VOCABULARY;
    private final InvertedIndex INVERTED_INDEX;
    private final DocumentIndex DOCUMENT_INDEX;
    private final Preprocesser PREPROCESSER;
    private int INCREMENTAL_PARTIAL_BLOCK_NUMBER;
    private int CURRENT_BLOCK_POSTING_COUNT;
    private int INCREMENTAL_DOCID = 0;

    /**
     * SPIMI constructor
     *
     * @param inputPath Where the Corpus to process is located
     * @param stemming    Enable/Disable Stemming & Stopwords removal
     */
    public SPIMI(String inputPath, boolean stemming)
    {
        this.INPUT_PATH = inputPath;

        VOCABULARY = new Vocabulary();
        INVERTED_INDEX = new InvertedIndex();
        DOCUMENT_INDEX = new DocumentIndex();

        PREPROCESSER = new Preprocesser(stemming);
        INCREMENTAL_PARTIAL_BLOCK_NUMBER = 0;
        CURRENT_BLOCK_POSTING_COUNT = 0;

        // System.out.println(String.format(
        //         "SPIMI >\tINPUT_PATH = %s\nSPIMI >\tSTEMMING = %b",
        //         INPUT_PATH,
        //         stemming
        // ));
    }

    /**
     * Executes the SPIMI algorithm as configured
     * @param debug Enable debug mode
     * @return the number of blocks created
     */
    public int algorithm(boolean debug)
    {

        // System.out.println("SPIMI > Starting SPIMI algorithm...");


        // Starting cleaning the folder
        FileManager.cleanFolder(ConfigReader.getWorkingDir());
        FileManager.cleanFolder(ConfigReader.getDebugDir());

        Corpus corpus = new Corpus(INPUT_PATH);
        if (!corpus.iterator().hasNext())
            return 0;

        // Terms in all documents
        long globalTermCountSum = 0;

        ProgressBar pb = new ProgressBar("SPIMI > ", 8841823);
        pb.start();
        // For each documents
        for(String doc: corpus)
        {
            String[] docParts = doc.split("\t");

            /*
             * docid = Unique id assigned in incremental manner
             * pid = Unique name of the document
             */
            String pid = docParts[0];
            String text = docParts[1];
            List<String> tokens = PREPROCESSER.process(text);

            // To update AvarageDocumentLenght
            globalTermCountSum += tokens.size();

            Document currentDocument = new Document(pid, INCREMENTAL_DOCID, tokens);
            DOCUMENT_INDEX.add(currentDocument);

            INCREMENTAL_DOCID++;

            // current document has no tokens inside, skip
            if(currentDocument.getTokens().isEmpty()) continue;


            for (String t : currentDocument.getTokens())
            {
                /*
                 * ADDING A TERM TO INVERTED INDEX AND VOCABULARY
                 * Always add to the InvertedIndex
                 * Increment NumPosting of that term only if a new PostingList
                 * has been added in the InvertedIndex
                 */

                // Add to the inverted index the term t for the current document
                boolean newPostingWasCreated = INVERTED_INDEX.add(currentDocument.getDocid(), t);

                // addNew automatically handle a new term, a new posting or an already existing one
                VOCABULARY.addNew(t, newPostingWasCreated);

                // Always update maxTF and MaxBM25 parameters
                VOCABULARY.get(t).setMaxTF(INVERTED_INDEX.getLastPosting(t).getFrequency());
                VOCABULARY.get(t).setMaxBM25(INVERTED_INDEX.getLastPosting(t).getFrequency(), currentDocument.getTokenCount());

                CURRENT_BLOCK_POSTING_COUNT++;
            }

            // Memory control
            if(memoryCheck())
            {
                // System.out.println("SPIMI > Writing block #" + INCREMENTAL_PARTIAL_BLOCK_NUMBER);
                if (writeBlockToDisk(debug))
                {
                    INCREMENTAL_PARTIAL_BLOCK_NUMBER++;
                    CURRENT_BLOCK_POSTING_COUNT = 0;

                    VOCABULARY.clear();
                    INVERTED_INDEX.clear();
                    System.gc();
                }
                else
                {
                    pb.stop();
                    System.err.println("SPIMI ERROR > Not able to write the binary file");
                    break;
                }
            }
            // End memory control

            if (INCREMENTAL_DOCID %10_000 == 0)
            {
                pb.stepBy(10000);
                // printMemInfo();
                //System.out.println("SPIMI > Documents processed " + INCREMENTAL_DOCID);
            }
        }

        // We need to write the last block
        if (writeBlockToDisk(debug))
        {
            // System.out.println("SPIMI > Writing block #" + INCREMENTAL_PARTIAL_BLOCK_NUMBER);
            INCREMENTAL_PARTIAL_BLOCK_NUMBER++;

            // Manually free memory
            VOCABULARY.clear();
            INVERTED_INDEX.clear();
        }
        else
        {
            pb.stop();
            System.out.println("SPIMI ERROR > Not able to write the binary file");
        }

        // Write CollectionDocument number and AvarageDocumentLenght
        CollectionInformation.setTotalDocuments(INCREMENTAL_DOCID);
        CollectionInformation.setAverageDocumentLength(globalTermCountSum / INCREMENTAL_DOCID);

        pb.stepTo(8841823);
        pb.stop();


        // There will be 'incrementalBlockNumber' blocks, but the last one has index 'incrementalBlockNumber - 1'
        return INCREMENTAL_PARTIAL_BLOCK_NUMBER;
    }

    /**
     * --------------------------------------------------------------------------
     * Write partial Inverted Index on the disk
     * @return true upon success, false otherwise
     * ---------------------------------------------------------------------------
     */
    public boolean writeBlockToDisk(boolean debug)
    {
        String docPath = ConfigReader.getPartialDocsPath() + INCREMENTAL_PARTIAL_BLOCK_NUMBER;
        String freqPath = ConfigReader.getPartialFrequenciesPath() + INCREMENTAL_PARTIAL_BLOCK_NUMBER;
        String vocPath = ConfigReader.getPartialVocabularyPath() + INCREMENTAL_PARTIAL_BLOCK_NUMBER;

        if(!FileManager.checkFile(docPath)) FileManager.createFile(docPath);
        if(!FileManager.checkFile(freqPath)) FileManager.createFile(freqPath);
        if(!FileManager.createFile(vocPath)) FileManager.createFile(vocPath);

        try (
                // Open a FileChannel for docId, frequencies and vocabulary fragments
                FileChannel docIdFileChannel = (FileChannel) Files.newByteChannel(Paths.get(docPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                FileChannel frequencyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

                FileChannel vocabularyFileChannel = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE)
            )
        {
            // Create the buffer where write the streams of bytes
            MappedByteBuffer docIdBuffer = docIdFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, CURRENT_BLOCK_POSTING_COUNT *4L);
            MappedByteBuffer frequencyBuffer = frequencyFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, CURRENT_BLOCK_POSTING_COUNT *4L);
            MappedByteBuffer vocabularyBuffer = vocabularyFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, VOCABULARY.getTerms().size() * TermInfo.SIZE_PRE_MERGING);

            // Used to write TermInfo on the disk, one next to the other
            long partialOffset = 0;

            for (String t: VOCABULARY.getTerms())
            {
                // For each term I have to save into the vocabulary file.
                TermInfo termInfo = VOCABULARY.get(t);
                // Set the offset at which postings start
                termInfo.setOffset(partialOffset);

                // Write vocabulary entry
                String paddedTerm = String.format("%-" + TermInfo.SIZE_TERM + "s", termInfo.getTerm()).substring(0, TermInfo.SIZE_TERM); // Pad with spaces up to 46 characters

                vocabularyBuffer.put(paddedTerm.getBytes());                    // Term                     46 bytes
                vocabularyBuffer.putInt(termInfo.getTotalFrequency());          // TotalFrequency           4 bytes
                vocabularyBuffer.putInt(termInfo.getNumPosting());              // NumPosting               4 bytes
                vocabularyBuffer.putLong(termInfo.getOffset());                 // Offset                   8 bytes
                vocabularyBuffer.putInt(termInfo.getMaxTF());                   // maxTF                    4 bytes
                vocabularyBuffer.putInt(termInfo.getBM25TF());                  // TFBM25                   4 bytes
                vocabularyBuffer.putInt(termInfo.getBM25DL());                  // DLBM25                   4 bytes

                // Write the other 2 files for DocId and Frequency
                for (Posting p: INVERTED_INDEX.getPostingList(t))
                {
                    docIdBuffer.putInt(p.getDocId());
                    frequencyBuffer.putInt(p.getFrequency());
                    partialOffset += 4L;
                }
            }
            DOCUMENT_INDEX.bulkWrite();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        // Debug version to write plain text
        if (debug)
        {
            FileManager.createDir(ConfigReader.getDebugDir());
            try(
                // Write inverted index to debug text file
                BufferedWriter indexWriter = new BufferedWriter(
                        new FileWriter((ConfigReader.getDebugDir() + "Block-" + INCREMENTAL_PARTIAL_BLOCK_NUMBER + ".txt"))
                );
                // Write vocabulary to debug text file
                BufferedWriter vocabularyWriter = new BufferedWriter(
                        new FileWriter((ConfigReader.getDebugDir() + "Vocabulary-" + INCREMENTAL_PARTIAL_BLOCK_NUMBER + ".txt"))
                )
            )
            {
                indexWriter.write(INVERTED_INDEX.toString());
                vocabularyWriter.write(VOCABULARY.toString());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * --------------------------------------------------------------------------
     * Support function to get used memory in %
     * @return xx.x% of memory used
     * --------------------------------------------------------------------------
     */
    private boolean memoryCheck(){

        Runtime rt = Runtime.getRuntime();
        double maxVirMemory = rt.maxMemory() / Math.pow(10,6);          // Max memory possible (1.8Gb default)
        // -----------------------------------
        double totalVirMemory = rt.totalMemory() / Math.pow(10,6);      // Allocated
        double freeVirMemory = rt.freeMemory() / Math.pow(10,6);        // Allocated and Free
        double occVirMemory = totalVirMemory - freeVirMemory;           // Allocated and not-Free

        // User-enabled threshold
        if((occVirMemory > totalVirMemory * 91 / 100)) {
//            System.out.println("User threshold reached");
            return true;
        }

        // Not enough FreeVirtual to fill with OccupiedVirtual
        //            System.out.println("Free virtual memory security limit");
        return occVirMemory > maxVirMemory * 40 / 100;
    }
}

/*
 * La seguente classe si occupa esclusivamente di creare blocchi parziali di Inverted Index, a partire dal Corpus intero.
 *  La creazione di piu blocchi e' necessaria, in quanto una macchina non ha memoria infinita.
 *
 * In particolare: per ogni documento del Corpus, viene effettuato un preprocessing (comprensivo di Stemming, rimozione delle Stopwords,
 *  rimozione di tag HTML e URL, lowercasing, eccetera).
 *  Il documento e' quindi ridotto ad una lista di Token, che saranno usati per aggiornare i Posting per quel documento.
 *  La struttura del Vocabolario e dell'Inverted Index, gestiscono internamente l'aggiunta di nuovi Posting o Termini.
 *  Al raggiungimento di un certo limite di memoria libera, le strutture parziali saranno scritte su disco e liberate per continuare
 *  lo stesso processo con la prossima partizione del Corpus.
 *
 * Si noti che viene creato anche un Document Index, contenente informazioni per ogni documento
 *  Tale struttura non richiede il partizionamento, in quanto ogni documento e' semplicemente scritto in modalita "append"
 *  alla fine dell'apposito file
 *
 * Quando il Corpus viene esaurito, vengono scritte due delle informazioni globali utilizzate in seguito:
 *  Document Average Length e Total Documents nella collezione
 *  Anche questa scrittura puo essere fatta direttamente su file, ed e' gestita automaticamente dell'apposita classe
 *
 * Al termine dell'algoritmo di SPIMI, inizia il Merging dei blocchi
 * NB: Il Merging, seppur implementato in una classe diversa, fa parte di SPIMI, ma per questioni di divisione del lavoro tra membri
 *  del gruppo e' stata creata (temporaneamente o permanentemente) una classe diversa
 */

