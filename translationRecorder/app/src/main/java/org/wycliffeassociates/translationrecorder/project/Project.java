package org.wycliffeassociates.translationrecorder.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.components.Anthology;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.Version;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.id.mask;

/**
 * Created by sarabiaj on 5/10/2016.
 */
public class Project implements Parcelable {

    public static final String PROJECT_EXTRA = "project_extra";

    private Language mTargetLanguage;
    private Language mSourceLanguage;
    private Anthology mAnthology;
    private Book mBook;
    private Version mVersion;
    private String mMode;

    String mContributors;
    String mSourceAudioPath;

    private FileNameUtils mFileNameUtils;

    public Project() {
    }

    public Project(Language target, Anthology anthology, Book book, Version version, String mode, String regex, int mask) {
        mTargetLanguage = target;
        mAnthology = anthology;
        mBook = book;
        mVersion = version;
        mMode = mode;
        mFileNameUtils = new FileNameUtils(regex, mask);
    }

    public Project(Language target, Anthology anthology, Book book, Version version, String mode, String regex, int mask, String sourceAudioPath) {
        this(target, anthology, book, version, mode, regex, mask);
        mSourceAudioPath = sourceAudioPath;
    }

    public static Project getProjectFromPreferences(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int projectId = pref.getInt(Settings.KEY_RECENT_PROJECT_ID, -1);
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        Project project = db.getProject(projectId);
        return project;
    }

    public static void loadProjectIntoPreferences(Context ctx, Project project) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        if(db.projectExists(project)) {
            int projectId = db.getProjectId(project);
            pref.edit().putInt(Settings.KEY_RECENT_PROJECT_ID, projectId).commit();
        }
    }

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

    public boolean isOBS() {
        if (getAnthologySlug().compareTo("obs") == 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getTargetLanguageSlug() {
        return (mTargetLanguage == null) ? "" : mTargetLanguage.getSlug();
    }

    public String getSourceLanguageSlug() {
        return (mSourceLanguage == null) ? "" : mSourceLanguage.getSlug();
    }

    public String getAnthologySlug() {
        return (mAnthology == null) ? "" : mAnthology.getSlug();
    }

    public String getBookSlug() {
        return (mBook == null) ? "" : mBook.getSlug();
    }

    public String getVersionSlug() {
        return (mVersion == null) ? "" : mVersion.getSlug();
    }

    public String getMode() {
        return (mMode == null) ? "" : mMode;
    }

    public String getContributors() {
        return (mContributors == null) ? "" : mContributors;
    }

    public String getBookNumber() {
        return (mBook == null) ? "" : String.valueOf(mBook.getOrder());
    }

    public String getSourceAudioPath() {
        return (mSourceAudioPath == null) ? "" : mSourceAudioPath;
    }

    public void setTargetLanguage(Language target) {
        mTargetLanguage = target;
    }

    public void setSourceLanguage(Language source) {
        mSourceLanguage = source;
    }

    public void setVersion(Version version) {
        mVersion = version;
    }

    public void setAnthology(Anthology anthology) {
        mAnthology = anthology;
    }

    public void setMode(String mode) {
        mMode = mode;
    }

    public void setContributors(String contributors) {
        mContributors = contributors;
    }

    public void setSourceAudioPath(String sourceAudioPath) {
        mSourceAudioPath = sourceAudioPath;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mTargetLanguage, flags);
        if(mSourceLanguage != null) {
            dest.writeInt(1);
            dest.writeParcelable(mSourceLanguage, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeParcelable(mBook, flags);
        dest.writeParcelable(mVersion, flags);
        dest.writeString(mMode);
        dest.writeParcelable(mAnthology, flags);
        dest.writeString(mFileNameUtils.getRegex());
        dest.writeInt(mFileNameUtils.getMask());
        dest.writeString(mContributors);
        dest.writeString(mSourceAudioPath);
    }

    public static final Parcelable.Creator<Project> CREATOR = new Parcelable.Creator<Project>() {
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    public Project(Parcel in) {
        mTargetLanguage = in.readParcelable(Language.class.getClassLoader());
        int hasSourceLanguage = in.readInt();
        if(hasSourceLanguage == 1) {
            mSourceLanguage = in.readParcelable(Language.class.getClassLoader());
        }
        mBook = in.readParcelable(Book.class.getClassLoader());
        mVersion = in.readParcelable(Version.class.getClassLoader());
        mMode = in.readString();
        mAnthology = in.readParcelable(Anthology.class.getClassLoader());
        mFileNameUtils = new FileNameUtils(in.readString(), in.readInt());
        mContributors = in.readString();
        mSourceAudioPath = in.readString();
    }

    private class FileNameUtils {

        String mRegex;
        String mGroups;
        Pattern mPattern;
        Matcher mMatch;
        int[] locations;

        File mFile;
        String mName;


        public FileNameUtils(String regex, String groups) {
            mRegex = regex;
            mGroups = groups;
            mPattern = Pattern.compile(regex);
            parseLocations();
        }

        private void parseLocations() {
            String[] groups = mGroups.split(" ");
            locations = new int[groups.length];
            for(int i = 0; i < locations.length; i++) {
                locations[i] = Integer.parseInt(groups[i]);
            }
        }


        public String getRegex() {
            return mRegex;
        }

        public ProjectSlugs match(File file){
            match(file.getName());
        }

        public ProjectSlugs match(String file) {
            if (!(mName.equals(file))) {
                mName = file;
                mMatch = mPattern.matcher(file);
                mMatch.find();
                if(locations[0] != -1) {
                    mTargetLanguage =
                }
            } else {
                return
            }
        }

    }

    public class ProjectSlugs {

        public String getLanguage() {
            return mLanguage;
        }

        public String getAnthology() {
            return mAnthology;
        }

        public String getVersion() {
            return mVersion;
        }

        public int getBookNumber() {
            return mBookNumber;
        }

        public String getBook() {
            return mBook;
        }

        public int getChapter() {
            return mChapter;
        }

        public int getStartVerse() {
            return mStartVerse;
        }

        public int getEndVerse() {
            return mEndVerse;
        }

        public int getTake() {
            return mTake;
        }

        String mLanguage;
        String mAnthology;
        String mVersion;
        int mBookNumber;
        String mBook;
        int mChapter;
        int mStartVerse;
        int mEndVerse;
        int mTake;

        public ProjectSlugs(String language, String anthology, String version, int bookNumber, String book,
                            int chapter, int startVerse, int endVerse, int take)
        {
            mLanguage = language;
            mAnthology = anthology;
            mVersion = version;
            mBookNumber = bookNumber;
            mBook = book;
            mChapter = chapter;
            mStartVerse = startVerse;
            mEndVerse = endVerse;
            mTake = take;
        }
    }
}
