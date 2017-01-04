package org.wycliffeassociates.translationrecorder.AudioVisualization;

/**
 * Created by sarabiaj on 9/4/2015.
 */
public class RecordingTimer {
    long startTime;
    long timeStored;
    boolean paused;

    public RecordingTimer(){
        startTime = 0;
        timeStored = 0;
        paused = false;
    }

    public void startTimer(){
        startTime = System.currentTimeMillis();
    }

    public long getTimeElapsed(){
        long elapsed;
        elapsed = System.currentTimeMillis() - startTime + timeStored;
        if(paused){
            return timeStored;
        } else {
            return elapsed;
        }
    }

    public void pause(){
        timeStored = System.currentTimeMillis() - startTime + timeStored;
        paused = true;
    }

    public void resume(){
        startTime = System.currentTimeMillis();
        paused = false;
    }

}
