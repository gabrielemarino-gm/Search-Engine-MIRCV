package it.unipi.aide;

import it.unipi.aide.utils.Commons;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommonsTests {

    byte[][] arr;

    @Before
    public void setUp(){
        arr = new byte[][] {
                {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xAF},
                {(byte) 0x0F, (byte) 0xC0, (byte) 0x00, (byte) 0xC0},
                {(byte) 0x2F, (byte) 0x01, (byte) 0x22, (byte) 0x0F}
        };
    }

    @Test
    public void byteConversion(){
        assertEquals(Commons.bytesToInt(arr[0]), 175);
        assertEquals(Commons.bytesToInt(arr[1]), 264241344);
        assertEquals(Commons.bytesToInt(arr[2]), 788603407);
    }
}
