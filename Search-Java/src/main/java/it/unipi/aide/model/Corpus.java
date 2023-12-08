package it.unipi.aide.model;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * This class represents a Corpus
 * During the creation of the object, a BufferedReader is opened towards a specified path
 * The class implements the Iterator interface, and through the correct use in the for-each, you can scroll
 * the lines of the document to be processed as if it were an array of strings
 *
 * the next() function will simply return the next line, as long as hasNext() is true.
 */
public class Corpus implements Iterable<String>
{
    private String INPUT_PATH;

    public Corpus(String in_path) {
        INPUT_PATH = in_path;
    }

    @Override
    public Iterator<String> iterator()
    {
        return new DocIterator();
    }

    private class DocIterator implements Iterator<String>
    {
        private BufferedReader br = null;

        private DocIterator()
        {
            try
            {
                if(INPUT_PATH.contains(".tsv"))
                {
                    br = new BufferedReader(new FileReader(INPUT_PATH));
                }
                else
                {
                    TarArchiveInputStream tarInput = new TarArchiveInputStream(
                            new GzipCompressorInputStream(
                                    new FileInputStream(INPUT_PATH)));

                            tarInput.getNextTarEntry();
                            br = new BufferedReader(new InputStreamReader(tarInput, StandardCharsets.UTF_8));
                }
            }
            catch (FileNotFoundException e)
            {
                System.err.println("Input File Not Found");
                System.exit(1);
            }
            catch (IOException ioe)
            {
                System.err.println("Error while reading the input file");
                System.exit(1);
            }
        }

        @Override
        public boolean hasNext()
        {
            boolean toRet = false;

            try
            {
                toRet = br.ready();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return toRet;
        }

        @Override
        public String next()
        {
            String toRet = null;

            try
            {
                toRet = br.readLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return toRet;
        }
    }
}

/*
 * Tale classe rappresenta un Corpus
 *
 * In fase di creazione viene aperto un BufferReader verso un path specificato
 *  La classe quindi implementa l'interfaccia Iterator, e tramite il corretto uso nei for-each, si possono scorrere
 *  le righe del documento da processare come se fosse un array di stringhe
 *
 * la funzione next() restituira semplicemente la prossima riga, finche hasNext() e' true.
 */
