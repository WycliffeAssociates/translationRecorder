package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.FilesPage.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.utilities.Task;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sarabiaj on 1/20/2017.
 */

public class UnitResyncTask extends Task implements ProjectDatabaseHelper.OnLanguageNotFound {
    private final Project mProject;
    Context mCtx;
    FragmentManager mFragmentManager;
    private int mChapter;

    public UnitResyncTask(int taskId, Context ctx, FragmentManager fm, Project project, int chapter){
        super(taskId);
        mCtx = ctx;
        mFragmentManager = fm;
        mProject = project;
        mChapter = chapter;
    }

    public List<File> getAllTakes(){
        File root = new File(Environment.getExternalStorageDirectory(),
                "TranslationRecorder/" + mProject.getTargetLanguage() + "/" + mProject.getVersion() + "/" + mProject.getSlug() + "/" + FileNameExtractor.chapterIntToString(mProject, mChapter) + "/");
        File[] dirs = root.listFiles();
        List<File> files = new LinkedList<>(Arrays.asList(dirs));

//        if(dirs != null && dirs.length > 0) {
//
////old from when the entire project was resynced at the project level
////            for (File f : dirs) {
////                if (f.isDirectory()) {
////                    files.addAll(getFilesInDirectory(f.listFiles()));
////                }
////            }
//        }
        return files;
    }

//    public List<File> getFilesInDirectory(File[] files){
//        List<File> list = new LinkedList<>();
//        for(File f : files){
//            if(f.isDirectory()) {
//                list.addAll(getFilesInDirectory(f.listFiles()));
//            } else {
//                list.add(f);
//            }
//        }
//        return list;
//    }

    @Override
    public void run() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        db.resyncChapterWithFilesystem(mProject, mChapter, getAllTakes(), this);
        db.close();
        onTaskCompleteDelegator();
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
