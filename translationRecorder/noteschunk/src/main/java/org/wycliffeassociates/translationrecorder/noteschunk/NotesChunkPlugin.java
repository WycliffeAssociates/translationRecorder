package org.wycliffeassociates.translationrecorder.noteschunk;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.noteschunk.tokens.ChunkToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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
        mChapters = new HashMap<>();
        Gson gson = new Gson();
        ChunkToken[] chunks = gson.fromJson(new JsonReader(new InputStreamReader(chunkFile)), ChunkToken[].class);
        for(int i = 0; i < chunks.length; i++) {
            int num = Integer.parseInt(chunks[i].getId());
            if(!mChapters.containsKey(num)) {
                mChapters.put(num, new NotesChapter(num, new ArrayList<NotesChunk>()));
            }
            mChapters.get(num).addChunk(new NotesChunk(i));
        }
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
        String pref = (id % 2 == 0)? "ref " : "text ";
        return pref + mChapters.get(chapter).getChunks().get(id);
    }
}
