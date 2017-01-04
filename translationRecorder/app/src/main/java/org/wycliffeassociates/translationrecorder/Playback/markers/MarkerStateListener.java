package org.wycliffeassociates.translationrecorder.Playback.markers;

/**
 * Created by sarabiaj on 12/5/2016.
 */

public interface MarkerStateListener {
    void onMarkerAdded();
    void onMarkerRemoved();
    void onMarkerMoved();
}
