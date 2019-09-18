package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import android.app.FragmentManager;
import android.os.Environment;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.CorruptFileDialog;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectProgress;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Mode;
import org.wycliffeassociates.translationrecorder.utilities.Task;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.io.IOException;
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

    FragmentManager mFragmentManager;
    ProjectDatabaseHelper db;
    ChunkPluginLoader chunkPluginLoader;

    public ProjectListResyncTask(
            int taskId,
            FragmentManager fm,
            ProjectDatabaseHelper db,
            ChunkPluginLoader pluginLoader
    ) {
        super(taskId);
        mFragmentManager = fm;
        this.db = db;
        chunkPluginLoader = pluginLoader;
    }

    public Map<Project, File> getProjectDirectoriesOnFileSystem() {
        Map<Project, File> projectDirectories = new HashMap<>();
        File root = new File(
                Environment.getExternalStorageDirectory(),
                TranslationRecorderApp.getContext().getResources().getString(R.string.folder_name)
        );
        File[] langs = root.listFiles();
        if (langs != null) {
            for(File lang : langs) {
                if (!lang.isDirectory()) {
                    continue;
                }
                File[] versions = lang.listFiles();
                if (versions != null) {
                    for(File version : versions) {
                        if(!version.isDirectory()) {
                            continue;
                        }
                        File[] bookDirs = version.listFiles();
                        if (bookDirs != null) {
                            for(File bookDir : bookDirs) {
                                if (!bookDir.isDirectory()) {
                                    continue;
                                }
                                //get the project from the database if it exists
                                Project project = db.getProject(lang.getName(), version.getName(), bookDir.getName());
                                if(project != null) {
                                    projectDirectories.put(project, bookDir);
                                } else { //otherwise derive the project from the filename
                                    File[] chapters = bookDir.listFiles();
                                    Mode mode = null;
                                    if(chapters != null) {
                                        for(File chapter : chapters) {
                                            if(!chapter.isDirectory()) {
                                                continue;
                                            }
                                            File[] c = chapter.listFiles();
                                            if(c != null) {
                                                for (int i = 0; i < c.length; i++) {
                                                    try {
                                                        WavFile wav = new WavFile(c[i]);
                                                        mode = db.getMode(
                                                                db.getModeId(
                                                                        wav.getMetadata().getModeSlug(),
                                                                        wav.getMetadata().getAnthology()
                                                                )
                                                        );
                                                    } catch (IllegalArgumentException e) {
                                                        //don't worry about the corrupt file dialog here;
                                                        // the database resync will pick it up.
                                                        Logger.e(this.toString(), c[i].getName(), e);
                                                        continue;
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if(chapters != null && mode != null) {
                                        int languageId = db.getLanguageId(lang.getName());
                                        int bookId = db.getBookId(bookDir.getName());
                                        Book book = db.getBook(bookId);
                                        int anthologyId = db.getAnthologyId(book.getAnthology());
                                        int versionId = db.getVersionId(version.getName());
                                        project = new Project(
                                                db.getLanguage(languageId),
                                                db.getAnthology(anthologyId),
                                                book,
                                                db.getVersion(versionId),
                                                mode
                                        );
                                        projectDirectories.put(project, bookDir);
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
        Map<Project, File> directoriesOnFs = getProjectDirectoriesOnFileSystem();
        //if the number of projects doesn't match up between the filesystem and the db, OR,
        //the projects themselves don't match an id in the db, then resync everything (only resyncing
        // projects missing won't remove dangling take references in the db)
        //NOTE: removing a project only removes dangling takes, not the project itself from the db
        boolean projectCountDiffers = directoriesOnFs.size() != db.getNumProjects();
        boolean projectsNeedResync = db.projectsNeedingResync(directoriesOnFs.keySet()).size() > 0;
        if (projectCountDiffers || projectsNeedResync) {
            fullResync(directoriesOnFs);
        }
        onTaskCompleteDelegator();
    }

    public void fullResync(Map<Project, File> directoriesOnFs) {
        List<Project> projects = db.getAllProjects();
        Map<Project, File> directoriesFromDb = getProjectDirectories(projects);
        Map<Project, File> directoriesMissingFromFs = getDirectoriesMissingFromDb(directoriesOnFs, directoriesFromDb);

        //get directories of projects
        //check which directories are not in the list
        //for projects with directories, get their files and resync
        //for directories not in the list, try to find which pattern match succeeds
        for(Map.Entry<Project, File> dir : directoriesOnFs.entrySet()) {
            File[] chapters = dir.getValue().listFiles();
            Project project = dir.getKey();
            if(chapters != null) {
                List<File> takes = getFilesInDirectory(chapters);
                db.resyncDbWithFs(dir.getKey(), takes, this, this);

                try {
                    ChunkPlugin chunkPlugin = project.getChunkPlugin(chunkPluginLoader);
                    // Recalculate project progress
                    ProjectProgress pp = new ProjectProgress(project, db, chunkPlugin.getChapters());
                    pp.updateProjectProgress();

                    // Recalculate chapters progress
                    pp.updateChaptersProgress();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for(Map.Entry<Project, File> dir : directoriesMissingFromFs.entrySet()) {
            File[] chapters = dir.getValue().listFiles();
            if(chapters != null) {
                List<File> takes = getFilesInDirectory(chapters);
                db.resyncDbWithFs(dir.getKey(), takes, this, this);
            }
        }
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
