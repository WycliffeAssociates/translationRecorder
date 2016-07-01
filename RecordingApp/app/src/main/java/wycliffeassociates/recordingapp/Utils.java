package wycliffeassociates.recordingapp;

import java.io.File;

/**
 * Created by sarabiaj on 7/1/2016.
 */
public class Utils {
    private Utils(){}

    //http://stackoverflow.com/questions/13410949/how-to-delete-folder-from-internal-storage-in-android
    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
}
