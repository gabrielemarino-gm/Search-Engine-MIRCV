package it.unipi.aide.utils.beautify;

import com.sun.management.UnixOperatingSystemMXBean;
import it.unipi.aide.model.Cache;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

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

            frame.setFocusableWindowState(false);

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

                StringBuilder bricks = new StringBuilder();
                bricks.append("Memory usage:\n");
                bricks.append("\tUsed: ").append(formatBytes(used)).append("\n");
                bricks.append("\tMax: ").append(formatBytes(max)).append("\n");
                bricks.append("--------------------------------------\n");
                bricks.append("L1: ").append(c.getL1Used()).append("/").append(c.getL1Max()).append("\n");
                bricks.append("L2: ").append(c.getL2Used()).append("/").append(c.getL2Max()).append("\n");
                bricks.append("L3: ").append(c.getL3Used()).append("/").append(c.getL3Max()).append("\n");

                bricks.append("\n\n");

                OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
                if(os instanceof UnixOperatingSystemMXBean){
                    bricks.append("Open fd: ").append(((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount());
                }

                updateTextArea(bricks.toString());

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
