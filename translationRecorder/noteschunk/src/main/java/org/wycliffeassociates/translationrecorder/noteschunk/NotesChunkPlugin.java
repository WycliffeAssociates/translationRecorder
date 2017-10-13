package org.wycliffeassociates.translationrecorder.noteschunk;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkIterator;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.noteschunk.tokens.ChunkToken;
import org.wycliffeassociates.translationrecorder.noteschunk.tokens.NotesToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class NotesChunkPlugin extends ChunkPlugin {
    Map<Integer, Chapter> mChaptersMap;

    public NotesChunkPlugin(TYPE mode) {
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
        ChunkToken[] chunks = gson.fromJson(
                new JsonReader(new InputStreamReader(chunkFile)),
                ChunkToken[].class
        );
        for(int i = 0; i < chunks.length; i++) {
            int num = Integer.parseInt(chunks[i].getId());
            if(!mChaptersMap.containsKey(num)) {
                mChaptersMap.put(num, new NotesChapter(num, new ArrayList<NotesChunk>()));
            }
            NotesToken[] notes = chunks[i].getNotes();
            //Need to double length for ref and text
            int numNotes = notes.length * 2;
            for(int j = 1; j <= numNotes; j++) {
                mChaptersMap.get(num).addChunk(new NotesChunk(j));
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
        return "chunk";
    }

    @Override
    public String getChapterName(int chapter) {
        return mChaptersMap.get(chapter).getName();
    }

//    @Override
//    public String getChunkName(int chapter, int id) {
//        String pref = (id % 2 == 1)? "ref " : "text ";
//        return pref + mChaptersMap.get(chapter).getChunks().get(id);
//    }
}
