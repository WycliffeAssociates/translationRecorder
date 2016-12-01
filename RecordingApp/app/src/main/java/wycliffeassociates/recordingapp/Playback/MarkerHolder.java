package wycliffeassociates.recordingapp.Playback;

import android.view.View;
import android.widget.FrameLayout;

import java.util.Collection;
import java.util.HashMap;

import wycliffeassociates.recordingapp.Playback.interfaces.MarkerMediator;
import wycliffeassociates.recordingapp.Playback.overlays.DraggableViewFrame;
import wycliffeassociates.recordingapp.widgets.DraggableMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarker;
import wycliffeassociates.recordingapp.widgets.VerseMarker;

/**
 * Created by sarabiaj on 11/30/2016.
 */

public class MarkerHolder implements MarkerMediator {

    FrameLayout mDraggableViewFrame;
    AudioVisualController mAudioController;

    HashMap<Integer, DraggableMarker> mMarkers = new HashMap<>();
    public static final int START_MARKER_ID = -1;
    public static final int END_MARKER_ID = -2;


    public MarkerHolder(AudioVisualController controller){
        mAudioController = controller;
    }

    public void setDraggableViewFrame(DraggableViewFrame dvf){
        mDraggableViewFrame = dvf;
    }

    public void onAddVerseMarker(int verseNumber, VerseMarker marker){
        addMarker(verseNumber, marker);
    }

    public VerseMarker getVerseMarker(int verseNumber){
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

    @Override
    public void onRemoveVerseMarker(int verseNumber) {
        removeMarker(verseNumber);
    }

    @Override
    public void onRemoveSectionMarkers() {
        onRemoveStartSectionMarker();
        onRemoveEndSectionMarker();
        mAudioController.clearLoopPoints();
    }

    @Override
    public void onRemoveStartSectionMarker() {
        removeMarker(START_MARKER_ID);
    }

    @Override
    public void onRemoveEndSectionMarker() {
        removeMarker(END_MARKER_ID);
    }

    private void removeMarker(int id){
        if(mMarkers.containsKey(id)){
            View marker = mMarkers.get(id).getView();
            mDraggableViewFrame.removeView(marker);
            mMarkers.remove(id);
        }
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

    @Override
    public void updateCurrentFrame(int frame) {

    }

    public DraggableMarker getMarker(int id){
        return mMarkers.get(id);
    }

    public boolean contains(int id){
        return mMarkers.containsKey(id);
    }

    @Override
    public void onCueScroll(int id, float distX) {
        int position = mAudioController.getLocationInFrames() + ((int)(distX-240) * 230);
        position = Math.min(position, 0);
        if(id == START_MARKER_ID) {
            mAudioController.setStartMarker(position);
        } else if (id == END_MARKER_ID) {
            mAudioController.setEndMarker(position);
        }
        mMarkers.get(id).updateX(position, mDraggableViewFrame.getWidth());
        mDraggableViewFrame.postInvalidate();
    }

    @Override
    public void updateStartMarkerFrame(int frame) {
        updateMarkerFrame(START_MARKER_ID, frame);
    }

    @Override
    public void updateEndMarkerFrame(int frame) {
        updateMarkerFrame(END_MARKER_ID, frame);
    }

    private void updateMarkerFrame(int id, int frame) {
        if(mMarkers.containsKey(id)) {
            mMarkers.get(id).updateX(frame, mDraggableViewFrame.getWidth());
        }
        mDraggableViewFrame.postInvalidate();
    }
}
