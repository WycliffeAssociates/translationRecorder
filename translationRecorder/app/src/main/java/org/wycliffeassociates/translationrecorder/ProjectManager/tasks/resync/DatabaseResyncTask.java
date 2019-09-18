package org.wycliffeassociates.translationrecorder.ProjectManager.tasks.resync;

import android.app.FragmentManager;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.ProjectManager.dialogs.RequestLanguageNameDialog;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp;
import org.wycliffeassociates.translationrecorder.database.CorruptFileDialog;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.utilities.Task;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sarabiaj on 9/27/2016.
 */
public class DatabaseResyncTask extends Task implements ProjectDatabaseHelper.OnLanguageNotFound,
        ProjectDatabaseHelper.OnCorruptFile {
    FragmentManager mFragmentManager;
    ProjectDatabaseHelper db;

    public DatabaseResyncTask(int taskId, FragmentManager fm, ProjectDatabaseHelper db){
        super(taskId);
        mFragmentManager = fm;
        this.db = db;
    }

    public List<File> getAllTakes(){
        File root = new File(
                Environment.getExternalStorageDirectory(),
                TranslationRecorderApp.getContext().getResources().getString(R.string.folder_name)
        );
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

    public Map<Project, File> getProjectDirectoriesOnFileSystem() {
        Map<Project, File> projectDirectories = new HashMap<>();
        File root = new File(
                Environment.getExternalStorageDirectory(),
                TranslationRecorderApp.getContext().getResources().getString(R.string.folder_name)
        );
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
        List<Project> projects = db.getAllProjects();
        Map<Project, File> directoriesOnFs = getProjectDirectoriesOnFileSystem();
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
