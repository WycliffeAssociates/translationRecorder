package wycliffeassociates.recordingapp.Playback.interfaces;

import java.util.Collection;

import wycliffeassociates.recordingapp.Playback.fragments.FragmentPlaybackTools;
import wycliffeassociates.recordingapp.Playback.overlays.DraggableViewFrame;
import wycliffeassociates.recordingapp.widgets.DraggableMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarker;
import wycliffeassociates.recordingapp.widgets.VerseMarker;

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
}
