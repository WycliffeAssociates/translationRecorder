package wycliffeassociates.recordingapp.FileManagerUtils;

import android.app.Application;

import java.util.ArrayList;

public class AudioItemHolder extends Application {
    private ArrayList<FileItem> audioList;

    public ArrayList<FileItem> getData(){ return audioList; }
    public void setData(ArrayList<FileItem> audioList){ this.audioList = audioList; }

}