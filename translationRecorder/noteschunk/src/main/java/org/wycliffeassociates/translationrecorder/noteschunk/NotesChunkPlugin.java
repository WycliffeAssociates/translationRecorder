package org.wycliffeassociates.translationrecorder.noteschunk;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.noteschunk.tokens.ChunkToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesChunkPlugin extends ChunkPlugin {
    Map<Integer, Chapter> mChapters;
    ArrayList<Map<String, String>> mParsedChunks;
    int mNumChapters = 0;

    public NotesChunkPlugin(TYPE mode) {
        super(mode);
    }

    @Override
    public Chapter getChapter(int chapter) {
        return mChapters.get(chapter);
    }

    @Override
    public void parseChunks(File chunkFile) {
        try (InputStream is = new FileInputStream(chunkFile)) {
            parseChunks(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void parseChunks(InputStream chunkFile) {
        Gson gson = new Gson();
        ChunkToken[] chunks = gson.fromJson(new JsonReader(new InputStreamReader(chunkFile)), ChunkToken[].class);
    }


    @Override
    public void parseChunks(Reader chunkFile) {

    }

    @Override
    public void nextChunk() {

    }

    @Override
    public void previousChunk() {

    }

    @Override
    public int numChapters() {
        return mChapters.keySet().size();
    }

    @Override
    public int numChunks(int chapter) {
        return mChapters.get(chapter).getChunks().size();
    }

    @Override
    public String getUnitLabel(int chapter, int unit) {
        return mChapters.get(chapter).getChunks().get(unit).getLabel();
    }

    @Override
    public String getChapterLabel(int chapter) {
        return mChapters.get(chapter).getLabel();
    }

    @Override
    public String getChunkName(int chapter, int id) {
        //if (mMode == TYPE.SINGLE) {
        return "verse " + mChapters.get(chapter).getChunks().get(id);
//        } else {
//            return "chunk " + mVerses.get(chapter).get(id);
//        }
    }
}
