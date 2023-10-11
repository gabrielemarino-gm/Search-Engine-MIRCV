package it.unipi.aide;

import it.unipi.aide.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        boolean DEBUG = false;
        String MODE = "TFIDF";

        String testPath = System.getProperty("user.dir");
        System.out.println("DBG:    Dir = " + testPath);

        try
        {
            System.out.println("DBG:    file path: " + testPath + "src/main/java/it/unipi/aide/config/prova.txt");
            File file = new File(testPath + "/src/main/java/it/unipi/aide/config/prova.txt");

            if (file.exists())
                System.out.println("DBG:    File Esiste");
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        

        Preprocesser pTest = new Preprocesser(true);
    }

    public static String readTextFile(String path)
    {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                stringBuilder.append(line).append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
