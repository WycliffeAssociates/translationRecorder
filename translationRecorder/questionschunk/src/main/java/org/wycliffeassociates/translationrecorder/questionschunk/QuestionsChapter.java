package org.wycliffeassociates.translationrecorder.questionschunk;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarabiaj on 8/16/2017.
 */

public class QuestionsChapter extends Chapter {

    int mNumber;
    List<QuestionsChunk> mChunks;

    QuestionsChapter(int number, List<QuestionsChunk> chunks){
        mNumber = number;
        mChunks = chunks;
    }

    @Override
    public List<Chunk> getChunks() {
        return new ArrayList<Chunk>(mChunks);
    }

    @Override
    public String getLabel(){
        return "chapter";
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
        mChunks.add((QuestionsChunk)chunk);
    }

    @Override
    public String[] getChunkDisplayValues() {
        return new String[0];
    }
}
