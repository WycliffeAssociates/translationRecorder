package org.wycliffeassociates.translationrecorder.project;

import android.content.Context;
import android.os.Environment;

import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Joe on 3/31/2017.
 */

public class ProjectFileUtils {

    private ProjectFileUtils(){}

    public static File getProjectDirectory(Project project) {
        File projectDir = new File(getLanguageDirectory(project), project.getVersionSlug() +
                "/" + project.getBookSlug());
        return projectDir;
    }

    public static File getLanguageDirectory(Project project) {
        File root = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder");
        File projectDir = new File(root, project.getTargetLanguageSlug());
        return projectDir;
    }

    public static void deleteProject(Context ctx, Project project) {
        File dir = getProjectDirectory(project);
        Utils.deleteRecursive(dir);
        File langDir = getLanguageDirectory(project);
        File sourceDir;
        if (project.isOBS()) {
            sourceDir = new File(langDir, "obs");
        } else {
            sourceDir = new File(langDir, project.getVersionSlug());
        }
        if (sourceDir.exists() && sourceDir.listFiles().length == 0) {
            sourceDir.delete();
            if (langDir.listFiles().length == 0) {
                langDir.delete();
            }
        }
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        db.deleteProject(project);
    }

    public static String chapterIntToString(String bookSlug, int chapter) {
        String result;
        if (bookSlug.compareTo("psa") == 0) {
            result = String.format("%03d", chapter);
        } else {
            result = String.format("%02d", chapter);
        }
        return result;
    }

    public static String chapterIntToString(Project project, int chapter) {
        String result;
        if (project.getBookSlug().compareTo("psa") == 0) {
            result = String.format("%03d", chapter);
        } else {
            result = String.format("%02d", chapter);
        }
        return result;
    }

    public static String unitIntToString(int unit) {
        return String.format("%02d", unit);
    }

    public static String getMode(WavFile file) {
        return file.getMetadata().getMode();
    }

    public static File getFileFromFileName(File file) {
        File dir = FileNameExtractor.getParentDirectory(file);
        if (file.getName().contains(".wav")) {
            return new File(dir, file.getName());
        } else {
            return new File(dir, file.getName() + ".wav");
        }
    }

    public static String getNameWithoutTake(String name) {
        FileNameExtractor fne = new FileNameExtractor(name);
        return fne.getNameWithoutTake();
    }

    public static String getNameWithoutExtention(File file) {
        String name = file.getName();
        if (name.contains(".wav")) {
            name = name.replace(".wav", "");
        }
        return name;
    }

    //Extracts the identifiable section of a filename for source audio
    public static String getChapterAndVerseSection(String name) {
        String CHAPTER = "c([\\d]{2,3})";
        String VERSE = "v([\\d]{2,3})(-([\\d]{2,3}))?";
        Pattern chapterAndVerseSection = Pattern.compile("(" + CHAPTER + "_" + VERSE + ")");
        Matcher matcher = chapterAndVerseSection.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
