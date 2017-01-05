package org.wycliffeassociates.translationrecorder.Playback.interfaces;

/**
 * Created by Joe on 11/7/2016.
 */

public interface MediaControlReceiver {
    void play();
    void pause();
    void seekNext();
    void seekPrevious();
    int getAbsoluteLocationMs();
    int getRelativeDurationMs();
}
