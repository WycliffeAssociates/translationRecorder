package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import com.door43.tools.reporting.Logger;
import org.wycliffeassociates.translationrecorder.database.CorruptFileDialog;
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

public class ChapterResyncTask extends Task implements ProjectDatabaseHelper.OnLanguageNotFound,
        ProjectDatabaseHelper.OnCorruptFile {

    Context mCtx;
    FragmentManager mFragmentManager;
    Project mProject;
    File mChapterDir;
    ProjectDatabaseHelper db;

    public ChapterResyncTask(
            int taskId,
            Context ctx,
            FragmentManager fm,
            Project project,
            ProjectDatabaseHelper db
    ) {
        super(taskId);
        mCtx = ctx;
        mFragmentManager = fm;
        mProject = project;
        this.db = db;
        String path = String.format(
                "%s/%s/%s/%s/",
                mCtx.getResources().getString(R.string.folder_name),
                mProject.getTargetLanguageSlug(),
                mProject.getVersionSlug(),
                mProject.getBookSlug()
                );
        mChapterDir = new File(Environment.getExternalStorageDirectory(), path);
    }

    public List<Integer> getAllChapters(File chapterDir) {
        List<Integer> chapterList = new ArrayList<>();
        File[] dirs = chapterDir.listFiles();
        if(dirs != null) {
            for (File f : dirs) {
                if (f.isDirectory()) {
                    try {
                        chapterList.add(Integer.parseInt(f.getName()));
                    } catch (NumberFormatException e) {
                        Logger.e(this.toString(), "Tried to add chapter " + f.getName() + " which does not parse as an Integer");
                    }
                }
            }
        }
        return chapterList;
    }

    @Override
    public void run() {
        List<Integer> chapters = getAllChapters(mChapterDir);
        for(Integer i : chapters) {
            if(!db.chapterExists(mProject, i)){
                db.resyncProjectWithFilesystem(mProject, ResyncUtils.getFilesInDirectory(mChapterDir.listFiles()), this, this);
                break;
            }
        }
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
