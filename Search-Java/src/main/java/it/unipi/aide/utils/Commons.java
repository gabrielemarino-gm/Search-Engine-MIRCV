package it.unipi.aide.utils;

/**
 * This class contains common utilities function that can be used
 * from any other class
 */
public class Commons
{
    /**
     * Convert 4 bytes into an integer
     * @param b 4-bytes array
     * @return Integer representation
     */
    public static int bytesToInt(byte[] b){
        int toRet = 0;
        for(int i = 0; i < 4; i++){
            toRet = (toRet << 8) | (b[i] & 0xFF);
        }
        return toRet;
    }

    /**
     * Convert an array of bytes into integers
     * @param i Integer to convert
     * @return 4-bytes array
     */
    public static int[] bytesToIntArray(byte[] i){
        int[] toRet = new int[i.length/4];

        for(int j = 0; j < i.length; j += 4){
            toRet[j/4] = bytesToInt(new byte[]{i[j], i[j+1], i[j+2], i[j+3]});
        }
        return toRet;
    }

}
