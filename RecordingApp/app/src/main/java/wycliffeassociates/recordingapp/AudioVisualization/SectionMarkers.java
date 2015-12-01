package wycliffeassociates.recordingapp.AudioVisualization;

import wycliffeassociates.recordingapp.Playback.WavPlayer;

/**
 * Created by sarabiaj on 11/20/2015.
 */
public class SectionMarkers {

    private  int minimapStart = 0;
    private  int minimapEnd = 0;
    private boolean playSelectedSection = false;
    private int mainEnd;
    private int mainStart;
    private boolean setStart = false;
    private boolean setEnd = false;


    public boolean shouldPlaySelectedSection() {
        return playSelectedSection;
    }

    public boolean shouldDrawMarkers(){
        return playSelectedSection || setStart || setEnd;
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
        mainStart = x;
        minimapStart = (int)((x / (double) WavPlayer.getDuration()) * width);
        setStart = true;
        if(setStart && setEnd){
            playSelectedSection = true;
        }
    }

    public void setEndTime(int x, int width){
        mainEnd = x;
        minimapEnd = (int)((x / (double) WavPlayer.getDuration()) * width);
        setEnd = true;
        if(setStart && setEnd){
            playSelectedSection = true;
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
}
