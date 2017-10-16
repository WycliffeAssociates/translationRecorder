package org.wycliffeassociates.translationrecorder.chunkplugin;

import java.util.List;

/**
 * Created by sarabiaj on 8/22/2017.
 */

public class ChunkIterator {

    protected ChunkState state;
    protected List<Chapter> chapters;

    public ChunkIterator(List<Chapter> chapters) {
        state = new ChunkState(chapters);
        this.chapters = chapters;
    }

    public void nextChunk() {
        state.nextChunk();
    }

    public void previousChunk() {
        state.previousChunk();
    }

    public void nextChapter() {
        state.nextChapter();
    }

    public void previousChapter() {
        state.previousChapter();
    }

    public Chunk getChunk() {
        return chapters.get(state.getCurrentChapter()).getChunks().get(state.getCurrentChunk());
    }

    public Chapter getChapter() {
        return chapters.get(state.getCurrentChapter());
    }

    public int getChapterIndex() {
        return state.getCurrentChapter();
    }

    public int getChunkIndex() {
        return state.getCurrentChunk();
    }

    /**
     * Accepts a chapter number and looks for that chapter in the list, sets the state to the index
     * of that chapter, and the chunk resets to 0
     *
     * @param chapter the chapter to set the iterator to
     */
    protected void setChapter(int chapter) {
        int chapterIndex = chapter;
        if (chapter != 0) {
            //default incase chapter is out of range
            chapterIndex = chapters.size() - 1;
            for (int i = 0; i < chapters.size(); i++) {
                if (chapters.get(i).getNumber() == chapter) {
                    chapterIndex = i;
                }
            }
        }
        state.setState(chapterIndex, 0);
    }

    /**
     * Accepts the chunk number and looks for that chunk in the list, sets the state to the index
     * of that chunk, and the chapter remains the same.
     *
     * @param chunk the chunk to set the iterator to
     */
    protected void setChunk(int chunk) {
        int chunkIndex = chunk;
        if (chunk != 0) {
            List<Chunk> chunks = chapters.get(state.currentChapter).getChunks();
            //default in case chunk is out of range
            chunkIndex = chunks.size() - 1;
            for (int i = 0; i < chunks.size(); i++) {
                if (chunks.get(i).getStartVerse() == chunk) {
                    chunkIndex = i;
                }
            }
        }
        state.setState(state.currentChapter, chunkIndex);
    }
}