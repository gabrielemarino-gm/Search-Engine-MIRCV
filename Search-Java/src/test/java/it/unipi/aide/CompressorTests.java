package it.unipi.aide;

import it.unipi.aide.utils.Compressor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CompressorTests {

    byte[][] unaryCompIn;
    byte[][] unaryDecompIn;
    byte[][] variableCompIn;
    byte[][] variableDecompIn;

    @Before
    public void unaryCompressionSetUp(){

        unaryCompIn = new byte[][]{
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

        unaryDecompIn = new byte[][]{
                {(byte) 0b00011101, (byte) 0b10110110}, // {1,1,1,4,3,3,3}
                {(byte) 0b11111000, (byte) 0b01110011, (byte) 0b10110100}, // {1,2,3,4,1,4,1,1,1,5}
                {(byte) 0b01000100, (byte) 0b01000000}, // 1 2 1 1 2 1 1 2 1 1 1 1 1
        };

    }

    @Test
    public void unaryCompression(){
        byte[] outBytes;
        outBytes = Compressor.UnaryCompression(unaryCompIn[0]);
        assertArrayEquals(outBytes, new byte[] {(byte)0b01111011, (byte)0b00000001});

        outBytes = Compressor.UnaryCompression(unaryCompIn[1]);
        assertArrayEquals(outBytes, new byte[] {(byte)0b00100010, (byte)0b00000010});

    }

    @Test
    public void unaryDecompression(){
        int[] outInts;

        outInts = Compressor.UnaryDecompression(unaryDecompIn[0], 5);
        assertArrayEquals(outInts, new int[] {1,4,3,3,3});

        outInts = Compressor.UnaryDecompression(unaryDecompIn[1], 6);
        assertArrayEquals(outInts, new int[] {4,1,4,3,2,1});

        outInts = Compressor.UnaryDecompression(unaryDecompIn[2], 8);
        assertArrayEquals(outInts, new int[] {1,1,2,1,1,1,1,1});
    }

    @Before
    public void variableByteCompressionSetUp(){
        variableCompIn = new byte[][]{
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

        variableDecompIn = new byte[][]{
                {
                        (byte) 0X87, (byte) 0X9A, (byte) 0XF8, (byte) 0XA0, (byte) 0X02,
                        (byte) 0X97, (byte) 0XD0, (byte) 0X9C, (byte) 0X92, (byte) 0X01,
                        (byte) 0X90, (byte) 0XA2, (byte) 0XC0, (byte) 0X81, (byte) 0X01
                }
        };
    }

    @Test
    public void variableByteCompression() {
        byte[] outBytes;
        outBytes = Compressor.VariableByteCompression(variableCompIn[0]);
        assertArrayEquals(outBytes, new byte[] {
                (byte) 238, (byte) 145, (byte) 4,
                (byte) 133, (byte) 138, (byte) 72,
                (byte) 130, (byte) 144, (byte) 148, (byte) 128, (byte) 1,
                (byte) 128, (byte) 2
        });

        outBytes = Compressor.VariableByteCompression(variableCompIn[1]);
        assertArrayEquals(outBytes, new byte[] {
                (byte) 133, (byte) 154, (byte) 232, (byte) 160, (byte) 2,
                (byte) 147, (byte) 208, (byte) 148, (byte) 146, (byte) 1,
                (byte) 128, (byte) 162, (byte) 192, (byte) 129, (byte) 1,
                (byte) 208, (byte) 255, (byte) 131, (byte) 168, (byte) 5,
        });
    }

    @Test
    public void variableByteDecompression(){
        int[] outInts;
        outInts = Compressor.VariableByteDecompression(variableDecompIn[0]);
        assertArrayEquals(outInts, new int[] {
                605949191,
                306653207,
                271585552
        });
    }
}
