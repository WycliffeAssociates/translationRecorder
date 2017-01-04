package org.wycliffeassociates.translationrecorder.Playback.interfaces;

/**
 * Created by sarabiaj on 11/8/2016.
 */

public interface AudioEditDelegator {
    void onSave();
    void onCut();
    void onDropStartMarker();
    void onDropEndMarker();
    void onClearMarkers();
    void onDropVerseMarker();
    void onUndo();
}
