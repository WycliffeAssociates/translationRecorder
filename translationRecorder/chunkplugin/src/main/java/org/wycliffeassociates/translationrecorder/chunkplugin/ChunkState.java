package org.wycliffeassociates.translationrecorder.chunkplugin;

import java.util.List;

/**
 * Created by sarabiaj on 8/22/2017.
 */

public class ChunkState {

    int[] chunksPerChapter;
    int currentChunk = 0;
    int currentChapter = 0;

    ChunkState(List<Chapter> chapters) {
        chunksPerChapter = new int[chapters.size()];
        for(int i = 0; i < chapters.size(); i++) {
            chunksPerChapter[i] = chapters.get(i).getChunks().size();
        }
    }

    public void setState(int chapter, int chunk) {
        currentChapter = chapter;
        currentChunk = chunk;
    }

    public void nextChunk(){
        currentChunk++;
        if (currentChunk >= chunksPerChapter[currentChapter]) {
            currentChunk = 0;
        }
    }

    public void previousChunk(){
        currentChunk--;
        if (currentChunk < 0) {
            currentChunk = chunksPerChapter[currentChapter] - 1;
        }
    }

    public void nextChapter() {
        currentChapter++;
        if(currentChapter >= chunksPerChapter.length) {
            currentChapter = 0;
        }
        currentChunk = 0;
    }

    public void previousChapter() {
        currentChapter--;
        if(currentChapter < 0) {
            currentChapter = chunksPerChapter.length - 1;
        }
        currentChunk = 0;
    }

    public int getCurrentChunk(){
        return currentChunk;
    }

    public int getCurrentChapter() {
        return currentChapter;
    }
}
