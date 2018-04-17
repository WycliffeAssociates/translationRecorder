package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import org.wycliffeassociates.translationrecorder.database.CorruptFileDialog;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.data.model.Project;
import org.wycliffeassociates.translationrecorder.data.model.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.data.model.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.utilities.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sarabiaj on 1/20/2017.
 */

public class UnitResyncTask extends Task implements ProjectDatabaseHelper.OnLanguageNotFound, ProjectDatabaseHelper.OnCorruptFile {
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
                "TranslationRecorder/" + mProject.getTargetLanguageSlug() + "/" + mProject.getVersionSlug() + "/" + mProject.getBookSlug() + "/" + ProjectFileUtils.chapterIntToString(mProject, mChapter) + "/");
        File[] dirs = root.listFiles();
        List<File> files;
        if(dirs != null) {
            files = new LinkedList<>(Arrays.asList(dirs));
        } else {
            files = new ArrayList<>();
        }
        filterFiles(files);
        return files;
    }

    public void filterFiles(List<File> files) {
        Iterator<File> iter = files.iterator();
        while (iter.hasNext()) {
            ProjectPatternMatcher ppm = mProject.getPatternMatcher();
            ppm.match(iter.next());
            if(!ppm.matched()) {
                iter.remove();
            }
        }
    }

    @Override
    public void run() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        db.resyncChapterWithFilesystem(mProject, mChapter, getAllTakes(), this, this);
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
