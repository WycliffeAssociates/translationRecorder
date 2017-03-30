package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.project.FileNameExtractor;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;
import org.wycliffeassociates.translationrecorder.database.CorruptFileDialog;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sarabiaj on 1/19/2017.
 */

public class ProjectListResyncTask extends Task implements ProjectDatabaseHelper.OnLanguageNotFound,
        ProjectDatabaseHelper.OnCorruptFile {

    Context mCtx;
    FragmentManager mFragmentManager;

    public ProjectListResyncTask(int taskId, Context ctx, FragmentManager fm) {
        super(taskId);
        mCtx = ctx;
        mFragmentManager = fm;
    }

    public List<Project> getAllProjects() {
        File root = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder");
        List<Project> projectList = new ArrayList<>();
        File[] langs = root.listFiles();
        if (langs == null) {
            return projectList;
        }
        for (int i = 0; i < langs.length; i++) {
            File[] versions = langs[i].listFiles();
            if(versions == null) {
                continue;
            }
            for (int j = 0; j < versions.length; j++) {
                File[] books = versions[j].listFiles();
                if(books == null) {
                    continue;
                }
                for (int k = 0; k < books.length; k++) {
                    File[] chapters = books[k].listFiles();
                    if (chapters == null) {
                        continue;
                    }
                    for (int l = 0; l < chapters.length; l++) {
                        File[] takes = chapters[l].listFiles();
                        if (takes == null || takes.length <= 0) {
                            continue;
                        }
                        //loop over all files here in the event the first wav file happens to be corrupted
                        FileNameExtractor fne = new FileNameExtractor(takes[l]);
                        if (fne.matched()) {
                            try {
                                WavFile wav = new WavFile(takes[l]);
                                Project project = new Project(fne.getLang(), "", fne.getBookNumber(),
                                        fne.getBook(), fne.getVersion(), fne.getMode(wav), "", "", "");
                                projectList.add(project);
                            } catch (IllegalArgumentException e) {
                                //don't worry about the corrupt file dialog here; the database resync will pick it up.
                                Logger.e(this.toString(), "Corrupt File: " + takes[l].toString(), e);
                            }
                            break;
                        }
                    }
                }
            }
        }

        return projectList;
    }

    @Override
    public void run() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        List<Project> projects = getAllProjects();
        //if the number of projects doesn't match up between the filesystem and the db, OR,
        //the projects themselves don't match an id in the db, then resync everything (only resyncing
        // projects missing won't remove dangling take references in the db)
        //NOTE: removing a project only removes dangling takes, not the project itself from the db
        if (projects.size() != db.getNumProjects() || db.projectsNeedingResync(projects).size() > 0) {
            File projectDir = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder/");
            db.resyncDbWithFs(ResyncUtils.getAllTakes(projectDir), this, this);
        }
        db.close();
        onTaskCompleteDelegator();
    }

    public void onCorruptFile(final File file) {
        CorruptFileDialog cfd = CorruptFileDialog.newInstance(file);
        cfd.show(mFragmentManager, "CORRUPT_FILE");
    }

    public String requestLanguageName(String code) {
        BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
        RequestLanguageNameDialog dialog = RequestLanguageNameDialog.newInstance(code, response);
        dialog.show(mFragmentManager, "REQUEST_LANGUAGE");
        try {
            return response.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "???";
    }
}
