package it.unipi.aide.utils.beautify;

import it.unipi.aide.model.Cache;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.swing.*;

public class MemoryDisplay extends Thread {
        private static JTextArea textArea;
        private static JFrame frame;
        private boolean running = true;

        public MemoryDisplay() {
            super("Memory Display");
            SwingUtilities.invokeLater(MemoryDisplay::createAndShowGUI);

            // Create and start the memory monitoring thread
            this.start();
        }

        private static void createAndShowGUI() {
            frame = new JFrame("Memory Display");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            textArea = new JTextArea();
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            frame.getContentPane().add(scrollPane);

            frame.setVisible(true);
        }

        public void end() {
            running = false;
            SwingUtilities.invokeLater(() -> {
                frame.dispose();
            });
        }

        @Override
        public void run() {
            Cache c = Cache.getCacheInstance();
            while(running) {
                long max = Runtime.getRuntime().maxMemory();
                long used = max - Runtime.getRuntime().freeMemory();

                String message = "Memory Usage:\n" +
                            "  Used: " + formatBytes(used) + "\n" +
                         "  Max: " + formatBytes(max) + "\n" +
                        "--------------------------------------\n" +
                        "L1: " + c.getL1Used() + "/" + c.getL1Max() + "\n" +
                        "L2: " + c.getL2Used() + "/" + c.getL2Max() + "\n" +
                        "L3: " + c.getL3Used() + "/" + c.getL3Max() + "\n";

                updateTextArea(message);

                try {
                    Thread.sleep(1000); // Adjust the sleep duration as needed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        private static void updateTextArea(String message) {
            SwingUtilities.invokeLater(() -> textArea.setText(message));
        }

        private static String formatBytes(long bytes) {
            long kilobytes = bytes / 1024;
            long megabytes = kilobytes / 1024;
            long gigabytes = megabytes / 1024;

            if (gigabytes > 0) {
                return gigabytes + " GB";
            } else if (megabytes > 0) {
                return megabytes + " MB";
            } else if (kilobytes > 0) {
                return kilobytes + " KB";
            } else {
                return bytes + " B";
            }
        }
}
