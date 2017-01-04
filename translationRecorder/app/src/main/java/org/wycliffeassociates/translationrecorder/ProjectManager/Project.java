package org.wycliffeassociates.translationrecorder.ProjectManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;

/**
 * Created by sarabiaj on 5/10/2016.
 */
public class Project implements Parcelable {

    public static final String PROJECT_EXTRA = "project_extra";

    String mTargetLanguage;
    String mSourceLanguage;
    String mSlug;
    String mVersion;
    String mMode;
    String mAnthology;
    String mContributors;
    String mBookNumber;
    String mSourceAudioPath;

    public Project() {
    }

    public Project(String tLang, String sLang, String bookNum, String slug, String version, String mode, String project, String contributors, String sourceAudioPath) {
        mTargetLanguage = tLang;
        mSourceLanguage = sLang;
        mSlug = slug;
        mBookNumber = bookNum;
        mVersion = version;
        mMode = mode;
        mAnthology = project;
        mContributors = contributors;
        mSourceAudioPath = sourceAudioPath;
    }

    public Project(String tLang, String sLang, int bookNum, String slug, String version, String mode, String project, String contributors, String sourceAudioPath) {
        mTargetLanguage = tLang;
        mSourceLanguage = sLang;
        mSlug = slug;
        mBookNumber = String.valueOf(bookNum);
        mVersion = version;
        mMode = mode;
        mAnthology = project;
        mContributors = contributors;
        mSourceAudioPath = sourceAudioPath;
    }

    public static Project getProjectFromPreferences(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String tLang = pref.getString(Settings.KEY_PREF_LANG, "");
        String sLang = pref.getString(Settings.KEY_PREF_LANG_SRC, "");
        String bookNum = pref.getString(Settings.KEY_PREF_BOOK_NUM, "");
        String slug = pref.getString(Settings.KEY_PREF_BOOK, "");
        String version = pref.getString(Settings.KEY_PREF_VERSION, "");
        String mode = pref.getString(Settings.KEY_PREF_CHUNK_VERSE, "");
        String project = pref.getString(Settings.KEY_PREF_ANTHOLOGY, "");
        String contributors = getContributorsFromJson(pref.getString(Settings.KEY_PROFILE, ""));
        String sourceAudioPath = pref.getString(Settings.KEY_PREF_SRC_LOC, "");
        return new Project(tLang, sLang, bookNum, slug, version, mode, project, contributors, sourceAudioPath);
    }

    private static String getContributorsFromJson(String jsonString){
        try {
            JSONObject json = new JSONObject(jsonString);
            return json.getString("full_name");
        } catch (JSONException e) {
            return "";
        }
    }

    public static void loadProjectIntoPreferences(Context ctx, Project project) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        pref.edit().putString(Settings.KEY_PREF_LANG, project.getTargetLanguage()).commit();
        pref.edit().putString(Settings.KEY_PREF_LANG_SRC, project.getSourceLanguage()).commit();
        pref.edit().putString(Settings.KEY_PREF_BOOK_NUM, project.getBookNumber()).commit();
        pref.edit().putString(Settings.KEY_PREF_BOOK, project.getSlug()).commit();
        pref.edit().putString(Settings.KEY_PREF_VERSION, project.getVersion()).commit();
        pref.edit().putString(Settings.KEY_PREF_CHUNK_VERSE, project.getMode()).commit();
        pref.edit().putString(Settings.KEY_PREF_ANTHOLOGY, project.getAnthology()).commit();
        pref.edit().putString(Settings.KEY_PROFILE, project.getContributors()).commit();
        pref.edit().putString(Settings.KEY_PREF_SRC_LOC, project.getSourceAudioPath()).commit();
    }

    public static File getProjectDirectory(Project project) {
        File projectDir = new File(getLanguageDirectory(project), project.getVersion() +
                "/" + project.getSlug());
        return projectDir;
    }

    public static File getLanguageDirectory(Project project) {
        File root = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder");
        File projectDir = new File(root, project.getTargetLanguage());
        return projectDir;
    }

    public boolean isOBS() {
        if (getAnthology().compareTo("obs") == 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getTargetLanguage() {
        return (mTargetLanguage == null) ? "" : mTargetLanguage;
    }

    public String getSourceLanguage() {
        return (mSourceLanguage == null) ? "" : mSourceLanguage;
    }

    public String getSlug() {
        return (mSlug == null) ? "" : mSlug;
    }

    public String getVersion() {
        return (mVersion == null) ? "" : mVersion;
    }

    public String getMode() {
        return (mMode == null) ? "" : mMode;
    }

    public String getContributors() {
        return (mContributors == null) ? "" : mContributors;
    }

    public String getBookNumber() {
        return (mBookNumber == null) ? "" : mBookNumber;
    }

    public String getAnthology() {
        return (mAnthology == null) ? "" : mAnthology;
    }

    public String getSourceAudioPath() {
        return (mSourceAudioPath == null) ? "" : mSourceAudioPath;
    }

    public void setTargetLanguage(String target) {
        mTargetLanguage = target;
    }

    public void setSourceLanguage(String source) {
        mSourceLanguage = source;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    public void setSlug(String slug) {
        mSlug = slug;
    }

    public void setMode(String mode) {
        mMode = mode;
    }

    public void setProject(String project) {
        mAnthology = project;
    }

    public void setContributors(String contributors) {
        mContributors = contributors;
    }

    public void setBookNumber(String bookNumber) {
        mBookNumber = bookNumber;
    }

    public void setBookNumber(int bookNumber) {
        mBookNumber = String.valueOf(bookNumber);
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
            sourceDir = new File(langDir, project.getVersion());
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
        dest.writeString(mTargetLanguage);
        dest.writeString(mSourceLanguage);
        dest.writeString(mSlug);
        dest.writeString(mVersion);
        dest.writeString(mMode);
        dest.writeString(mAnthology);
        dest.writeString(mContributors);
        dest.writeString(mBookNumber);
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
        mTargetLanguage = in.readString();
        mSourceLanguage = in.readString();
        mSlug = in.readString();
        mVersion = in.readString();
        mMode = in.readString();
        mAnthology = in.readString();
        mContributors = in.readString();
        mBookNumber = in.readString();
        mSourceAudioPath = in.readString();
    }
}
