package org.wycliffeassociates.translationrecorder.Playback.interfaces;

/**
 * Created by Joe on 11/7/2016.
 */

public interface MediaController {
    void onMediaPlay();
    void onMediaPause();
    void onSeekForward();
    void onSeekBackward();
    int getDuration();
    int getLocation();
    int getDurationInFrames();
    int getLocationInFrames();
    void setOnCompleteListner(Runnable onComplete);
    void onSeekTo(float time);
    void setStartMarkerAt(int frame);
    void setEndMarkerAt(int frame);
    int getStartMarkerFrame();
    int getEndMarkerFrame();
    boolean hasSetMarkers();
}
