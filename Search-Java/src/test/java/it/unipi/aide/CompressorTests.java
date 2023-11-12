package it.unipi.aide;

import it.unipi.aide.utils.Compressor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CompressorTests {

    byte[][] in1;
    byte[][] out1 = new byte[10][];
    byte[][] in2;
    int[][] out2 = new int[10][];

    @Before
    public void setUp(){

        in1 = new byte[][]{
                {   // 3 5 2 1
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X03,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X05,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X02,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                },
                {   // 1 2 1 1 2 1 1 2 1 1 1 1 1
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X02,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X02,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X02,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01,
                    (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X01
                }
        };

        in2 = new byte[][]{
                {(byte) 0b00011101, (byte) 0b10110110}, // {3,3,3,4,1,1}
                {(byte) 0b11111000, (byte) 0b01110011, (byte) 0b10110100}, // {1,2,3,4,1,4,1,1,1,5}
                {(byte) 0b01000100, (byte) 0b01000000}, // 1 2 1 1 2 1 1 2 1 1 1 1 1
        };


    }

    @Test
    public void compression(){
        out1[0] = Compressor.UnaryCompression(in1[0]);
        assertArrayEquals(out1[0], new byte[] {(byte)0b00000110, (byte)0b11110100});
        // 1 2 1 1 2 1 1 2 1 1 1 1 1
        out1[1] = Compressor.UnaryCompression(in1[1]);
        assertArrayEquals(out1[1], new byte[] {(byte)0b01000100, (byte)0b01000000});
    }

    @Test
    public void decompression(){
        out2[0] = Compressor.UnaryDecompression(in2[0], 7);
        out2[1] = Compressor.UnaryDecompression(in2[1], 9);
        out2[2] = Compressor.UnaryDecompression(in2[2], 13);
        assertArrayEquals(out2[0], new int[] {1,1,1,4,3,3,3});
        assertArrayEquals(out2[1], new int[] {1,1,1,4,1,4,3,2,1});
        assertArrayEquals(out2[2], new int[] {1,2,1,1,2,1,1,2,1,1,1,1,1});
    }
}
