package it.unipi.aide.utils;

/**
 * This class is used to check, create and delete files and directories
 * along the whole project without implementing the wheel
 */
public class FileManager {

    /**
    * Check if a directory exists
    * @param path Path of the directory to check
    * @return true if exists, false otherwise
    */
    public static boolean checkDir(String path){
        return true;
    }

    /**
     * Create a directory
     * @param path Path in which directory should be created
     * @return true upon success, false otherwise
     */
    public static boolean createDir(String path){

        return true;
    }

    /**
     * Delete a directory
     * @param path Path of the directory to remove
     * @return true upon success, false otherwise
     */
    public static boolean deleteDir(String path){

        return true;
    }

    /**
     * Check if a file exists
     * @param path Path of the file to check
     * @return true if exists, false otherwise
     */
    public static boolean checkFile(String path){

        return true;
    }

    /**
     * Create a file (touch)
     * @param path Path at witch create the file
     * @return true upon success, false otherwise
     */
    public static boolean createFile(String path){

        return true;
    }


    /**
     * Removes a file
     * @param path Path of the file to remove
     * @return true upon success, false otherwise
     */
    public static boolean removeFile(String path){

        return true;
    }
}
