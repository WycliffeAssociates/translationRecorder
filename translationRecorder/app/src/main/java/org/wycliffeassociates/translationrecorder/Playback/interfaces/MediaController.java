package org.wycliffeassociates.translationrecorder.Playback.interfaces;

/**
 * Created by Joe on 11/7/2016.
 */

public interface MediaController {
    void onMediaPlay();
    void onMediaPause();
    void onSeekForward();
    void onSeekBackward();
    //The durations and locations here are intended to be relative, ideally to abstract away
    //relative vs absolute times/frames
    int getDurationMs();
    int getLocationMs();
    int getDurationInFrames();
    int getLocationInFrames();
    void setOnCompleteListner(Runnable onComplete);
    void onSeekTo(float time);
    void setStartMarkerAt(int frame);
    void setEndMarkerAt(int frame);
    int getStartMarkerFrame();
    int getEndMarkerFrame();
    boolean hasSetMarkers();
    boolean isPlaying();
    boolean isInVerseMarkerMode();
    boolean isInEditMode();
}
