package org.wycliffeassociates.translationrecorder.wav;

/**
 * Created by sarabiaj on 10/4/2016.
 */
public class WavCue {
    String mLabel = null;
    Integer mLocation = null;

    public WavCue(String label, int location) {
        mLabel = label;
        mLocation = location;
    }

    public WavCue(int loc) {
        mLocation = loc;
    }

    public WavCue(String label) {
        mLabel = label;
    }

    public int getLoctionInMilliseconds() {
        return (int) (mLocation / 44.1);
    }

    public String getLabel() {
        return mLabel;
    }

    public int getLocation() {
        return mLocation;
    }

    public void setLocation(int loc) {
        mLocation = loc;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public boolean complete(){
        return (mLabel != null && mLocation != null);
    }
}
