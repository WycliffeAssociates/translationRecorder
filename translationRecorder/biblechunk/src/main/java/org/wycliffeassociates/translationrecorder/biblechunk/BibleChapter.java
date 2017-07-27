package org.wycliffeassociates.translationrecorder.biblechunk;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

import java.util.List;
import java.util.Map;

/**
 * Created by sarabiaj on 7/27/2017.
 */

public class BibleChapter extends Chapter {

    List<Map<String, String>> mChunks;
    int mNumber;

    public BibleChapter(int number, List<Map<String, String>> chunks) {
        mChunks = chunks;
        mNumber = number;
    }

    public Chunk getChunk(int chunk){

    }

}
