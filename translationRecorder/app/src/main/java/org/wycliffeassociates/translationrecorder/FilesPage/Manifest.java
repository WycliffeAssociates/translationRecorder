package org.wycliffeassociates.translationrecorder.FilesPage;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;
import org.wycliffeassociates.translationrecorder.project.ChunkPluginLoader;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.project.ProjectPatternMatcher;
import org.wycliffeassociates.translationrecorder.project.TakeInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sarabiaj on 11/15/2017.
 */

public class Manifest {

    protected Project mProject;
    protected List<File> mTakes = new ArrayList<>();

    public Manifest() {}

    public void createManifestFile(Context ctx, Project p, File file, ProjectDatabaseHelper db) throws IOException {
        mProject = p;
        ChunkPlugin plugin = p.getChunkPlugin(new ChunkPluginLoader(ctx));
        List<Chapter> chapters = plugin.getChapters();
        Gson gson = new Gson();
        try (JsonWriter jw = gson.newJsonWriter(new FileWriter(file))) {
            jw.beginObject();
            writeLanguage(jw);
            writeBook(jw);
            writeVersion(jw);
            writeAnthology(jw);
            writeMode(jw);
            writeChapters(db, chapters, jw);
            jw.endObject();
        }
    }

    public List<File> getTakesInManifest() {
        return mTakes;
    }

    private void writeLanguage(JsonWriter jw) throws IOException {
        jw.name("language");
        jw.beginObject();
        jw.name("slug").value(mProject.getTargetLanguageSlug());
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
        jw.endObject();
    }

    private void writeAnthology(JsonWriter jw) throws IOException {
        jw.name("anthology");
        jw.beginObject();
        jw.name("slug").value(mProject.getAnthologySlug());
        jw.endObject();
    }

    private void writeChapters(ProjectDatabaseHelper db, List<Chapter> chapters, JsonWriter jw) throws IOException {
        jw.name("manifest");
        jw.beginArray();
        for(Chapter chapter : chapters) {
            int number = chapter.getNumber();
            int checkingLevel = 0;
            if(db.chapterExists(mProject, number)) {
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
        for(Chunk chunk : chunks) {
            int startv = chunk.getStartVerse();
            int endv = chunk.getEndVerse();
            jw.beginObject();
            jw.name("startv").value(startv);
            jw.name("endv").value(endv);
            writeTakes(db, chapter, startv, endv, jw);
            jw.endObject();
        }
        jw.endArray();
    }

    private void writeTakes(ProjectDatabaseHelper db, int chapter, int startv, int endv, JsonWriter jw) throws IOException {
        List<File> takes = getTakesList(chapter, startv, endv);
        jw.name("takes");
        jw.beginArray();
        for(Iterator<File> i = takes.iterator(); i.hasNext();) {
            File take = i.next();
            ProjectPatternMatcher ppm = mProject.getPatternMatcher();
            ppm.match(take);
            if(ppm.matched()) {
                TakeInfo info = ppm.getTakeInfo();
                int rating = db.getTakeRating(info);
                jw.beginObject();
                jw.name("name").value(take.getName());
                jw.name("rating").value(rating);
                jw.endObject();
            } else {
                i.remove();
            }
        }
        jw.endArray();
        mTakes.addAll(takes);
    }

    private List<File> getTakesList(int chapter, int startv, int endv) {
        File root = ProjectFileUtils.getProjectDirectory(mProject);
        String chap = ProjectFileUtils.chapterIntToString(mProject, chapter);
        File folder = new File(root, chap);
        File[] files = folder.listFiles();
        ProjectPatternMatcher ppm;
        int first = startv;
        int end = endv;
        //Get only the files of the appropriate unit
        List<File> resultFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                ppm = mProject.getPatternMatcher();
                ppm.match(file);
                TakeInfo ti = ppm.getTakeInfo();
                if (ti.getStartVerse() == first && ti.getEndVerse() == end) {
                    resultFiles.add(file);
                }
            }
        }
        return resultFiles;
    }
}
