package org.wycliffeassociates.translationrecorder.project;

import android.os.Environment;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.TranslationRecorderApp;
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

    public static File createFile(Project project, int chapter, int startVerse, int endVerse) {
        File dir = getParentDirectory(project, chapter);
        String nameWithoutTake = project.getFileName(chapter, startVerse, endVerse);
        int take = getLargestTake(project, dir, nameWithoutTake) + 1;
        return new File(dir, nameWithoutTake + "_t" +  String.format("%02d", take) + ".wav");
    }

    private static int getLargestTake(Project project, File directory, String nameWithoutTake) {
        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }
        ProjectPatternMatcher ppm = project.getPatternMatcher();
        ppm.match(nameWithoutTake);
        TakeInfo baseTakeInfo = ppm.getTakeInfo();
        int maxTake = Math.max(baseTakeInfo.getTake(), 0);
        for (File f : files) {
            ppm.match(f);
            TakeInfo ti = ppm.getTakeInfo();
            if (baseTakeInfo.equalBaseInfo(ti)) {
                maxTake = (maxTake < ti.getTake()) ? ti.getTake() : maxTake;
            }
        }
        return maxTake;
    }

    public static int getLargestTake(Project project, File directory, File filename) {
        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }
        ProjectPatternMatcher ppm = project.getPatternMatcher();
        ppm.match(filename);
        TakeInfo takeInfo = ppm.getTakeInfo();
        int maxTake = takeInfo.getTake();
        for (File f : files) {
            ppm.match(f);
            TakeInfo ti = ppm.getTakeInfo();
            if (takeInfo.equalBaseInfo(ti)) {
                maxTake = (maxTake < ti.getTake()) ? ti.getTake() : maxTake;
            }
        }
        return maxTake;
    }

    public static File getParentDirectory(TakeInfo takeInfo){
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        File root = new File(
                Environment.getExternalStorageDirectory(),
                TranslationRecorderApp.getContext().getResources().getString(R.string.folder_name)
        );
        String path = String.format(
                "%s/%s/%s/%s",
                slugs.getLanguage(),
                slugs.getVersion(),
                slugs.getBook(),
                chapterIntToString(slugs.getBook(), takeInfo.getChapter())
        );
        File out = new File(root, path);
        return out;
    }

    public static File getParentDirectory(Project project, File file) {
        ProjectPatternMatcher ppm = project.getPatternMatcher();
        ppm.match(file);
        TakeInfo takeInfo = ppm.getTakeInfo();
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        File root = new File(
                Environment.getExternalStorageDirectory(),
                TranslationRecorderApp.getContext().getResources().getString(R.string.folder_name)
        );
        String path = String.format(
                "%s/%s/%s/%s",
                slugs.getLanguage(),
                slugs.getVersion(),
                slugs.getBook(),
                chapterIntToString(slugs.getBook(), takeInfo.getChapter())
        );
        File out = new File(root, path);
        return out;
    }

    public static File getParentDirectory(Project project, String file) {
        ProjectPatternMatcher ppm = project.getPatternMatcher();
        ppm.match(file);
        TakeInfo takeInfo = ppm.getTakeInfo();
        ProjectSlugs slugs = takeInfo.getProjectSlugs();
        File root = new File(
                Environment.getExternalStorageDirectory(),
                TranslationRecorderApp.getContext().getResources().getString(R.string.folder_name)
        );
        String path = String.format(
                "%s/%s/%s/%s",
                slugs.getLanguage(),
                slugs.getVersion(),
                slugs.getBook(),
                chapterIntToString(slugs.getBook(), takeInfo.getChapter())
        );
        File out = new File(root, path);
        return out;
    }

    public static File getParentDirectory(Project project, int chapter) {
        File root = new File(
                Environment.getExternalStorageDirectory(),
                TranslationRecorderApp.getContext().getResources().getString(R.string.folder_name)
        );
        String path = String.format(
                "%s/%s/%s/%s",
                project.getTargetLanguageSlug(),
                project.getVersionSlug(),
                project.getBookSlug(),
                chapterIntToString(project, chapter)
        );
        return new File(root, path);
    }

    public static File getProjectDirectory(Project project) {
        File projectDir = new File(getLanguageDirectory(project), project.getVersionSlug() +
                "/" + project.getBookSlug());
        return projectDir;
    }

    public static File getLanguageDirectory(Project project) {
        File root = new File(
                Environment.getExternalStorageDirectory(),
                TranslationRecorderApp.getContext().getResources().getString(R.string.folder_name)
        );
        File projectDir = new File(root, project.getTargetLanguageSlug());
        return projectDir;
    }

    public static void deleteProject(Project project, ProjectDatabaseHelper db) {
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
        return file.getMetadata().getModeSlug();
    }

    public static String getNameWithoutExtention(File file) {
        String name = file.getName();
        if (name.contains(".wav")) {
            name = name.replace(".wav", "");
        }
        return name;
    }

    public static String getNameWithoutTake(File file){
        return getNameWithoutTake(file.getName());
    }

    public static String getNameWithoutTake(String file){
        return file.split("(_t([\\d]{2}))?(.wav)?$")[0];
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

    //Extracts the identifiable section of a filename for source audio
    // For OBS project
    public static String getVerseSection(String name) {
        String VERSE = "(v([\\d]{2,3})(-([\\d]{2,3}))?)";
        Pattern verseSection = Pattern.compile(VERSE);
        Matcher matcher = verseSection.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
