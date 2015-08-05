package wycliffeassociates.recordingapp.model;

import android.app.Application;

import java.util.ArrayList;

public class AudioItemHolder extends Application {
    private ArrayList<AudioItem> audioList;

    public ArrayList<AudioItem> getData(){ return audioList; }
    public void setData(ArrayList<AudioItem> audioList){ this.audioList = audioList; }

}