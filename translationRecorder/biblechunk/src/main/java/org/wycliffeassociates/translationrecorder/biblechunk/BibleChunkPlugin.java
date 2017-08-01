package org.wycliffeassociates.translationrecorder.biblechunk;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;

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

public class BibleChunkPlugin extends ChunkPlugin {

    Map<Integer, Chapter> mChapters;
    ArrayList<Map<String, String>> mParsedChunks;
    int mNumChapters = 0;

    public BibleChunkPlugin(TYPE mode) {
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
        Type type = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
        InputStreamReader isr = new InputStreamReader(chunkFile);
        try (JsonReader json = new JsonReader(isr)) {
            mParsedChunks = gson.fromJson(json, type);
            json.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String id = mParsedChunks.get(mParsedChunks.size() - 1).get("id");
        mNumChapters = Integer.parseInt(id.substring(0, id.lastIndexOf("-")));
        generateChunks(mParsedChunks);
    }

    private void generateChunks(List<Map<String, String>> parsedChunks) {
        mChapters = new HashMap<>();
        String chunkId;
        int chapter;
        for (Map<String, String> chunk : parsedChunks) {
            chunkId = chunk.get("id");
            chapter = Integer.parseInt(chunkId.substring(0, chunkId.lastIndexOf("-")));
            if (!mChapters.containsKey(chapter)) {
                mChapters.put(chapter, new BibleChapter(chapter, new HashMap<String, String>()));
            }
            mChapters.get(chapter).addChunk(new BibleChunk(chunk.get("firstvs"), chunk.get("lastvs")));
        }
    }

//    private void generateVerses() {
//        mVerses = new ArrayList<>();
//        ArrayList<Map<String, String>> temp = new ArrayList<>();
//        for (List<Map<String, String>> chapter : mChunks) {
//            int length = Integer.parseInt(chapter.get(chapter.size() - 1).get(LAST_VERSE));
//            for (int i = 1; i <= length; i++) {
//                Map<String, String> verse = new HashMap<>();
//                String verseNumber = String.valueOf(i);
//                verse.put(FIRST_VERSE, verseNumber);
//                verse.put(LAST_VERSE, verseNumber);
//                temp.add(verse);
//            }
//            mVerses.add(temp);
//            temp = new ArrayList<>();
//        }
//    }

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
        return null;
    }

    @Override
    public String getChapterLabel(int chapter) {
        return null;
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
