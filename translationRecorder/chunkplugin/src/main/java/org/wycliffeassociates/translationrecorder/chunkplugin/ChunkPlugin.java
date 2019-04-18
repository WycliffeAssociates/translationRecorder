package org.wycliffeassociates.translationrecorder.chunkplugin;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class ChunkPlugin {

    /**
     * Create a Chunk Plugin for tR
     * ----------------------------
     *
     * compile subclassed files to a jar
     * run dx command from android sdk tools
     * dx --dex --output={output jar name} {input jar}
      */

    public enum TYPE {
        SINGLE, MULTI
    }

    public static final int DEFAULT_CHAPTER = 0;
    public static final int DEFAULT_UNIT = 0;

    protected ChunkIterator mIter;
    protected List<Chapter> mChapters;

    protected TYPE mMode;

    public static String FIRST_VERSE = "firstvs";
    public static String LAST_VERSE = "lastvs";

    public ChunkPlugin(TYPE mode) {
        mMode = mode;
    }

    public Chapter getChapter(int chapter) {
        //chapter number might not be associated with any particular index, so search
        for(Chapter c : mChapters) {
            if (c.getNumber() == chapter) {
                return c;
            }
        }
        return null;
    }

    public List<Chapter> getChapters() {
        return mChapters;
    }
    public List<Chunk> getChunks(int chapter) {
        return getChapter(chapter).getChunks();
    }

    public int getStartVerse() {
        return mIter.getChunk().getStartVerse();
    }
    public int getEndVerse() {
        return mIter.getChunk().getEndVerse();
    }
    public int getChapter() {
        return mIter.getChapter().getNumber();
    }

    public int getChapterLabelIndex() {
        return mIter.getChapterIndex();
    }

    public int getStartVerseLabelIndex() {
        return mIter.getChunkIndex();
    }

    public String[] getChapterDisplayLabels() {
        Collections.sort(mChapters, new Comparator<Chapter>() {
            @Override
            public int compare(Chapter c1, Chapter c2) {
                return Integer.compare(c1.getNumber(), c2.getNumber());
            }
        });
        String[] labels = new String[mChapters.size()];
        for(int i = 0; i < mChapters.size(); i++) {
            labels[i] = mChapters.get(i).getName();
        }
        return labels;
    }

    public String[] getChunkDisplayLabels() {
        List<Chunk> chunks = mIter.getChapter().getChunks();
        Collections.sort(chunks, new Comparator<Chunk>() {
            @Override
            public int compare(Chunk c1, Chunk c2) {
                return Integer.compare(c1.getStartVerse(), c2.getStartVerse());
            }
        });
        String[] labels = new String[chunks.size()];
        for(int i = 0; i < chunks.size(); i++) {
            labels[i] = chunks.get(i).getLabel();
        }
        return labels;
    }
    public void initialize(int chapter, int chunk) {
        mIter.setChapter(chapter);
        mIter.setChunk(chunk);
    }

    public int numChapters(){
        return mChapters.size();
    }

    public abstract void parseChunks(File chunkFile);
    public abstract void parseChunks(InputStream chunkFile);

    public void nextChunk() {
        mIter.nextChunk();
    };
    public void previousChunk() {
        mIter.previousChunk();
    }

    public void nextChapter(){
        mIter.nextChapter();
    }

    public void previousChapter() {
        mIter.previousChapter();
    }

    //public abstract int numChapters();
    //public abstract int numChunks(int chapter);
    public abstract String getChapterName(int chapter);
    public abstract String getChapterLabel();
    public abstract String getUnitLabel(int chapter, int unit);

    public String getChunkName() {
        return mIter.getChunk().getLabel();
    }


}
