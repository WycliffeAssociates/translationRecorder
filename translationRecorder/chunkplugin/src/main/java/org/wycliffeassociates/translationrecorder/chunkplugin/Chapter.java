package org.wycliffeassociates.translationrecorder.chunkplugin;

import java.util.List;

/**
 * Created by sarabiaj on 7/27/2017.
 */

public abstract class Chapter {

//    List<Chunk> mChunks;
//    String mLabel;
//    int mNumber;
//
//    public Chapter(int number, String label, List<Chunk> chunks) {
//        mChunks = chunks;
//        mLabel = label;
//        mNumber = number;
//    }

    public abstract List<Chunk> getChunks();

    public abstract String getName();

    public abstract String getLabel();

    public abstract int getNumber();

    public abstract void addChunk(Chunk chunk);

    public abstract String[] getChunkDisplayValues();

}
