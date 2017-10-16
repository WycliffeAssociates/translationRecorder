package org.wycliffeassociates.translationrecorder.questionschunk;

import org.wycliffeassociates.translationrecorder.chunkplugin.Chunk;

/**
 * Created by sarabiaj on 8/16/2017.
 */

public class QuestionsChunk extends Chunk {
    public QuestionsChunk(int number) {
        super(computeLabel(number), number, number, 1);
    }

    static String computeLabel(int number){
        String pref = (number % 2 == 1)? "question " : "answer ";
        int num = (number % 2 == 0)? number/2 : (number + 1) / 2;
        return pref + num;
    }
}
