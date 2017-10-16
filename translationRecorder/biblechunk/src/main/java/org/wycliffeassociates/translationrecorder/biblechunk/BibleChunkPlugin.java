package org.wycliffeassociates.translationrecorder.biblechunk;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkIterator;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleChunkPlugin extends ChunkPlugin {

    ArrayList<Map<String, String>> mParsedChunks;

    public BibleChunkPlugin(TYPE mode) {
        super(mode);
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
        Type type = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        InputStreamReader isr = new InputStreamReader(chunkFile);
        try (JsonReader json = new JsonReader(isr)) {
            mParsedChunks = gson.fromJson(json, type);
            json.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        generateChunks(mParsedChunks);
        if (mMode == TYPE.SINGLE) {
            generateVerses();
        }
        mIter = new ChunkIterator(mChapters);
    }

    private void generateChunks(List<Map<String, String>> parsedChunks) {
        Map<Integer, Chapter> chaptersMap = new HashMap<>();
        String chunkId;
        int chapter;
        for (Map<String, String> chunk : parsedChunks) {
            chunkId = chunk.get("id");
            chapter = Integer.parseInt(chunkId.substring(0, chunkId.lastIndexOf("-")));
            if (!chaptersMap.containsKey(chapter)) {
                chaptersMap.put(chapter, new BibleChapter(chapter, new HashMap<String, String>()));
            }
            chaptersMap.get(chapter).addChunk(
                    new BibleChunk(
                            chunk.get("firstvs"),
                            chunk.get("lastvs")
                    )
            );
        }
        mChapters = new ArrayList<>(chaptersMap.values());
        Collections.sort(mChapters, new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return Integer.compare(c1.getNumber(), c2.getNumber());
            }
        });
    }

    private void generateVerses() {
        List<Chapter> verses = new ArrayList<>();
        for (Chapter chap : mChapters) {
            int max = 0;
            List<Chunk> chunks = chap.getChunks();
            for (Chunk chunk : chunks) {
                if (chunk.getEndVerse() > max) {
                    max = chunk.getEndVerse();
                }
            }
            Chapter temp = new BibleChapter(chap.getNumber(), new HashMap<String, String>());
            max++;
            for (int i = 1; i < max; i++) {
                temp.addChunk(new BibleChunk(String.valueOf(i), String.valueOf(i)));
            }
            verses.add(temp);
        }
        mChapters = verses;
    }

    @Override
    public String getUnitLabel(int chapter, int unit) {
        return getChapter(chapter).getChunks().get(unit).getLabel();
    }

    @Override
    public String getChapterLabel() {
        return "chapter";
    }

    @Override
    public String getChapterName(int chapter) {
        return getChapter(chapter).getName();
    }

//    @Override
//    public String getChunkName(int chapter, int id) {
//        //if (mMode == TYPE.SINGLE) {
//        return "verse " + mChapters.get(chapter).getChunks().get(id);
////        } else {
////            return "chunk " + mVerses.get(chapter).get(id);
////        }
//    }
}
