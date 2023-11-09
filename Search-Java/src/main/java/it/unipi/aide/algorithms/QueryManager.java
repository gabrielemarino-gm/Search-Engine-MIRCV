package it.unipi.aide.algorithms;

import it.unipi.aide.model.*;
import it.unipi.aide.utils.Preprocesser;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class QueryManager {

    private final String WORK_DIR_PATH;
    private Vocabulary vocabulary;
    private DocumentIndex documentIndex;
    private CollectionInformation ci; // This class should load configurations from a  file, not manually set
    private DAAT daat;


    public QueryManager(String in_path){
        WORK_DIR_PATH = in_path;
        documentIndex =  new DocumentIndex(WORK_DIR_PATH);
        ci = new CollectionInformation(WORK_DIR_PATH);
        daat = new DAAT(WORK_DIR_PATH, 5);
    }

    public void makeQuery(String phrase){

//        TermInfo term = vocabulary.get("bomb");
//        System.out.println(String.format("[%s]\t\t Postings: %d\tDocID Bytes: %d\t Freq Bytes: %d",term.getTerm(), term.getNumPosting(), term.getBytesOccupiedDocid(),term.getBytesOccupiedFreq()));


        List<String> tokens = new Preprocesser(true).process(phrase);
        List<ScoredDocument> top_k = daat.executeDAAT(tokens);

        for(ScoredDocument d : top_k) System.out.println(String.format("PID: %s\tScore: %f", d.getDocID(), d.getScore()));
    }

//    private TermInfo binarySearch(String term)
//    {
//        try(
//                FileChannel channel = (FileChannel) Files.newByteChannel(Paths.get(WORK_DIR_PATH + "vocabularyBlock"),
//                        StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
//                )
//        {
//            long WIN_DOWN = 0;
//            long WIN_UP = CollectionInformation.getTotalTerms();
//            while (true)
//            {
//                long MID_POINT = (WIN_UP - WIN_DOWN)/ 2 + WIN_DOWN;
//                if(MID_POINT == 0) return null;
//                TermInfo middleTerm = getTerm(channel, MID_POINT);
//
//                int comp = middleTerm.getTerm().compareTo(term);
//                if (comp == 0)
//                {
//                    // Found
//                    return middleTerm;
//                }
//                else if (comp > 0)
//                {
//                    // Right half
//                    WIN_UP = MID_POINT;
//                }
//                else
//                {
//                    // Second half
//                    WIN_DOWN = MID_POINT;
//                }
//
//            }
//
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    private TermInfo getTerm(FileChannel channel, long from) throws IOException
//    {
//        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,from * TermInfo.SIZE_POST_MERGING, TermInfo.SIZE_POST_MERGING);
//
//        byte[] termBytes = new byte[TermInfo.SIZE_TERM];
//        buffer.get(termBytes);
//
//        String term = new String(termBytes).trim();
//        int totFreq = buffer.getInt();
//        long off = buffer.getLong();
//        long docBytes = buffer.getLong();
//        long freqBytes = buffer.getLong();
//        int nPost = buffer.getInt();
//
//        return new TermInfo(term, totFreq, off, docBytes, freqBytes, nPost);
//    }

    /**
     * Get the posting list from bin blocks
     * @param term Term to retrieve posting list
     * @return Posting List of given term
     */
//    public List<Posting> getPostingsByTerm(String term)
//    {
//        TermInfo toRetrieve = binarySearch(term);
//
//        if (toRetrieve == null) return null;
//
//        List<Posting> toRet = new ArrayList<>();
//
//        String docsPath = WORK_DIR_PATH + "docIDsBlock";
//        String freqPath = WORK_DIR_PATH + "frequenciesBlock";
//        try
//        {
//            FileChannel docsChannel = (FileChannel) Files.newByteChannel(Paths.get(docsPath),
//                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
//            FileChannel freqChannel = (FileChannel) Files.newByteChannel(Paths.get(freqPath),
//                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
//
//            long toRead = 4L * toRetrieve.getNumPosting();
//
//            MappedByteBuffer docBuffer = docsChannel.map(FileChannel.MapMode.READ_WRITE, toRetrieve.getOffset(), toRead);
//            MappedByteBuffer freqBuffer = freqChannel.map(FileChannel.MapMode.READ_WRITE, toRetrieve.getOffset(), toRead);
//            byte[] docBytes = new byte[toRetrieve.getNumPosting() * 4];
//            byte[] freqBytes = new byte[toRetrieve.getNumPosting() * 4];
//            docBuffer.get(docBytes);
//            freqBuffer.get(freqBytes);
//
//            for (int i = 0; i < toRetrieve.getNumPosting(); i++) {
//                byte[] tempDocBytes = new byte[4];
//                byte[] tempFreqBytes = new byte[4];
//                System.arraycopy(docBytes, i*4, tempDocBytes, 0 ,4);
//                System.arraycopy(freqBytes, i*4, tempFreqBytes, 0 ,4);
//
//                toRet.add(new Posting(Commons.bytesToInt(tempDocBytes), Commons.bytesToInt(tempFreqBytes)));
//            }
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//        return toRet;
//    }

    /**
     * Load the entire vocabulary from the disk
     */
    private void loadVocabulary() {
        vocabulary = new Vocabulary();

        String vocPath = WORK_DIR_PATH + "vocabularyBlock";
        try {
            FileChannel vocChannel = (FileChannel) Files.newByteChannel(Paths.get(vocPath),
                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

            for(long offset = 0; offset < vocChannel.size(); offset += TermInfo.SIZE_POST_MERGING){
                TermInfo nextTerm = getNextVoc(vocChannel, offset);
                vocabulary.set(nextTerm);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get next TermInfo from that channel
     * @param fileChannel FileChannel to retrieve the Term from
     * @param offsetVocabulary Offset at which the term is
     * @return Next TermInfo in line
     * @throws IOException
     */
    private TermInfo getNextVoc(FileChannel fileChannel, long offsetVocabulary) throws IOException{
        MappedByteBuffer tempBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offsetVocabulary, TermInfo.SIZE_POST_MERGING);

        byte[] termBytes = new byte[TermInfo.SIZE_TERM];
        tempBuffer.get(termBytes);

        int frequency = tempBuffer.getInt();
        long offset = tempBuffer.getLong();
        long docidBytes = tempBuffer.getLong();
        long freqBytes = tempBuffer.getLong();
        int nPosting = tempBuffer.getInt();

        String term = new String(termBytes).trim();

        return new TermInfo(term, frequency, offset, docidBytes, freqBytes, nPosting);
    }
}

/*
 * Tale classe si occupa di ricevere le query dall'utente, effettuare il preprocessing della stessa ed utilizzare i
 *  token generati per trovare i documenti migliori.
 *  L'approccio seguito e' Document At A Time.
 *
 * La ricerca di ogni termine nel vocabolario viene effettuata tramite ricerca binaria O(log n):
 *  si riceve un token, si guarda il termine presente a meta' dizionario: se il termine trovato ha ordine lessicografico
 *  maggiore del token, allora il termine per quel token si trova nella prima meta' del dizionario, altrimenti si trovera'
 *  nella seconda meta' del dizionario. Il processo e' ripetuto finche non si trova il termine o finche non si arriva alla
 *  condizione di stop (DA DEFINIRE), e quindi tale termine non e' presente nell'InvertedIndex.
 *
 * Il comportamento da tenere se un termine non esiste e' (DA DEFINIRE).
 *
 * Tale classe prevede l'aggiunta di una struttura di tipo cache per mantenere in memoria i TermInfo piu recenti
 *  Approccio simile a LIFO:
 *  - Prima di effettare la ricerca binaria, si controlla se tale termine e' presente nella cache
 *  - Se fosse presente, va utilizzato e rimesso "in cima"
 *  - Se non fosse presente, va effettuata la ricerca binaria e poi messo "in cima" alla cache
 *  Supponendo di avere una Coda di massimo 1000 elementi, all'aggiunta del 1001 esimo elemento, esso verra messo "in cima"
 *   mentre l'elemento ultimo sara' rimosso (l'ultimo elemento equivale a quello cercato meno frequentemente)
 *
 * Tale approccio permette di risparmiare il tempo di lettura da file dei termini, ma allo stesso tempo evita di tenere
 *  tutto il dizionario in memoria (utile nel caso si voglia utilizzare tale motore di ricerca su un dispositivo con poca
 *  memoria)
 *
 * Tale meccanismo di Cache puo essere esteso anche a query intere
 *  La Cache di cui discusso pocanzi, mantiene in memoria fino a X TermInfo
 *  La Cache di cui discusso ora, mantiene in memoria fino a Y coppie di <Lista di Token, Top K PID retrieved>
 *   Non ha senso calcolare di nuovo gli score di una query gia calcolata di recente da qualcun'altro
 */


