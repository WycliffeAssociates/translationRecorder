package org.wycliffeassociates.translationrecorder.chunkplugin;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

public abstract class ChunkPlugin {

    public enum TYPE {
        SINGLE, MULTI
    }

    List<Chapter> mChapters;

    protected TYPE mMode;

    public static String FIRST_VERSE = "firstvs";
    public static String LAST_VERSE = "lastvs";

    public ChunkPlugin(TYPE mode) {
        mMode = mode;
    }

    public Chapter getChapter(int chapter) {
        return mChapters.get(chapter - 1);
    }

    public List<Chunk> getChunks(int chapter) {
        return mChapters.get(chapter -1).getChunks();
    }

    public abstract void parseChunks(File chunkFile);
    public abstract void parseChunks(InputStream chunkFile);
    public abstract void parseChunks(Reader chunkFile);
    public abstract void nextChunk();

    public abstract void previousChunk();
    public abstract int numChapters();
    public abstract int numChunks(int chapter);

    public abstract String getUnitLabel(int chapter, int unit);

    public abstract String getChunkName();


}
