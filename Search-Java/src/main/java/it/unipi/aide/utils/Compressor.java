package it.unipi.aide.utils;

import java.util.ArrayList;
import java.util.List;

public class Compressor {

    /**
     * Compress an array of bytes in an array of Unary Compressed bytes
     * @param fromBytes Array of bytes to compress
     * @return Array of bytes representing the compressed numbers
     */
        public static byte[] UnaryCompression(byte[] fromBytes) {
            // In Merging, we directly use arrays of Bytes
            // In order to use Unary, we have to temporary switch back to integers
            int[] ints = toIntArray(fromBytes);
            int bytesNeeded = bytesNeeded(ints);

            byte[] toRet = new byte[bytesNeeded];
            int bitCount = 0;
            byte currentByte = 0;
            int idx = 0;

            for (int num : ints) {
                for (int i = 0; i < num - 1; i++) {
                    currentByte = setBit(currentByte, bitCount);
                    bitCount++;

                    if (bitCount == 8) {
                        toRet[idx] = currentByte;
                        currentByte = 0;
                        bitCount = 0;
                        idx ++;
                    }
                }

                // Add a 0 bit to separate numbers
                bitCount++;
                if (bitCount == 8) {
                    toRet[idx] = currentByte;
                    currentByte = 0;
                    bitCount = 0;
                    idx ++;
                }
            }

            // Add the last byte if it contains data
            if (idx < bytesNeeded)
                toRet[idx] = currentByte;

            return toRet;
        }

    /**
     * Decompress an Array of bytes into an Array of Integers, using Unary Decompression
     * @param compressed Array of bytes to decompress
     * @param qty Number of integers to decompress
     * @return Array of integer decompressed
     */
        public static int[] UnaryDecompression(byte[] compressed, int qty)
        {
            int[] toRet = new int[qty];
            int idx = 0;
            int count = 1;

            boolean inNumber = false;

            for (byte b : compressed) {
                for (int i = 0; i < 8; i++){
                    boolean isSet = (b & (1 << i)) != 0;
                    if (isSet) {
                        count++;
                    }
                    else
                    {
                        toRet[idx] = count;
                        count = 1;
                        idx++;
                        if(idx == qty)
                            return toRet;
                    }
                }
            }

            return toRet;
        }

        // Helper method to set a bit at a specific position
        private static byte setBit(byte num, int pos) {
            return (byte) (num | (1 << pos));
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
