package org.wycliffeassociates.translationrecorder.Playback.interfaces;

import java.nio.ShortBuffer;
import java.util.List;

/**
 * Created by Joe on 11/7/2016.
 */

public interface AudioStateCallback {
    void onPlayerPaused();
    void onLocationUpdated();
    void onVisualizationLoaded(List<ShortBuffer> mappedVisualizationFile);
}
