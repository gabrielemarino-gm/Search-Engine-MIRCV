package it.unipi.aide.model;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static it.unipi.aide.utils.beautify.ColorText.ANSI_RESET;
import static it.unipi.aide.utils.beautify.ColorText.RED;

/**
 * This class represents a Corpus
 * During the creation of the object, a BufferedReader is opened towards a specified path
 * The class implements the Iterator interface, and through the correct use in the for-each, you can scroll
 * the lines of the document to be processed as if it were an array of strings
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
                if(INPUT_PATH == null)
                    throw new FileNotFoundException();

                if(INPUT_PATH.contains(".tsv"))
                {
                    br = new BufferedReader(new FileReader(INPUT_PATH));
                }
                else if (INPUT_PATH.contains(".tar.gz"))
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
            try
            {
                if(br.ready())
                {
                    return true;
                }
                else
                {
                    br.close();
                    return false;
                }
            }
            catch (IOException | NullPointerException e)
            {
                return false;
            }
        }

        @Override
        public String next() {
            try {
                return br.readLine();
            } catch (IOException e) {
                return null;
            }
        }
    }
}
