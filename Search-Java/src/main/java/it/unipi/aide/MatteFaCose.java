package it.unipi.aide;

import it.unipi.aide.utils.beautify.MemoryDisplay;
import me.tongfei.progressbar.ProgressBar;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.swing.*;

public class MatteFaCose {

    public static void main(String[] args) {
        MemoryDisplay memoryDisplay = new MemoryDisplay();

        try
        {
            long ts = System.currentTimeMillis();
            while (ts + 10000 > System.currentTimeMillis()) {
                System.out.println("Doing...");
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        memoryDisplay.end();
    }
}
