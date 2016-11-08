package wycliffeassociates.recordingapp.Playback.interfaces;

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
    void setOnCompleteListner(Runnable onComplete);
}
