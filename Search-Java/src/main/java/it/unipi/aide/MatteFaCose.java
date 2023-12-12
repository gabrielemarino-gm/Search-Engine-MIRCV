package it.unipi.aide;

import it.unipi.aide.utils.ConfigReader;
import it.unipi.aide.utils.Preprocesser;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MatteFaCose {
    public static void main(String[] args){
        String[] sentences = new String[]
                {
                        "The quick brown fox jumps over the lazy dog",
                        "A quick brown rabbit hops over the lazy cat",
                        "Swift brown wolves leap across the drowsy feline",
                        "A speedy tan hare vaults over the lethargic kitty",
                        "Nimble beige rodents dash past the sluggish tabby",
                        "The fast red squirrel sprints beyond the snoozing kitten",
                        "Rapid ginger weasels dart over the dozing tomcat",
                        "A fleet maroon ferret races by the slumbering moggy",
                        "Quick russet minks scamper around the napping puss",
                        "Speedy copper martens zoom by the resting kitty"
                };

        Preprocesser preprocesser = new Preprocesser(true);
        for(String sentence : sentences) {
            for (String word : preprocesser.process(sentence))
                System.out.print(String.format("%s ",word));
        System.out.println();
        }
    }
}
