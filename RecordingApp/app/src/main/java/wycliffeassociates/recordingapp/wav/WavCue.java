package wycliffeassociates.recordingapp.wav;

/**
 * Created by sarabiaj on 10/4/2016.
 */
public class WavCue {
    String mLabel = null;
    Long mLocation = null;

    public WavCue(String label, long location) {
        mLabel = label;
        mLocation = location;
    }

    public WavCue(long loc) {
        mLocation = loc;
    }

    public WavCue(String label) {
        mLabel = label;
    }

    public long getLoctionInMilliseconds() {
        return (long) (mLocation / 44.1);
    }

    public String getLabel() {
        return mLabel;
    }

    public long getLocation() {
        return mLocation;
    }

    public void setLocation(long loc) {
        mLocation = loc;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public boolean complete(){
        return (mLabel != null && mLocation != null);
    }
}
