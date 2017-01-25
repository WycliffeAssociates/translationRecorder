package org.wycliffeassociates.translationrecorder.Playback.interfaces;

import java.nio.MappedByteBuffer;

/**
 * Created by Joe on 11/7/2016.
 */

public interface AudioStateCallback {
    void onPlayerPaused();
    void onLocationUpdated();
    void onVisualizationLoaded(MappedByteBuffer mappedVisualizationFile);
}
