package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.FileManager;
import it.unipi.aide.utils.Preprocesser;
import it.unipi.aide.utils.ReturnableFromSPIMI;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
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
    private Vocabulary vocabulary;
    private InvertedIndex invertedIndex;
    private DocumentIndex documentIndex;
    private final Preprocesser preprocesser;
    private int incrementalBlockNumber;
    private int numBlocksPosting;

    private CollectionInformation ci;

    private int docid = 0;

    /**
     * SPIMI constructor
     *
     * @param inputPath Where the Corpus to process is located
     * @param stemming    Enable/Disable Stemming & Stopwords removal
     */
    public SPIMI(String inputPath, boolean stemming)
    {
        this.INPUT_PATH = inputPath;

        vocabulary = new Vocabulary();
        invertedIndex = new InvertedIndex();
        documentIndex = new DocumentIndex();

        preprocesser = new Preprocesser(stemming);
        incrementalBlockNumber = 0;
        numBlocksPosting = 0;

        System.out.println(String.format(
                "-----SPIMI-----\nINPUT_PATH = %s\nSTEMMING = %b\n---------------",
                INPUT_PATH,
                stemming
        ));
    }

    /**
     * Executes the SPIMI algorithm as configured
     * @param debug Enable debug mode
     * @return the number of blocks created
     */
    public int algorithm(boolean debug)
    {
        System.out.println("Starting SPIMI algorithm...");

        // Starting cleaning the folder
        FileManager.cleanFolder(ConfigReader.getWorkingDir());
        FileManager.cleanFolder(ConfigReader.getDebugDir());

        Corpus corpus = new Corpus(INPUT_PATH);

        // Terms in all documents
        long termSum = 0;

        // Used to calculate the upper bound
        HashMap<String, Integer> lastDocScoredForTerm = new HashMap<>();

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
            List<String> tokens = preprocesser.process(text);

            // To update AvarageDocumentLenght
            termSum += tokens.size();

            Document document = new Document(pid, docid, tokens);
            documentIndex.add(document);

            docid++;

            // current document has no tokens inside, skip
            if(document.getTokens().isEmpty()) continue;


            for (String t : document.getTokens())
            {
                /*
                 * ADDING A TERM TO INVERTED INDEX AND VOCABULARY
                 * Always add to the InvertedIndex
                 * Increment NumPosting of that term only if a new PostingList
                 * has been added in the InvertedIndex
                 */

                boolean newPosting = invertedIndex.add(document.getDocid(), t);
                vocabulary.add(t, newPosting);
                if(newPosting) numBlocksPosting++;


                // TERM UPPER BOUND TDIDF
                // Calculate the actual term frequency
                int actualTermFrequency = document.getTokens().stream().mapToInt(s -> s.equals(t) ? 1 : 0).sum();

                // if we have never seen this term before, we need to set the upper bound
                if (!lastDocScoredForTerm.containsKey(t))
                {
                    vocabulary.getTermInfo(t).setTermUpperBoundTDIDF((float) (1.0 + Math.log(actualTermFrequency)));
                    lastDocScoredForTerm.put(t, document.getDocid());
                }
                // if we have already seen this term in the previous document we need to update the partial score
                else if (lastDocScoredForTerm.get(t) < document.getDocid())
                {
                    // we need to update the partial score
                    double partialScore = vocabulary.getTermInfo(t).getTermUpperBoundTDIDF() + 1.0 + Math.log(actualTermFrequency);
                    vocabulary.getTermInfo(t).setTermUpperBoundTDIDF((float) partialScore);
                    lastDocScoredForTerm.replace(t, document.getDocid());
                }
            }

            // Memory control
            if(memoryCheck())
            {
                System.out.println("LOG:\t\tWriting block #" + incrementalBlockNumber);
                if (writeBlockToDisk(debug))
                {
                    incrementalBlockNumber++;
                    numBlocksPosting = 0;

                    vocabulary.clear();
                    invertedIndex.clear();
                    System.gc();
                }
                else
                {
                    System.err.println("ERROR:\t\tNot able to write the binary file");
                    break;
                }
            }
            // End memory control

            if (docid%100000 == 0)
            {
                // printMemInfo();
                System.out.println("LOG:\t\tDocuments processed " + docid);
            }
        }

        // We need to write the last block
        if (writeBlockToDisk(debug))
        {
            System.out.println("LOG:\t\tWriting block #" + incrementalBlockNumber);
            incrementalBlockNumber++;
            // Manually free memory
            vocabulary.clear();
            invertedIndex.clear();
        }
        else
        {
            System.out.println("ERROR:\t\tNot able to write the binary file");
        }

        // Write CollectionDocument number and AvarageDocumentLenght
        CollectionInformation.setTotalDocuments(docid);
        CollectionInformation.setAverageDocumentLength(termSum / docid);


        // There will be 'incrementalBlockNumber' blocks, but the last one has index 'incrementalBlockNumber - 1'
        return incrementalBlockNumber;
    }

    /**
     * Write partial Inverted Index on the disk
     * @return true upon success, false otherwise
     */
    public boolean writeBlockToDisk(boolean debug)
    {
        String docPath = ConfigReader.getPartialDocsPath() + incrementalBlockNumber;
        String freqPath = ConfigReader.getPartialFrequenciesPath() + incrementalBlockNumber;
        String vocPath = ConfigReader.getPartialVocabularyPath() + incrementalBlockNumber;

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
            MappedByteBuffer docIdBuffer = docIdFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, numBlocksPosting*4L);
            MappedByteBuffer frequencyBuffer = frequencyFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, numBlocksPosting*4L);
            MappedByteBuffer vocabularyBuffer = vocabularyFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, vocabulary.getTerms().size()*TermInfo.SIZE_PRE_MERGING);

            // Used to write TermInfo on the disk, one next to the other
            long partialOffset = 0;

            for (String t: vocabulary.getTerms())
            {
                // For each term I have to save into the vocabulary file.
                TermInfo termInfo = vocabulary.get(t);
                // Set the offset at which postings start
                termInfo.setOffset(partialOffset);

                // Write vocabulary entry
                StringBuilder pattern = new StringBuilder("%-").append(TermInfo.SIZE_TERM).append("s");
                String paddedTerm = String.format(pattern.toString(), termInfo.getTerm()).substring(0, TermInfo.SIZE_TERM); // Pad with spaces up to 64 characters

                vocabularyBuffer.put(paddedTerm.getBytes());                    // Term                     46 bytes
                vocabularyBuffer.putInt(termInfo.getTotalFrequency());          // TotalFrequency           4 bytes
                vocabularyBuffer.putInt(termInfo.getNumPosting());              // NumPosting               4 bytes
                vocabularyBuffer.putLong(termInfo.getOffset());                 // Offset                   8 bytes
                vocabularyBuffer.putFloat(termInfo.getTermUpperBoundTDIDF());   // TermUpperBoundTDIDF      4 bytes
                vocabularyBuffer.putFloat(termInfo.getTermUpperBoundBM25());    // TermUpperBoundBM25       4 bytes

                // Write the other 2 files for DocId and Frequency
                for (Posting p: invertedIndex.getPostingList(t))
                {
                    docIdBuffer.putInt(p.getDocId());
                    frequencyBuffer.putInt(p.getFrequency());
                    partialOffset += 4L;
                }
            }
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
                        new FileWriter((ConfigReader.getDebugDir() + "Block-" + incrementalBlockNumber + ".txt"))
                );
                // Write vocabulary to debug text file
                BufferedWriter vocabularyWriter = new BufferedWriter(
                        new FileWriter((ConfigReader.getDebugDir() + "Vocabulary-" + incrementalBlockNumber + ".txt"))
                )
            )
            {
                indexWriter.write(invertedIndex.toString());
                vocabularyWriter.write(vocabulary.toString());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Support function to get used memory in %
     * @return xx.x% of memory used
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
        if(occVirMemory > maxVirMemory * 40 / 100) {
//            System.out.println("Free virtual memory security limit");
            return true;
        }

        return false;
    }

    private void printMemInfo(){
        double freeVirMemory = Runtime.getRuntime().freeMemory() / Math.pow(10,6);
        double totalVirMemory = Runtime.getRuntime().totalMemory() / Math.pow(10,6);
        double maxVirMemory = Runtime.getRuntime().maxMemory() / Math.pow(10,6);
        double occVirMemory = totalVirMemory - freeVirMemory;
        double percentOccVirMemory = (occVirMemory/maxVirMemory) * 100;

        System.out.println(String.format("LOG:\t\tMaxVir: %.2f\tTotalVir: %.2f\tFreeVir: %.2f\tOccupiedVir: %.2f\tPercentVir: %.2f",
                maxVirMemory,
                totalVirMemory,
                freeVirMemory,
                occVirMemory,
                percentOccVirMemory
        ));
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

