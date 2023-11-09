package it.unipi.aide.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

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
                br = new BufferedReader(new FileReader(INPUT_PATH));
            }
            catch (FileNotFoundException e)
            {
                System.err.println("Input File Not Found");
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
