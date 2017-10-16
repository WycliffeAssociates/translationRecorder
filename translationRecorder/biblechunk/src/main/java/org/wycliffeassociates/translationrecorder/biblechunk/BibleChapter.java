package org.wycliffeassociates.translationrecorder.biblechunk;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sarabiaj on 7/27/2017.
 */

public class BibleChapter extends Chapter {

    List<BibleChunk> mChunks;
    int mNumber;

    public BibleChapter(int number, Map<String, String> chunks) {
        mChunks = constructChunks(chunks);
        mNumber = number;
    }

    public String[] getChunkDisplayValues() {
        String[] display = new String[mChunks.size()];
        if (mChunks.size() > 0) {
            for (int i = 0; i < mChunks.size(); i++) {
                display[i] = mChunks.get(i).getRangeDisplay();
            }
        }
        return display;
    }

    private List<BibleChunk> constructChunks(Map<String,String> map) {
        mChunks = new ArrayList<>();
        for (String startVerse : map.keySet()) {
            mChunks.add(new BibleChunk(startVerse, map.get(startVerse)));
        }
        return mChunks;
    }

    @Override
    public List<Chunk> getChunks() {
        return new ArrayList<Chunk>(mChunks);
    }

    @Override
    public String getName() {
        return String.valueOf(mNumber);
    }

    @Override
    public String getLabel() {
        return "chapter";
    }

    @Override
    public int getNumber() {
        return mNumber;
    }

    public void addChunk(Chunk chunk) {
        mChunks.add((BibleChunk)chunk);
    }
}
