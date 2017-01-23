package org.wycliffeassociates.translationrecorder.ProjectManager.tasks;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.utilities.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sarabiaj on 1/23/2017.
 */

public class ChapterResyncTask extends Task implements ProjectDatabaseHelper.OnLanguageNotFound{

    Context mCtx;
    FragmentManager mFragmentManager;
    Project mProject;
    File mChapterDir;

    public ChapterResyncTask(int taskId, Context ctx, FragmentManager fm, Project project) {
        super(taskId);
        mCtx = ctx;
        mFragmentManager = fm;
        mProject = project;
        mChapterDir = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder/" + mProject.getTargetLanguage() + "/" + mProject.getVersion() + "/" + mProject.getSlug() + "/");
    }

    public List<Integer> getAllChapters(File chapterDir) {
        List<Integer> chapterList = new ArrayList<>();
        File[] dirs = chapterDir.listFiles();
        for(File f : dirs){
            if(f.isDirectory()){
                chapterList.add(Integer.parseInt(f.getName()));
            }
        }
        return chapterList;
    }

    @Override
    public void run() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        List<Integer> chapters = getAllChapters(mChapterDir);
        for(Integer i : chapters) {
            if(!db.chapterExists(mProject, i)){
                db.resyncProjectWithFilesystem(mProject, TaskUtils.getFilesInDirectory(mChapterDir.listFiles()), this);
                break;
            }
        }
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
