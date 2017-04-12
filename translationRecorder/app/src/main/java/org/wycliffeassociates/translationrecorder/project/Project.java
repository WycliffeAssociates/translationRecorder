package org.wycliffeassociates.translationrecorder.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.components.Anthology;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.Version;

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

    private ProjectPatternMatcher mProjectPatternMatcher;

    public Project() {
    }

    public Project(Language target, Anthology anthology, Book book, Version version, String mode, String regex, String groups) {
        mTargetLanguage = target;
        mAnthology = anthology;
        mBook = book;
        mVersion = version;
        mMode = mode;
        mProjectPatternMatcher = new ProjectPatternMatcher(regex, groups);
    }

    public Project(Language target, Anthology anthology, Book book, Version version, String mode, String regex, String groups, String sourceAudioPath) {
        this(target, anthology, book, version, mode, regex, groups);
        mSourceAudioPath = sourceAudioPath;
    }

    public static Project getProjectFromPreferences(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int projectId = pref.getInt(Settings.KEY_RECENT_PROJECT_ID, -1);
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        Project project = db.getProject(projectId);
        return project;
    }

    public void loadProjectIntoPreferences(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(ctx);
        if(db.projectExists(this)) {
            int projectId = db.getProjectId(this);
            pref.edit().putInt(Settings.KEY_RECENT_PROJECT_ID, projectId).commit();
        }
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

    public void setBook(Book book) {
        mBook = book;
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

    public ProjectPatternMatcher getPatternMatcher(){
        return mProjectPatternMatcher;
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
        dest.writeString(mProjectPatternMatcher.getRegex());
        dest.writeString(mProjectPatternMatcher.getGroups());
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
        mProjectPatternMatcher = new ProjectPatternMatcher(in.readString(), in.readString());
        mContributors = in.readString();
        mSourceAudioPath = in.readString();
    }
}
