package wycliffeassociates.recordingapp.Playback;

/**
 * Created by leongv on 9/21/2016.
 */
public class VerseMarker {

    public static int STATIC = 0;
    public static int DYNAMIC = 1;

    private int mNumber;
    private int mType;

    public VerseMarker(int number, int type) {
        setNumber(number);
        mType = type;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public void draw(float startPosX, float startPosY) {

    }

}
