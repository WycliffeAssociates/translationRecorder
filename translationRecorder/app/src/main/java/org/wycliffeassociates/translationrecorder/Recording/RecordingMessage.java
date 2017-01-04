package org.wycliffeassociates.translationrecorder.Recording;

/**
 * Created by jsarabia on 7/24/15.
 */

//Contains data and flags to be passed as messages in a BlockingQueue
public class RecordingMessage {
    private byte[] data;
    private boolean paused;
    private boolean stopped;

    public RecordingMessage(byte[] data, boolean paused, boolean stopped){
        if(data != null){
            this.data = data.clone();
        }
        else {
            data = null;
        }
        this.paused = paused;
        this.stopped = stopped;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
