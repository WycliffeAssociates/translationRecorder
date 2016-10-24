package wycliffeassociates.recordingapp.ProjectManager.tasks;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.utilities.Task;

/**
 * Created by sarabiaj on 9/27/2016.
 */
public class DatabaseResyncTask extends Task {

    Context mCtx;

    public DatabaseResyncTask(int taskId, Context ctx){
        super(taskId);
        mCtx = ctx;
    }

    public List<File> getAllTakes(){
        File root = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder");
        File[] dirs = root.listFiles();
        List<File> files = new LinkedList<>();
        //files shouldn't be at this level, and the app currently could not handle adding them in this way.
        //skip the visualization folder
        if(dirs != null && dirs.length > 0) {
            for (File f : dirs) {
                if (f.isDirectory() && !f.getName().equals("Visualization")) {
                    files.addAll(getFilesInDirectory(f.listFiles()));
                }
            }
        }
        return files;
    }

    public List<File> getFilesInDirectory(File[] files){
        List<File> list = new LinkedList<>();
        for(File f : files){
            if(f.isDirectory()) {
                list.addAll(getFilesInDirectory(f.listFiles()));
            } else {
                list.add(f);
            }
        }
        return list;
    }

    @Override
    public void run() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        db.resyncDbWithFs(getAllTakes());
        db.close();
        onTaskCompleteDelegator();
    }
}
