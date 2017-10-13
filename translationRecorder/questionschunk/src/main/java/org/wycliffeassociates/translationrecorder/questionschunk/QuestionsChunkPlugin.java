package org.wycliffeassociates.translationrecorder.questionschunk;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkIterator;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.questionschunk.tokens.QuestionsToken;

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
import java.util.Map;

public class QuestionsChunkPlugin extends ChunkPlugin {
    Map<Integer, Chapter> mChaptersMap;

    public QuestionsChunkPlugin(TYPE mode) {
        super(mode);
    }

    @Override
    public Chapter getChapter(int chapter) {
        return mChaptersMap.get(chapter);
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
        mChaptersMap = new HashMap<>();
        Gson gson = new Gson();

        Type type = new TypeToken<Map<String, QuestionsToken[]>>(){}.getType();

        Map<String, QuestionsToken[]> chunks = gson.fromJson(
                new JsonReader(new InputStreamReader(chunkFile)),
                type
        );

        for(Map.Entry<String, QuestionsToken[]> chunk : chunks.entrySet()) {
            int num = Integer.parseInt(chunk.getKey());
            if(!mChaptersMap.containsKey(num)) {
                mChaptersMap.put(num, new QuestionsChapter(num, new ArrayList<QuestionsChunk>()));
            }
            QuestionsToken[] notes = chunk.getValue();
            //Need to double length for ref and text
            int numQuestions = notes.length * 2;
            for(int j = 1; j <= numQuestions; j++) {
                mChaptersMap.get(num).addChunk(new QuestionsChunk(j));
            }
        }

        mChapters = new ArrayList<>(mChaptersMap.values());
        Collections.sort(mChapters, new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return Integer.compare(c1.getNumber(), c2.getNumber());
            }
        });
        mIter = new ChunkIterator(mChapters);
    }

    @Override
    public String getUnitLabel(int chapter, int unit) {
        return mChaptersMap.get(chapter).getChunks().get(unit).getLabel();
    }

    @Override
    public String getChapterLabel() {
        return "chapter";
    }

    @Override
    public String getChapterName(int chapter) {
        return mChaptersMap.get(chapter).getName();
    }
}
