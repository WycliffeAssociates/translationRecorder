package org.wycliffeassociates.translationrecorder.biblechunk;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleChunk extends Chunk {

    ArrayList<ArrayList<Map<String, String>>> mChunks;
    ArrayList<ArrayList<Map<String, String>>> mVerses;
    ArrayList<Map<String, String>> mParsedChunks;
    int mNumChapters = 0;

    public BibleChunk(TYPE mode) {
        super(mode);
    }

    @Override
    public int get(int chapter, int chunk) {
        if (mMode == TYPE.SINGLE) {
            return mVerses.get(chapter - 1).get(chunk - 1).get("id");
        }
    }

    @Override
    public void parseChunks(File chunkFile) {
        try (InputStream is = new FileInputStream(chunkFile)) {
            parseChunks(is);
        }
    }

    @Override
    public void parseChunks(InputStream chunkFile) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();

        InputStreamReader isr = new InputStreamReader(chunkFile);
        JsonReader json = new JsonReader(isr);
        mParsedChunks = gson.fromJson(json, type);
        json.close();
        String id = mParsedChunks.get(mParsedChunks.size() - 1).get("id");
        mNumChapters = Integer.parseInt(id.substring(0, id.lastIndexOf("-")));
    }

    private void generateChunks(List<Map<String, String>> parsedChunks) {
        mChunks = new ArrayList<>();
        ArrayList<Map<String, String>> temp = new ArrayList<>();
        int currentChapter = 1;
        String chunkId;
        int chapter;
        for (Map<String, String> chunk : parsedChunks) {
            chunkId = chunk.get("id");
            chapter = Integer.parseInt(chunkId.substring(0, chunkId.lastIndexOf("-")));
            if (chapter == currentChapter) {
                temp.add(chunk);
            } else {
                mChunks.add(temp);
                temp = new ArrayList<>();
                temp.add(chunk);
                currentChapter = chapter;
            }
        }
        //add last
        mChunks.add(temp);
    }

    private void generateVerses() {
        mVerses = new ArrayList<>();
        ArrayList<Map<String, String>> temp = new ArrayList<>();
        for (List<Map<String, String>> chapter : mChunks) {
            int length = Integer.parseInt(chapter.get(chapter.size() - 1).get(LAST_VERSE));
            for (int i = 1; i <= length; i++) {
                Map<String, String> verse = new HashMap<>();
                String verseNumber = String.valueOf(i);
                verse.put(FIRST_VERSE, verseNumber);
                verse.put(LAST_VERSE, verseNumber);
                temp.add(verse);
            }
            mVerses.add(temp);
            temp = new ArrayList<>();
        }
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
        return mNumChapters;
    }

    @Override
    public int numChunks(int chapter) {
        if (mMode == TYPE.MULTI) {
            return mChunks.get(chapter - 1).size();
        } else {
            return mVerses.get(chapter - 1).size();
        }
    }

    @Override
    public String getUnitLabel(int chapter, int unit) {
        return null;
    }

    @Override
    public String getChapterLabel() {
        return null;
    }

    @Override
    public String getChunkName() {
        return null;
    }

    @Override
    public String getChunkName(int chapter, int id) {
        if (mMode == TYPE.SINGLE) {
            return "verse " + mVerses.get(chapter - 1).get(id);
        } else {
            return "chunk " + mVerses.get(chapter - 1).get(id);
        }
    }
}
