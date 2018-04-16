package org.wycliffeassociates.translationrecorder.chunkplugin;

/**
 * Created by sarabiaj on 7/27/2017.
 */

public abstract class Chunk {

    int id;
    int mStartVerse;
    int mEndVerse;
    int mNumMarkers;

    String mLabel;

    public Chunk(int id, String label, int startVerse, int endVerse, int numMarkers) {
        this.id = id;
        mLabel = label;
        mStartVerse = startVerse;
        mEndVerse = endVerse;
        mNumMarkers = numMarkers;
    }

    public String getLabel() {
        return mLabel;
    }

    public int getStartVerse() {
        return mStartVerse;
    }

    public int getEndVerse() {
        return mEndVerse;
    }

    public int getNumMarkers() {
        return mNumMarkers;
    }
}
