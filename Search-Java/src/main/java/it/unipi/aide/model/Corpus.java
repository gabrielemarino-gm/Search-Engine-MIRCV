package it.unipi.aide.model;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static it.unipi.aide.utils.ColorText.ANSI_RESET;
import static it.unipi.aide.utils.ColorText.RED;

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
    private final String INPUT_PATH;
    public Corpus(String in_path) { INPUT_PATH = in_path; }

    @Override
    public Iterator<String> iterator() { return new DocIterator(); }

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
                System.out.println(RED + "SPIMI ERROR > Input File Not Found" + ANSI_RESET);
            }
            catch (IOException ioe)
            {
                System.out.println(RED + "SPIMI ERROR > Error while reading the input file" + ANSI_RESET);
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
            catch (IOException | NullPointerException e)
            {
                // e.printStackTrace();
                return false;
            }
            return toRet;
        }

        @Override
        public String next() {
            String toRet = null;

            try {
                toRet = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return toRet;
        }
    }
}
