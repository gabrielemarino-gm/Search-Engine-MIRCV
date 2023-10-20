package it.unipi.aide.utils;

/**
 * This class contains common utilities function that can be used
 * from any other class
 */
public class Commons {

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


}
