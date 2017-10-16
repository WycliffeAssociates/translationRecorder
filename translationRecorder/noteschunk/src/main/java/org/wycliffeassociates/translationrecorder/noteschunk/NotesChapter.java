package org.wycliffeassociates.translationrecorder.noteschunk;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarabiaj on 8/16/2017.
 */

public class NotesChapter extends Chapter {

    int mNumber;
    List<NotesChunk> mChunks;

    NotesChapter(int number, List<NotesChunk> chunks){
        mNumber = number;
        mChunks = chunks;
    }

    @Override
    public List<Chunk> getChunks() {
        return new ArrayList<Chunk>(mChunks);
    }

    @Override
    public String getLabel(){
        return "chunk";
    }

    @Override
    public String getName() {
        return String.valueOf(mNumber);
    }

    @Override
    public int getNumber() {
        return mNumber;
    }

    @Override
    public void addChunk(Chunk chunk) {
        mChunks.add((NotesChunk)chunk);
    }

    @Override
    public String[] getChunkDisplayValues() {
        return new String[0];
    }
}
