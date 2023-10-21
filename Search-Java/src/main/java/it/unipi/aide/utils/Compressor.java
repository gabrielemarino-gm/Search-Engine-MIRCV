package it.unipi.aide.utils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Compressor {

    /**
     * Converts an array of bytes in an array of Unary Compressed bytes
     * @param fromBytes Array of bytes to compress
     * @return Array of bytes representing the compressed numbers
     */
    public static byte[] UnaryCompression(byte[] fromBytes){
        // In Merging, we directly use arrays of Bytes
        // In order to use Unary, we have to temporary switch back to integers
        int[] ints = toIntArray(fromBytes);
        int bytesNeeded = bytesNeeded(ints);
        byte[] toBytes = new byte[bytesNeeded];
        // Pushes from the left to the right of the array
        // n-1 ones and 1 zero for every number n
        for(int j = 0; j < ints.length; j++){
            for(int i = ints[j]-1; i >= 0; i--){
                if(i>0) toBytes[bytesNeeded-1] = (byte)(toBytes[bytesNeeded-1] | 0x01);
                if(!(j==ints.length-1 && i == 0)) shiftLeft(toBytes);
            }
        }
        return toBytes;
    }

    /**
     * Converts an Array of bytes into an Array of Integers, using Unary Decompression
     * @param fromBytes Array of bytes to decompress
     * @return Array of integer decompressed
     */
    public static int[] UnaryDecompression(byte[] fromBytes){
        List<Integer> toRet = new ArrayList<>();
        int counter = 1;

        // Shift to the right: when we encounter a 0 on the second place we print
        // Otherwise, we accumulate on an accumulator
        for(int i = 0; i < fromBytes.length*8-1; i++) {
            if ((fromBytes[fromBytes.length - 1] & 0b00000010) == 0b00000000 ||
                    ((fromBytes[fromBytes.length - 1] & 0b00000011) == 0b00000011) && i == fromBytes.length*8-2)
            {
                toRet.add(counter);
                counter = 1;
            } else {
                counter += 1;
            }
            shiftRight(fromBytes);
        }

        // We want to return an Array, not a List
        int[] temp = new int[toRet.size()];
        for(int i = 0; i < toRet.size(); i++)
            {
                temp[i] = toRet.get(i);
            }
        return temp;
    }

    /**
     * Support function to convert an Array of 4 bytes values, into an Array of integers
     * @param list Byte Array with size 4*N elements
     * @return Integers Array of E elements
     */
    private static int[] toIntArray(byte[] list){
        int[] ints = new int[list.length/4];
        for(int j = 0; j < list.length; j += 4){
            for(int i = 0; i < 4; i++){
                ints[j/4] = list[i+j];
            }
        }
        return ints;
    }

    /**
     * Calculate how many bytes are needed to store an Array of Integers as Unary
     * @param ints Array of integer to analyze
     * @return Bytes needed
     */
    private static int bytesNeeded(int[] ints){
        int b = 0;
        for(int n : ints){
            b+=n;
        }
        return ((b - (b % 8)) + ((b % 8 == 0)? 0 : 8)) / 8;
    }

    /**
     * Support function to shift left an array of bytes as it was one
     * @param arr Array of Bytes to shift
     */
    private static void shiftLeft(byte[] arr){
        for(int i = 0; i < arr.length -1; i++){
            arr[i] = (byte)((arr[i] & 0xFF) << 1);
            byte ow = (byte)((arr[i+1] & 0x80) >>> 7);
            arr[i] = (byte)(arr[i] | ow);
        }
        arr[arr.length-1] = (byte)(arr[arr.length-1] << 1);
    }

    /**
     * Support function to shift right an array of bytes as it was one
     * @param arr Array of Bytes to shift
     */
    private static void shiftRight(byte[] arr){
        for(int i = arr.length - 1; i > 0; i--){
            arr[i] = (byte)((arr[i] & 0xFF) >>> 1);
            byte ow = (byte)(arr[i - 1] & 0x01);
            ow = (byte)((ow & 0xFF) << 7);
            arr[i] = (byte)(arr[i] | ow);
        }
        arr[0] = (byte)(arr[0] >>> 1);
    }
}
