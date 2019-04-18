package org.wycliffeassociates.translationrecorder.obschunk;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

/**
 * Created by sarabiaj on 8/8/2017.
 */

public class ObsChunk extends Chunk {
    public ObsChunk(String startVerse, String endVerse) {
        super(startVerse, Integer.parseInt(startVerse), Integer.parseInt(endVerse), ((Integer.parseInt(endVerse) - Integer.parseInt(startVerse)) + 1));
    }

    public String getRangeDisplay(){
        return String.valueOf(getStartVerse());
    }
}
