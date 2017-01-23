package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sarabiaj on 1/23/2017.
 */

public class ResyncUtils {
    private ResyncUtils(){}

    public static List<File> getAllTakes(File root){
        File[] dirs = root.listFiles();
        List<File> files = new LinkedList<>();
        if(dirs != null && dirs.length > 0) {
            for (File f : dirs) {
                files.addAll(getFilesInDirectory(f.listFiles()));
            }
        }
        return files;
    }

    public static  List<File> getFilesInDirectory(File[] files){
        List<File> list = new LinkedList<>();
        if(files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    list.addAll(getFilesInDirectory(f.listFiles()));
                } else {
                    list.add(f);
                }
            }
        }
        return list;
    }
}
