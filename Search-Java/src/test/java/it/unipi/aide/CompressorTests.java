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
    public void unaryCompressionSetUp(){

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
    public void unaryCompression(){
        out1[0] = Compressor.UnaryCompression(in1[0]);
        assertArrayEquals(out1[0], new byte[] {(byte)0b00000110, (byte)0b11110100});
        // 1 2 1 1 2 1 1 2 1 1 1 1 1
        out1[1] = Compressor.UnaryCompression(in1[1]);
        assertArrayEquals(out1[1], new byte[] {(byte)0b01000100, (byte)0b01000000});
    }

    @Test
    public void unaryDecompression(){
        out2[0] = Compressor.UnaryDecompression(in2[0], in2[0].length); //todo tocheck
        out2[1] = Compressor.UnaryDecompression(in2[1], in2[1].length);
        assertArrayEquals(out2[0], new int[] {3,3,3,4,1,1});
        assertArrayEquals(out2[1], new int[] {1,2,3,4,1,4,1,1,1,5});
    }

    @Before
    public void variableByteCompressionSetUp(){
        in1 = new byte[][]{
                {
                        (byte) 0X00, (byte) 0X01, (byte) 0X08, (byte) 0XEE,
                        (byte) 0X00, (byte) 0X12, (byte) 0X05, (byte) 0X05,
                        (byte) 0X10, (byte) 0X05, (byte) 0X08, (byte) 0X02,
                        (byte) 0X00, (byte) 0X00, (byte) 0X01, (byte) 0X00,
                },
                {
                        (byte) 0X24, (byte) 0X1A, (byte) 0x0D, (byte) 0X05,
                        (byte) 0X12, (byte) 0X45, (byte) 0X28, (byte) 0X13,
                        (byte) 0X10, (byte) 0X30, (byte) 0X11, (byte) 0x00,
                        (byte) 0X55, (byte) 0X00, (byte) 0XFF, (byte) 0XD0
                }
        };

        in2 = new byte[][]{
                {
                        (byte) 0X87, (byte) 0X9A, (byte) 0XF8, (byte) 0XA0, (byte) 0X02,
                        (byte) 0X97, (byte) 0XD0, (byte) 0X9C, (byte) 0X92, (byte) 0X01,
                        (byte) 0X90, (byte) 0XA2, (byte) 0XC0, (byte) 0X81, (byte) 0X01
                }
        };
    }

    @Test
    public void variableByteCompression() {
        out1[0] = Compressor.VariableByteCompression(in1[0]);
        assertArrayEquals(out1[0], new byte[] {
                (byte) 238, (byte) 145, (byte) 4,
                (byte) 133, (byte) 138, (byte) 72,
                (byte) 130, (byte) 144, (byte) 148, (byte) 128, (byte) 1,
                (byte) 128, (byte) 2
        });

        out1[1] = Compressor.VariableByteCompression(in1[1]);
        assertArrayEquals(out1[1], new byte[] {
                (byte) 133, (byte) 154, (byte) 232, (byte) 160, (byte) 2,
                (byte) 147, (byte) 208, (byte) 148, (byte) 146, (byte) 1,
                (byte) 128, (byte) 162, (byte) 192, (byte) 129, (byte) 1,
                (byte) 208, (byte) 255, (byte) 131, (byte) 168, (byte) 5,
        });
    }

    @Test
    public void variableByteDecompression(){
        out2[0] = Compressor.VariableByteDecompression(out1[0]);
        assertArrayEquals(out2[0], new int[] {
                67822,
                1180933,
                268765186,
                256
        });

        out2[1] = Compressor.VariableByteDecompression(in2[0]);
        assertArrayEquals(out2[1], new int[] {
                605949191,
                306653207,
                271585552
        });
    }
}
