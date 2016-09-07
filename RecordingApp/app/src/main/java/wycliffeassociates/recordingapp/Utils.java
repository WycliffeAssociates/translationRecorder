package wycliffeassociates.recordingapp;

import java.io.File;

/**
 * Created by sarabiaj on 7/1/2016.
 */
public class Utils {
    private Utils() {
    }

    //http://stackoverflow.com/questions/13410949/how-to-delete-folder-from-internal-storage-in-android
    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    // http://stackoverflow.com/questions/5725892/how-to-capitalize-the-first-letter-of-word-in-a-string-using-java
    public static String capitalizeFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
