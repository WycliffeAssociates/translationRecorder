package org.wycliffeassociates.translationrecorder.biblechunk;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

/**
 * Created by sarabiaj on 7/28/2017.
 */

public class BibleChunk extends Chunk {

    public BibleChunk(String startVerse, String endVerse) {
        super(
                getLabel(startVerse, endVerse),
                Integer.parseInt(startVerse),
                Integer.parseInt(endVerse),
                ((Integer.parseInt(endVerse) - Integer.parseInt(startVerse)) + 1)
        );
    }

    private static String getLabel(String startVerse, String endVerse) {
        if (startVerse.equals(endVerse)) {
            return startVerse;
        } else {
            return startVerse + "-"+ endVerse;
        }
    }

    public String getRangeDisplay(){
        if(getStartVerse() == getEndVerse()) {
            return String.valueOf(getStartVerse());
        } else {
            return String.valueOf(getStartVerse()) + "-" + String.valueOf(getEndVerse());
        }
    }
}
