package org.wycliffeassociates.translationrecorder.noteschunk.tokens;

/**
 * Created by sarabiaj on 8/15/2017.
 */

public class ChunkToken {
    String id;
    NotesToken[] tn;

    public String getId() {
        return id;
    }

    public NotesToken[] getNotes() {
        return tn;
    }
}
