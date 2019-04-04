package org.wycliffeassociates.translationrecorder.FilesPage;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import org.apache.commons.io.FileUtils;
import org.wycliffeassociates.translationrecorder.FilesPage.Export.SimpleProgressCallback;
import org.wycliffeassociates.translationrecorder.FilesPage.Export.TranslationExchangeDiff;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;
import org.wycliffeassociates.translationrecorder.project.components.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sarabiaj on 11/15/2017.
 */

public class Manifest {

    protected Project mProject;
    protected List<File> mTakes = new ArrayList<>();
    protected Map<Integer, User> mUsers = new HashMap<>();
    File mProjectDirectory;
    Collection<File> mProjectFiles;
    SimpleProgressCallback mProgressCallback;
    int mChunksWritten = 0;
    int mTotalChunks = 0;

    public Manifest(Project project, File projectDirectory) {
        mProject = project;
        mProjectDirectory = projectDirectory;
        mProjectFiles = FileUtils.listFiles(mProjectDirectory, new String[]{"wav"}, true);
    }

    public File createManifestFile(Context ctx, ProjectDatabaseHelper db) throws IOException {

        ChunkPlugin plugin = mProject.getChunkPlugin(new ChunkPluginLoader(ctx));
        List<Chapter> chapters = plugin.getChapters();
        mTotalChunks = getTotalChunks(chapters);
        Gson gson = new Gson();
        File output = new File(mProjectDirectory, "manifest.json");
        try (JsonWriter jw = gson.newJsonWriter(new FileWriter(output))) {
            jw.beginObject();
            writeLanguage(jw);
            writeBook(jw);
            writeVersion(jw);
            writeAnthology(jw);
            writeMode(jw);
            writeChapters(db, chapters, jw);
            writeUsers(jw);
            jw.endObject();
        }

        return output;
    }

    public List<File> getTakesInManifest() {
        return mTakes;
    }

    public void setProgressCallback(SimpleProgressCallback progressCallback) {
        mProgressCallback = progressCallback;
    }

    private void writeLanguage(JsonWriter jw) throws IOException {
        jw.name("language");
        jw.beginObject();
        jw.name("slug").value(mProject.getTargetLanguageSlug());
        jw.name("name").value(mProject.mTargetLanguage.getName());
        jw.endObject();
    }

    private void writeBook(JsonWriter jw) throws IOException {
        jw.name("book");
        jw.beginObject();
        jw.name("name").value(mProject.getBookName());
        jw.name("slug").value(mProject.getBookSlug());
        jw.name("number").value(mProject.getBookNumber());
        jw.endObject();
    }

    private void writeMode(JsonWriter jw) throws IOException {
        jw.name("mode");
        jw.beginObject();
        jw.name("name").value(mProject.getModeName());
        jw.name("slug").value(mProject.getModeSlug());
        jw.name("type").value(mProject.getModeType().toString());
        jw.endObject();
    }

    private void writeVersion(JsonWriter jw) throws IOException {
        jw.name("version");
        jw.beginObject();
        jw.name("slug").value(mProject.getVersionSlug());
        jw.name("name").value(mProject.mVersion.getName());
        jw.endObject();
    }

    private void writeAnthology(JsonWriter jw) throws IOException {
        jw.name("anthology");
        jw.beginObject();
        jw.name("slug").value(mProject.getAnthologySlug());
        jw.name("name").value(mProject.mAnthology.getName());
        jw.endObject();
    }

    private void writeChapters(ProjectDatabaseHelper db, List<Chapter> chapters, JsonWriter jw) throws IOException {
        jw.name("manifest");
        jw.beginArray();
        for (Chapter chapter : chapters) {
            int number = chapter.getNumber();
            int checkingLevel = 0;
            if (db.chapterExists(mProject, number)) {
                checkingLevel = db.getChapterCheckingLevel(mProject, number);
            }
            jw.beginObject();
            jw.name("chapter").value(number);
            jw.name("checking_level").value(checkingLevel);
            writeChunks(db, chapter.getChunks(), number, jw);
            jw.endObject();
        }
        jw.endArray();
    }

    private void writeChunks(ProjectDatabaseHelper db, List<Chunk> chunks, int chapter, JsonWriter jw) throws IOException {
        jw.name("chunks");
        jw.beginArray();
        for (Chunk chunk : chunks) {
            int startv = chunk.getStartVerse();
            int endv = chunk.getEndVerse();
            jw.beginObject();
            jw.name("startv").value(startv);
            jw.name("endv").value(endv);
            writeTakes(db, chapter, startv, endv, jw);
            jw.endObject();

            mChunksWritten++;

            if (mProgressCallback != null) {
                mProgressCallback.setUploadProgress(TranslationExchangeDiff.DIFF_ID, getManifestProgress());
            }
        }
        jw.endArray();
    }

    private void writeTakes(ProjectDatabaseHelper db, int chapter, int startv, int endv, JsonWriter jw) throws IOException {
        List<File> takes = getTakesList(chapter, startv, endv);
        jw.name("takes");
        jw.beginArray();
        for (Iterator<File> i = takes.iterator(); i.hasNext(); ) {
            File take = i.next();
            ProjectPatternMatcher ppm = mProject.getPatternMatcher();
            ppm.match(take);
            if (ppm.matched()) {
                TakeInfo info = ppm.getTakeInfo();
                int rating = db.getTakeRating(info);
                User user = db.getTakeUser(info);
                if(!mUsers.containsKey(user.getId())) {
                    mUsers.put(user.getId(), user);
                }
                jw.beginObject();
                jw.name("name").value(take.getName());
                jw.name("rating").value(rating);
                jw.name("user_id").value(user.getId());
                jw.endObject();
            } else {
                i.remove();
            }
        }
        jw.endArray();
        mTakes.addAll(takes);
    }

    private void writeUsers(JsonWriter jw) throws IOException {
        jw.name("users");
        jw.beginArray();
        for (User user : mUsers.values()) {
            jw.beginObject();
            jw.name("name_audio").value(user.getAudio().getName());
            jw.name("icon_hash").value(user.getHash());
            jw.name("id").value(user.getId());
            jw.endObject();
        }
        jw.endArray();
    }

    private int getTotalChunks(List<Chapter> chapters) {
        int total = 0;
        for (Chapter chapter: chapters) {
            total += chapter.getChunks().size();
        }
        return total;
    }

    private int getManifestProgress() {
        if(mTotalChunks <= 0) return 0;
        return Math.round((float) mChunksWritten / (float) mTotalChunks * 100);
    }

    private List<File> getTakesList(int chapter, int startv, int endv) {
        ProjectPatternMatcher ppm;
        //Get only the files of the appropriate unit
        List<File> resultFiles = new ArrayList<>();
        if (mProjectFiles != null) {
            for (File file : mProjectFiles) {
                ppm = mProject.getPatternMatcher();
                ppm.match(file);
                TakeInfo ti = ppm.getTakeInfo();
                if (ti != null
                        && ti.getChapter() == chapter
                        && ti.getStartVerse() == startv
                        && ti.getEndVerse() == endv
                        ) {
                    resultFiles.add(file);
                }
            }
        }
        return resultFiles;
    }

    public List<File> getUserFiles() {
        List<File> userAudioFiles = new ArrayList<>();
        for(User user: mUsers.values()) {
            userAudioFiles.add(user.getAudio());
        }
        return userAudioFiles;
    }
}
