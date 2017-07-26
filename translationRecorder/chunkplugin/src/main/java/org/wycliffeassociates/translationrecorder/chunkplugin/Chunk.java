package org.wycliffeassociates.translationrecorder.chunkplugin;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

public abstract class Chunk {

    public enum TYPE {
        SINGLE, MULTI
    }

    protected TYPE mMode;

    public static String FIRST_VERSE = "firstvs";
    public static String LAST_VERSE = "lastvs";

    public Chunk(TYPE mode) {
        mMode = mode;
    }
    public abstract int get(int chapter, int chunk);
    public abstract void parseChunks(File chunkFile);
    public abstract void parseChunks(InputStream chunkFile);
    public abstract void parseChunks(Reader chunkFile);
    public abstract void nextChunk();
    public abstract void previousChunk();
    public abstract int numChapters();
    public abstract int numChunks(int chapter);
    public abstract String getUnitLabel(int chapter, int unit);
    public abstract String getChapterLabel();
    public abstract String getChunkName();


}
