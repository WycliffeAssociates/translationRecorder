package org.wycliffeassociates.translationrecorder.Playback.markers;

import android.view.View;
import android.widget.FrameLayout;

import org.wycliffeassociates.translationrecorder.Playback.AudioVisualController;
import org.wycliffeassociates.translationrecorder.Playback.PlaybackActivity;
import org.wycliffeassociates.translationrecorder.Playback.fragments.FragmentPlaybackTools;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MarkerMediator;
import org.wycliffeassociates.translationrecorder.Playback.overlays.DraggableViewFrame;
import org.wycliffeassociates.translationrecorder.wav.WavCue;
import org.wycliffeassociates.translationrecorder.widgets.marker.DraggableMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.SectionMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.VerseMarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sarabiaj on 11/30/2016.
 */

public class MarkerHolder implements MarkerMediator {

    FrameLayout mDraggableViewFrame;
    AudioVisualController mAudioController;
    PlaybackActivity mActivity;
    int mTotalVerses;
    HashMap<Integer, DraggableMarker> mMarkers = new HashMap<>();
    public static final int START_MARKER_ID = -1;
    public static final int END_MARKER_ID = -2;
    FragmentPlaybackTools mMarkerButtons;

    public MarkerHolder(AudioVisualController controller, PlaybackActivity activity, FragmentPlaybackTools playbackTools, int totalVerses) {
        mAudioController = controller;
        mActivity = activity;
        mMarkerButtons = playbackTools;
        mTotalVerses = totalVerses;
    }

    public void setMarkerButtons(FragmentPlaybackTools playbackTools) {
        mMarkerButtons = playbackTools;
    }

    public void setDraggableViewFrame(DraggableViewFrame dvf) {
        mDraggableViewFrame = dvf;
    }

    public void onAddVerseMarker(int verseNumber, VerseMarker marker) {
        addMarker(verseNumber, marker);
    }

    public VerseMarker getVerseMarker(int verseNumber) {
        if (verseNumber > 0) {
            return (VerseMarker) mMarkers.get(verseNumber);
        }
        return null;
    }

    public void onAddStartSectionMarker(SectionMarker marker) {
        addMarker(START_MARKER_ID, marker);
        mMarkerButtons.viewOnSetStartMarker();
    }

    public void onAddEndSectionMarker(SectionMarker marker) {
        addMarker(END_MARKER_ID, marker);
        mMarkerButtons.viewOnSetEndMarker();
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
        mActivity.onLocationUpdated();
    }

    @Override
    public void onRemoveStartSectionMarker() {
        removeMarker(START_MARKER_ID);
    }

    @Override
    public void onRemoveEndSectionMarker() {
        removeMarker(END_MARKER_ID);
    }

    private void removeMarker(int id) {
        if (mMarkers.containsKey(id)) {
            View marker = mMarkers.get(id).getView();
            mDraggableViewFrame.removeView(marker);
            mMarkers.remove(id);
        }
        mAudioController.setCueList(getCueLocationList());
        mActivity.onLocationUpdated();
    }

    private synchronized void addMarker(int id, DraggableMarker marker) {
        if (mMarkers.get(id) != null) {
            if (mDraggableViewFrame != null) {
                mDraggableViewFrame.removeView(mMarkers.get(id).getView());
            }
        }
        mMarkers.put(id, marker);
        if (mDraggableViewFrame != null) {
            mDraggableViewFrame.addView(marker.getView());
        }
        mAudioController.setCueList(getCueLocationList());
        mActivity.onLocationUpdated();
    }

    public SectionMarker getSectionMarker(int sectionMarkerId) {
        if (sectionMarkerId != START_MARKER_ID || sectionMarkerId != END_MARKER_ID) {
            return null;
        } else {
            return (SectionMarker) mMarkers.get(sectionMarkerId);
        }
    }

    public boolean sectionMarkersAreSet() {
        return (mMarkers.containsKey(START_MARKER_ID) && mMarkers.containsKey(END_MARKER_ID));
    }

    public void updateVerseMarkerLocation(int verseNumber, int frame) {
        updateMarkerLocation(verseNumber, frame);
    }

    public void updateStartMarkerLocation(int frame) {
        updateMarkerLocation(START_MARKER_ID, frame);
    }

    public void updateEndMarkerLocation(int frame) {
        updateMarkerLocation(END_MARKER_ID, frame);
    }

    private void updateMarkerLocation(int id, int frame) {
        DraggableMarker marker = mMarkers.get(id);
        if (marker != null) {
            marker.updateX(frame, mDraggableViewFrame.getWidth());
        }
    }

    public Collection<DraggableMarker> getMarkers() {
        return mMarkers.values();
    }

    @Override
    public void updateCurrentFrame(int frame) {
        for (DraggableMarker m : mMarkers.values()) {
            m.updateX(frame, mDraggableViewFrame.getWidth());
        }
    }

    public DraggableMarker getMarker(int id) {
        return mMarkers.get(id);
    }

    public boolean contains(int id) {
        return mMarkers.containsKey(id);
    }

    @Override
    public void onCueScroll(int id, float newXPos) {
        DraggableMarker selectedMarker = mMarkers.get(id);
        float fpp = getFramesPerPixel();
        int position = selectedMarker.getFrame() + ((int) Math.round((newXPos - selectedMarker.getMarkerX()) * fpp));
        position = Math.min(Math.max(position, 0), mAudioController.getAbsoluteDurationInFrames());
        if (id == START_MARKER_ID) {
            mAudioController.setStartMarker(position);
        } else if (id == END_MARKER_ID) {
            mAudioController.setEndMarker(position);
        }
        selectedMarker.updateFrame(position);
        mAudioController.setCueList(getCueLocationList());
        mActivity.onLocationUpdated();
    }

    private float getFramesPerPixel(){
        return (44100 * 10) / (float) mDraggableViewFrame.getWidth();
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
        if (mMarkers.containsKey(id)) {
            mMarkers.get(id).updateFrame(frame);
            mMarkers.get(id).updateX(frame, mDraggableViewFrame.getWidth());
        }
        mAudioController.setCueList(getCueLocationList());
    }

    @Override
    public boolean hasVersesRemaining() {
        return numVersesRemaining() > 0;
    }

    @Override
    public int numVersesRemaining() {
        return mTotalVerses - numVerseMarkersPlaced();
    }

    public int numVerseMarkersPlaced(){
        int markers = mMarkers.size();
        if(mMarkers.containsKey(START_MARKER_ID)) {
            markers--;
        }
        if(mMarkers.containsKey(END_MARKER_ID)){
            markers--;
        }
        return markers;
    }

    public int availableMarkerNumber(int startVerse, int endVerse) {
        for(int i=startVerse; i <= endVerse; i++) {
            if(!contains(i)) {
                return i;
            }
        }

        throw new IllegalStateException(
                String.format("No markers available to insert in range of verses %s - %s", startVerse, endVerse)
        );
    }

    public boolean hasSectionMarkers(){
        return mMarkers.containsKey(START_MARKER_ID) || mMarkers.containsKey(END_MARKER_ID);
    }

    @Override
    public List<WavCue> getCueLocationList() {
        Collection<DraggableMarker> markers = mMarkers.values();
        ArrayList<WavCue> cueList = new ArrayList<>();
        for(DraggableMarker marker : markers) {
            if (marker instanceof VerseMarker) {
                cueList.add(new WavCue(marker.getFrame()));
            }
        }
        Collections.sort(cueList, new Comparator<WavCue>() {
            @Override
            public int compare(WavCue wavCue, WavCue t1) {
                return Integer.compare(wavCue.getLocation(), t1.getLocation());
            }
        });
        return cueList;
    }
}
