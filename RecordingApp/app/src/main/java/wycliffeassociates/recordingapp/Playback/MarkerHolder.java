package wycliffeassociates.recordingapp.Playback;

import android.widget.FrameLayout;

import java.util.Collection;
import java.util.HashMap;

import wycliffeassociates.recordingapp.Playback.interfaces.MarkerMediator;
import wycliffeassociates.recordingapp.widgets.DraggableMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarker;
import wycliffeassociates.recordingapp.widgets.VerseMarker;

/**
 * Created by sarabiaj on 11/30/2016.
 */

public class MarkerHolder implements MarkerMediator {

    FrameLayout mDraggableViewFrame;

    HashMap<Integer, DraggableMarker> mMarkers = new HashMap<>();
    public static final int START_MARKER_ID = -1;
    public static final int END_MARKER_ID = -2;


    public void onAddVerseMarker(int verseNumber, VerseMarker marker){
        addMarker(verseNumber, marker);
    }

    public wycliffeassociates.recordingapp.widgets.VerseMarker getVerseMarker(int verseNumber){
        if(verseNumber > 0) {
            return (wycliffeassociates.recordingapp.widgets.VerseMarker) mMarkers.get(verseNumber);
        }
        return null;
    }

    public void onAddStartSectionMarker(SectionMarker marker) {
        addMarker(START_MARKER_ID, marker);
    }

    public void onAddEndSectionMarker(SectionMarker marker) {
        addMarker(END_MARKER_ID, marker);
    }

    private void addMarker(int id, DraggableMarker marker){
        if(mMarkers.get(id) != null) {
            if(mDraggableViewFrame != null) {
                mDraggableViewFrame.removeView(mMarkers.get(id).getView());
            }
        }
        mMarkers.put(id, marker);
        if(mDraggableViewFrame != null) {
            mDraggableViewFrame.addView(marker.getView());
        }
    }

    public SectionMarker getSectionMarker(int sectionMarkerId) {
        if(sectionMarkerId != START_MARKER_ID || sectionMarkerId != END_MARKER_ID){
            return null;
        } else {
            return (SectionMarker) mMarkers.get(sectionMarkerId);
        }
    }

    public boolean sectionMarkersAreSet(){
        return (mMarkers.containsKey(START_MARKER_ID) && mMarkers.containsKey(END_MARKER_ID));
    }

    public void updateVerseMarkerLocation(int verseNumber, int frame){
        updateMarkerLocation(verseNumber, frame);
    }

    public void updateStartMarkerLocation(int frame){
        updateMarkerLocation(START_MARKER_ID, frame);
    }

    public void updateEndMarkerLocation(int frame){
        updateMarkerLocation(END_MARKER_ID, frame);
    }

    private void updateMarkerLocation(int id, int frame){
        DraggableMarker marker = mMarkers.get(id);
        if(marker != null) {
            marker.updateX(frame, mDraggableViewFrame.getWidth());
        }
    }

    public Collection<DraggableMarker> getMarkers(){
        return mMarkers.values();
    }

}
