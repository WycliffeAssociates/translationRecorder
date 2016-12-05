package wycliffeassociates.recordingapp.Playback.markers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wycliffeassociates.recordingapp.wav.WavCue;

/**
 * Created by sarabiaj on 12/5/2016.
 */

public class MarkerState {

    private int mTotalVerses;
    private HashMap<Integer, WavCue> mMarkers;

    public MarkerState(int totalVerses){
        mTotalVerses = totalVerses;
        mMarkers = new HashMap<>();
    }

    public MarkerState(int totalVerses, List<WavCue> markers){
        mTotalVerses = totalVerses;
        for(WavCue cue : markers) {
            mMarkers.put(Integer.parseInt(cue.getLabel()), cue);
        }
    }

    public void addMarker(int verse, WavCue marker){
        mMarkers.put(verse, marker);
    }

    public void removeMarker(int verse){
        mMarkers.remove(verse);
    }

    public int numMarkersRemaining(){
        return mTotalVerses - mMarkers.size();
    }
}
