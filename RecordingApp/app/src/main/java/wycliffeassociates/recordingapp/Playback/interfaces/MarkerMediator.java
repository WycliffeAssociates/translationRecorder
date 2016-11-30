package wycliffeassociates.recordingapp.Playback.interfaces;

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
}
