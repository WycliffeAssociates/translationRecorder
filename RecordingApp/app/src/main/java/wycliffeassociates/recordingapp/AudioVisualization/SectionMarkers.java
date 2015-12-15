package wycliffeassociates.recordingapp.AudioVisualization;

import wycliffeassociates.recordingapp.Playback.WavPlayer;

/**
 * Created by sarabiaj on 11/20/2015.
 */
public class SectionMarkers {

    private  int minimapStart = Integer.MIN_VALUE;
    private  int minimapEnd = Integer.MAX_VALUE;
    private boolean playSelectedSection = false;
    private int mainEnd = Integer.MAX_VALUE;
    private int mainStart = Integer.MIN_VALUE;
    private boolean startSet = false;
    private boolean endSet = false;

    public boolean bothSet() {
        return playSelectedSection;
    }

    public boolean shouldDrawMarkers(){
        return playSelectedSection || startSet || endSet;
    }

    public int getMinimapMarkerStart() {
        return minimapStart;
    }

    public int getMinimapMarkerEnd() {
        return minimapEnd;
    }

    public void setPlaySelectedSection(boolean x){
        playSelectedSection = x;
    }

    public void setStartTime(int x, int width){
        WavPlayer.startSectionAt(x);
        mainStart = x;
        minimapStart = (int)((x / (double) WavPlayer.getDuration()) * width);
        startSet = true;
        swapIfNeeded();
        if(startSet && endSet){
            playSelectedSection = true;
        }
    }

    public void setEndTime(int x, int width){
        mainEnd = x;
        minimapEnd = (int)((x / (double) WavPlayer.getDuration()) * width);
        endSet = true;
        swapIfNeeded();
        if(startSet && endSet){
            playSelectedSection = true;
        }
    }

    public void swapIfNeeded(){
        if(mainStart > mainEnd){
            int temp = mainEnd;
            mainEnd = mainStart;
            mainStart = temp;
            temp = minimapEnd;
            minimapEnd = minimapStart;
            minimapEnd = temp;
        }
    }

    public void setMinimapMarkers(int start, int end){
        minimapStart = start;
        minimapEnd = end;
        playSelectedSection = true;
    }

    public void setMainMarkers(int start, int end){
        mainStart = start;
        mainEnd = end;
    }

    public int getStartLocation(){
        return mainStart;
    }

    public int getEndLocation(){
        return mainEnd;
    }

    public void clearMarkers(){
        playSelectedSection = false;
        endSet = false;
        startSet = false;
        minimapEnd = Integer.MAX_VALUE;
        minimapStart = Integer.MIN_VALUE;
        mainEnd = Integer.MAX_VALUE;
        mainStart = Integer.MIN_VALUE;
        WavPlayer.setOnlyPlayingSection(false);
    }
}
