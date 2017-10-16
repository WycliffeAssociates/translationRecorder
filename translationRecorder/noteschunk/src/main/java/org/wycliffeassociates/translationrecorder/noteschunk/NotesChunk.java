package org.wycliffeassociates.translationrecorder.noteschunk;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

/**
 * Created by sarabiaj on 8/16/2017.
 */

public class NotesChunk extends Chunk {
    public NotesChunk(int number) {
        super(computeLabel(number), number, number, 1);
    }

    static String computeLabel(int number){
        String pref = (number % 2 == 1)? "ref " : "text ";
        int num = (number % 2 == 0)? number/2 : (number + 1) / 2;
        return pref + num;
    }
}
