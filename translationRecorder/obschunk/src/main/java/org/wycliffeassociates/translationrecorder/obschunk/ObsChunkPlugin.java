package org.wycliffeassociates.translationrecorder.obschunk;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObsChunkPlugin extends ChunkPlugin {

    ArrayList<Map<String, String>> mParsedChunks;

    public ObsChunkPlugin(TYPE mode) {
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
        Type type = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
        InputStreamReader isr = new InputStreamReader(chunkFile);
        try (JsonReader json = new JsonReader(isr)) {
            mParsedChunks = gson.fromJson(json, type);
            json.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        generateChunks(mParsedChunks);
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
                chaptersMap.put(chapter, new ObsChapter(chapter, new HashMap<String, String>()));
            }
            chaptersMap.get(chapter).addChunk(new ObsChunk(chunk.get("firstvs"), chunk.get("lastvs")));
        }
        mChapters = new ArrayList<>(chaptersMap.values());
    }

    @Override
    public String getUnitLabel(int chapter, int unit) {
        return null;
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
//        return "chunk " + mChapters.get(chapter).getChunks().get(id);
////        } else {
////            return "chunk " + mVerses.get(chapter).get(id);
////        }
//    }
}
