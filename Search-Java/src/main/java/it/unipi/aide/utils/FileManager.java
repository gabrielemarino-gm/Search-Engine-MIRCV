package it.unipi.aide.utils;

import java.io.File;

/**
 * This class is used to check, create and delete files and directories
 * along the whole project without implementing the wheel
 */
public class FileManager
{
    /**
    * Check if a directory exists
    * @param path Path of the directory to check
    * @return true if exists, false otherwise
    */
    public static boolean checkDir(String path)
    {
        File directory = new File(path);
        return directory.exists() && directory.isDirectory();
    }

    /**
     * Create a directory
     * @param path Path in which directory should be created
     * @return true upon success, false otherwise
     */
    public static boolean createDir(String path)
    {
        if (!checkDir(path))
        {
            File directory = new File(path);
            return directory.mkdirs();
        }

        return false;
    }

    /**
     * Delete a directory and all content
     * @param path Path of the directory to remove
     * @return true upon success, false otherwise
     */
    public static boolean deleteDir(String path)
    {
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory())
        {
            // Delete the directory and its contents
            String[] entries = directory.list();

            if (entries != null)
            {
                for (String entry : entries)
                {
                    File entryFile = new File(directory, entry);
                    if (entryFile.isDirectory())
                    {
                        // Recursively delete subdirectories
                        deleteDir(entryFile.getAbsolutePath());
                    }
                    else
                    {
                        // Delete files
                        entryFile.delete();
                    }
                }
            }

            // Delete the directory itself
            return directory.delete();
        }

        return false;
    }

    /**
     * Check if a file exists
     * @param path Path of the file to check
     * @return true if exists, false otherwise
     */
    public static boolean checkFile(String path)
    {
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * Create a file (touch)
     * @param path Path at witch create the file
     * @return true upon success, false otherwise
     */
    public static boolean createFile(String path)
    {
        File file = new File(path);

        try
        {
            if(!checkDir(file.getParent())){
                createDir(file.getParent());
            }
            return file.createNewFile();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a file
     * @param path Path of the file to remove
     * @return true upon success, false otherwise
     */
    public static boolean removeFile(String path)
    {
        File file = new File(path);
        if (file.exists() && file.isFile())
        {
            return file.delete();
        }
        return false;
    }

    /**
     * Clear all the content from a folder
     * @param path Path of the folder to clear
     */
    public static void cleanFolder(String path)
    {
        File directory = new File(path);

        String[] entries = directory.list();
        if (entries != null)
        {
            for (String entry : entries)
            {
                File entryFile = new File(directory, entry);
                if (entryFile.isDirectory())
                {
                    // Recursively delete subdirectories
                    deleteDir(entryFile.getAbsolutePath());
                }
                else
                {
                    // Delete files
                    entryFile.delete();
                }
            }
        }
    }
}
