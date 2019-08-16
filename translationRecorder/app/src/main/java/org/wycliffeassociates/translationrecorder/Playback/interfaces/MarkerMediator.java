package org.wycliffeassociates.translationrecorder.Playback.interfaces;

import org.wycliffeassociates.translationrecorder.Playback.fragments.FragmentPlaybackTools;
import org.wycliffeassociates.translationrecorder.Playback.overlays.DraggableViewFrame;
import org.wycliffeassociates.translationrecorder.wav.WavCue;
import org.wycliffeassociates.translationrecorder.widgets.marker.DraggableMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.SectionMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.VerseMarker;

import java.util.Collection;
import java.util.List;

/**
 * Created by sarabiaj on 11/30/2016.
 */

public interface MarkerMediator {
    void onAddVerseMarker(int verseNumber, VerseMarker marker);
    void onAddStartSectionMarker(SectionMarker marker);
    void onAddEndSectionMarker(SectionMarker marker);
    void onRemoveVerseMarker(int verseNumber);
    void onRemoveStartSectionMarker();
    void onRemoveEndSectionMarker();
    Collection<DraggableMarker> getMarkers();
    void updateCurrentFrame(int frame);
    DraggableMarker getMarker(int id);
    boolean contains(int id);
    void onCueScroll(int id, float distX);
    void setDraggableViewFrame(DraggableViewFrame mFrame);
    void onRemoveSectionMarkers();
    void updateStartMarkerFrame(int frame);
    void updateEndMarkerFrame(int frame);
    void setMarkerButtons(FragmentPlaybackTools playbackTools);
    boolean hasVersesRemaining();
    int numVersesRemaining();
    int numVerseMarkersPlaced();
    int availableMarkerNumber(int startVerse, int endVerse);
    boolean hasSectionMarkers();
    List<WavCue> getCueLocationList();
}
