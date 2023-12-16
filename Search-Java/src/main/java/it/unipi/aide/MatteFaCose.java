package it.unipi.aide;

import it.unipi.aide.utils.ConfigReader;

public class MatteFaCose {

    public static void main(String[] args) {
//        MemoryDisplay memoryDisplay = new MemoryDisplay();
//
//        try
//        {
//            long ts = System.currentTimeMillis();
//            while (ts + 10000 > System.currentTimeMillis()) {
//                System.out.println("Doing...");
//                Thread.sleep(1000);
//            }
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//
//        memoryDisplay.end();

        ConfigReader.setCompression(true);
        ConfigReader.setStemming(false);
    }
}
