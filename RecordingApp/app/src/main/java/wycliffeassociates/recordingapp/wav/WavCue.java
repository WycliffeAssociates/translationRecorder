package wycliffeassociates.recordingapp.wav;

/**
 * Created by sarabiaj on 10/4/2016.
 */
public class WavCue {
    String mLabel;
    long mLocation;

    public WavCue(String label, long location){
        mLabel = label;
        mLocation = location;
    }

    public long getLoctionInMilliseconds(){
        return (long)(mLocation / 44.1);
    }

    public String getLabel(){
        return mLabel;
    }

    public long getLocation(){
        return mLocation;
    }
}
