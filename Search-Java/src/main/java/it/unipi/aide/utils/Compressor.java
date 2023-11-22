package it.unipi.aide.utils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Compressor {

    /**
     * Compress an array of bytes in an array of Unary Compressed bytes
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
     * Decompress an Array of bytes into an Array of Integers, using Unary Decompression
     * @param fromBytes Array of bytes to decompress
     * @return Array of integer decompressed
     */
    public static int[] UnaryDecompression(byte[] fromBytes, int qty){
        List<Integer> toRet = new ArrayList<>();
        int counter = 1;

        // Shift to the right: when we encounter a 0 on the second place we print
        // Otherwise, we accumulate on an accumulator
        for(int i = 0; i < fromBytes.length * 8; i++)
        {
            if ((fromBytes[fromBytes.length - 1] & 0b00000010) == 0b00000000 ||
                    ((fromBytes[fromBytes.length - 1] & 0b00000011) == 0b00000011) && i == fromBytes.length*8 - 2)
            {
                toRet.add(counter);
                counter = 1;
            }
            else
            {
                counter += 1;
            }
            shiftRight(fromBytes);
        }

        toRet.add(counter);

        // We want to return an Array, not a List
        int[] temp = new int[qty];
        for(int i = 0; i < qty; i++)
        {
            temp[qty-1-i] = toRet.get(i);
        }
        return temp;
    }

    /**
     * Compress an Array of bytes into an Array of Variable Byte compressed numbers
     * @param fromBytes Array of bytes to compress
     * @return Array of bytes representing the compression
     */
    public static byte[] VariableByteCompression(byte[] fromBytes) {

        int[] numbers = toIntArray(fromBytes);
        List<Byte> compressedBytes = new ArrayList<>();

        for (int number : numbers) {
            byte[] compressed = compressIntToVariableByte(number);
            for (byte b : compressed) {
                compressedBytes.add(b);
            }
        }

        byte[] byteArray = new byte[compressedBytes.size()];
        for (int i = 0; i < compressedBytes.size(); i++) {
            byteArray[i] = compressedBytes.get(i);
        }

        return byteArray;
    }

    /**
     * Compress a single integer number using Variable Byte representation
     * @param number integer to convert
     * @return Array of bytes forming the conversion
     */
    public static byte[] compressIntToVariableByte(int number) {

        ArrayList<Byte> compressedBytes = new ArrayList<>();

        while (number >= 128) {
            compressedBytes.add((byte) (number % 128 + 128));
            number /= 128;
        }
        compressedBytes.add((byte) number);

        byte[] result = new byte[compressedBytes.size()];
        for (int i = 0; i < compressedBytes.size(); i++) {
            result[i] = compressedBytes.get(i);
        }

        return result;
    }

    /**
     * Decompress an Array of bytes into an array of integer, using Variable Byte Decompression
     * @param bytes Array of Bytes to convert
     * @return Array of integers converted
     */
    public static int[] VariableByteDecompression(byte[] bytes) {
        List<Integer> integers = new ArrayList<>();
        int index = 0;

        while (index < bytes.length) {
            int value = 0;
            int shift = 0;

            while ((bytes[index] & 0x80) != 0) {
                value |= (bytes[index] & 0x7F) << shift;
                shift += 7;
                index++;
            }

            value |= bytes[index] << shift;
            integers.add(value);
            index++;
        }

        int[] result = new int[integers.size()];
        for (int i = 0; i < integers.size(); i++) {
            result[i] = integers.get(i);
        }

        return result;
    }

    /**
     * Support function to convert an Array of 4 bytes values, into an Array of integers
     * @param list Byte Array with size 4*N elements
     * @return Integers Array of E elements
     */
    private static int[] toIntArray(byte[] list) {
        int[] ints = new int[list.length / 4];
        for (int j = 0; j < list.length; j += 4) {
            int value = 0;
            for (int i = 0; i < 4; i++) {
                value = (value << 8) | (list[j + i] & 0xFF);
            }
            ints[j / 4] = value;
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
            b += n;
        }
        return ((b - (b % 8)) + ((b % 8 == 0)? 0 : 8)) / 8;
    }

    /**
     * Support function to shift left an array of bytes as it was one
     * @param arr Array of Bytes to shift
     */
    private static void shiftLeft(byte[] arr){
        for(int i = 0; i < arr.length - 1; i++){
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

/*
 * Tale classe implementa i vari metodi di compressione utilizzati in Merging.Java, e varie funzioni di utilita
 *
 * La compressione Unaria e' fatta creando un "mega array" di tanti byte quanti sono necessari a comprimere tutti i numeri
 *  dopodiche, vengono shiftati verso sinistra 1 e 0 a seconda della rappresentazione unaria del numero sotto analisi
 * La decompressione Unaria e' fatta in maniera analoga shiftando verso destra e contando gli 1 e 0 per determinare
 *  il valore del numero compresso
 *
 * La compressione VariableByte e' da farsi
 * La decompressione VariableByte e' da farsi
 */
