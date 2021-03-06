package org.wycliffeassociates.translationrecorder.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import org.wycliffeassociates.translationrecorder.SettingsPage.Settings;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.components.Anthology;
import org.wycliffeassociates.translationrecorder.project.components.Book;
import org.wycliffeassociates.translationrecorder.project.components.Language;
import org.wycliffeassociates.translationrecorder.project.components.Mode;
import org.wycliffeassociates.translationrecorder.project.components.Version;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sarabiaj on 5/10/2016.
 */
public class Project implements Parcelable {

    public static final String PROJECT_EXTRA = "project_extra";

    public Language mTargetLanguage;
    public Language mSourceLanguage;
    public Anthology mAnthology;
    public Book mBook;
    public Version mVersion;
    public Mode mMode;
    public FileName mFileName;


    String mContributors;
    String mSourceAudioPath;

    public interface ProjectPluginLoader {
        ChunkPlugin loadChunkPlugin(Anthology anthology, Book book, ChunkPlugin.TYPE type);
        InputStream chunksInputStream(Anthology anthology, Book book);
    }

    public Project() {
    }

    public Project(Language target, Anthology anthology, Book book, Version version, Mode mode) {
        mTargetLanguage = target;
        mAnthology = anthology;
        mBook = book;
        mVersion = version;
        mMode = mode;
        mFileName = new FileName(target, anthology, version, book);
    }

    public Project(Language target, Anthology anthology, Book book, Version version, Mode mode, String sourceAudioPath) {
        this(target, anthology, book, version, mode);
        mSourceAudioPath = sourceAudioPath;
    }

    public boolean equals(Project p) {
        if (p != null) {
            if (mTargetLanguage != null  && mTargetLanguage.getSlug() != null && mTargetLanguage.getSlug().equals(p.getTargetLanguageSlug())
                    && mBook != null && mBook.getSlug() != null && mBook.getSlug().equals(p.getBookSlug())
                    && mVersion != null && mVersion.getSlug() != null && mVersion.getSlug().equals(p.getVersionSlug())
                    && mMode != null && mMode.getSlug() != null && mMode.getSlug().equals(p.getModeSlug())) {
                return true;
            }
        }
        return false;
    }

    public ChunkPlugin getChunkPlugin(ProjectPluginLoader pluginLoader) throws IOException {
        return pluginLoader.loadChunkPlugin(mAnthology, mBook, getModeType());
    }

    public static Project getProjectFromPreferences(Context ctx, ProjectDatabaseHelper db) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int projectId = pref.getInt(Settings.KEY_RECENT_PROJECT_ID, -1);
        Project project = db.getProject(projectId);
        return project;
    }

    public void loadProjectIntoPreferences(Context ctx, ProjectDatabaseHelper db) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
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

    public String getChapterFileName(int chapter) {
        String chapterFileName = mTargetLanguage.getSlug() +
                "_" + mAnthology.getSlug() +
                "_" + mVersion.getSlug() +
                "_" + mBook.getSlug() +
                "_c" + String.format("%02d", chapter) +
                ".wav";
        return chapterFileName;
    }

    public String getFileName(int chapter, int ... verses){
        if(mFileName == null) {
            mFileName = new FileName(mTargetLanguage, mAnthology, mVersion, mBook);
        }
        return mFileName.getFileName(chapter, verses);
    }

    public ProjectSlugs getProjectSlugs(){
        return new ProjectSlugs(getTargetLanguageSlug(), getVersionSlug(), Integer.parseInt(getBookNumber()), getBookSlug());
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

    public String getBookName() {
        return (mBook == null) ? "" : mBook.getName();
    }

    public String getVersionSlug() {
        return (mVersion == null) ? "" : mVersion.getSlug();
    }

    public String getModeSlug() {
        return (mMode == null) ? "" : mMode.getSlug();
    }

    public ChunkPlugin.TYPE getModeType() {
        return (mMode == null) ? null : mMode.getType();
    }

    public String getModeName() {
        return (mMode == null) ? "" : mMode.getName();
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

    public void setMode(Mode mode) {
        mMode = mode;
    }

    public void setContributors(String contributors) {
        mContributors = contributors;
    }

    public void setSourceAudioPath(String sourceAudioPath) {
        mSourceAudioPath = sourceAudioPath;
    }

    public ProjectPatternMatcher getPatternMatcher(){
        return new ProjectPatternMatcher(mAnthology.getRegex(), mAnthology.getMatchGroups());
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
        dest.writeParcelable(mMode, flags);
        dest.writeParcelable(mAnthology, flags);
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
        mMode = in.readParcelable(Mode.class.getClassLoader());
        mAnthology = in.readParcelable(Anthology.class.getClassLoader());
        mContributors = in.readString();
        mSourceAudioPath = in.readString();
    }
}
