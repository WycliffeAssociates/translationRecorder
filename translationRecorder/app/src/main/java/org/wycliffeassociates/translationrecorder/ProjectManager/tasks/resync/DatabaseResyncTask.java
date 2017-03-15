package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import org.wycliffeassociates.translationrecorder.database.CorruptFileDialog;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.utilities.Task;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sarabiaj on 9/27/2016.
 */
public class DatabaseResyncTask extends Task implements ProjectDatabaseHelper.OnLanguageNotFound, ProjectDatabaseHelper.OnCorruptFile {
    Context mCtx;
    FragmentManager mFragmentManager;

    public DatabaseResyncTask(int taskId, Context ctx, FragmentManager fm){
        super(taskId);
        mCtx = ctx;
        mFragmentManager = fm;
    }

    public List<File> getAllTakes(){
        File root = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder");
        File[] dirs = root.listFiles();
        List<File> files = new LinkedList<>();
        if(dirs != null && dirs.length > 0) {
            for (File f : dirs) {
                files.addAll(getFilesInDirectory(f.listFiles()));
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
        db.resyncDbWithFs(getAllTakes(), this, this);
        db.close();
        onTaskCompleteDelegator();
    }

    public void onCorruptFile(File file) {
        CorruptFileDialog cfd = CorruptFileDialog.newInstance(file);
        cfd.show(mFragmentManager, "CORRUPT_FILE");
    }

    public String requestLanguageName(String code) {
        BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
        RequestLanguageNameDialog dialog = RequestLanguageNameDialog.newInstance(code, response);
        dialog.show(mFragmentManager,"REQUEST_LANGUAGE");
        try {
            return response.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "???";
    }
}
