package wycliffeassociates.recordingapp.Playback.interfaces;

/**
 * Created by Joe on 11/7/2016.
 */

public interface MediaController {
    void onPlay();
    void onPause();
    void onSeekForward();
    void onSeekBackward();
    int getDuration();
    int getLocation();
    int setOnCompleteListner(Runnable onComplete);
}
