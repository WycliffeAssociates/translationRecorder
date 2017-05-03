package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import org.wycliffeassociates.translationrecorder.database.CorruptFileDialog;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync.ResyncUtils.getFilesInDirectory;

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

    public Map<Project, File> getProjectDirectoriesOnFileSystem() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        Map<Project, File> projectDirectories = new HashMap<>();
        File root = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder");
        File[] langs = root.listFiles();
        if (langs != null) {
            for(File lang : langs) {
                File[] versions = lang.listFiles();
                if (versions != null) {
                    for(File version : versions) {
                        File[] books = version.listFiles();
                        if (books != null) {
                            for(File book : books) {
                                Project project = db.getProject(lang.getName(), version.getName(), book.getName());
                                if(project != null) {
                                    projectDirectories.put(project, book);
                                } else {
                                    File[] chapters = book.listFiles();
                                    String mode = null;
                                    if(chapters != null) {
                                        for(File chapter : chapters) {
                                            File[] c = chapter.listFiles();
                                            if(c != null && c.length > 1) {
                                                WavFile wav = new WavFile(c[0]);
                                                mode = wav.getMetadata().getMode();
                                                break;
                                            }
                                        }
                                    }
                                    if(chapters != null && mode != null) {
                                        int languageId = db.getLanguageId(lang.getName());
                                        int bookId = db.getBookId(book.getName());
                                        int anthologyId = db.getLanguageId(lang.getName());
                                        int versionId = db.getVersionId(version.getName());
                                        project = new Project(db.getLanguage(languageId), db.getAnthology(anthologyId), db.getBook(bookId), db.getVersion(versionId), mode);
                                        projectDirectories.put(project, book);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return projectDirectories;
    }

    public Map<Project, File> getProjectDirectories(List<Project> projects) {
        Map<Project, File> projectDirectories = new HashMap();
        for (Project p : projects) {
            projectDirectories.put(p, ProjectFileUtils.getProjectDirectory(p));
        }
        return projectDirectories;
    }

    public Map<Project, File> getDirectoriesMissingFromDb(Map<Project, File> fs, Map<Project, File> db) {
        Map<Project, File> missingDirectories = new HashMap<>();
        for(Map.Entry<Project, File> f : fs.entrySet()) {
            if(!db.containsValue(f.getValue())) {
                missingDirectories.put(f.getKey(), f.getValue());
            }
        }
        return missingDirectories;
    }

    @Override
    public void run() {
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(mCtx);
        Map<Project, File> directoriesOnFs = getProjectDirectoriesOnFileSystem();
        //if the number of projects doesn't match up between the filesystem and the db, OR,
        //the projects themselves don't match an id in the db, then resync everything (only resyncing
        // projects missing won't remove dangling take references in the db)
        //NOTE: removing a project only removes dangling takes, not the project itself from the db
        if (directoriesOnFs.size() != db.getNumProjects() || db.projectsNeedingResync(directoriesOnFs.keySet()).size() > 0) {
            fullResync(db, directoriesOnFs);
        }
        db.close();
        onTaskCompleteDelegator();
    }

    public void fullResync(ProjectDatabaseHelper db, Map<Project, File> directoriesOnFs) {
        List<Project> projects = db.getAllProjects();
        Map<Project, File> directoriesFromDb = getProjectDirectories(projects);
        Map<Project, File> directoriesMissingFromFs = getDirectoriesMissingFromDb(directoriesOnFs, directoriesFromDb);

        //get directories of projects
        //check which directories are not in the list
        //for projects with directories, get their files and resync
        //for directories not in the list, try to find which pattern match succeeds
        for(Map.Entry<Project, File> dir : directoriesOnFs.entrySet()) {
            File[] chapters = dir.getValue().listFiles();
            if(chapters != null) {
                List<File> takes = getFilesInDirectory(chapters);
                db.resyncDbWithFs(dir.getKey(), takes, this, this);
            }
        }
        for(Map.Entry<Project, File> dir : directoriesMissingFromFs.entrySet()) {
            File[] chapters = dir.getValue().listFiles();
            if(chapters != null) {
                List<File> takes = getFilesInDirectory(chapters);
                db.resyncDbWithFs(dir.getKey(), takes, this, this);
            }
        }

        db.close();
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
