package wycliffeassociates.recordingapp.AudioVisualization.Utils;

import android.util.Log;

import wycliffeassociates.recordingapp.AudioInfo;

/**
 * Created by sarabiaj on 1/14/2016.
 */
public class U {
    public static double computeDb(double value){
        value+=.0000001;
        value = Math.abs(value);
        double db = Math.log10(value / (double) AudioInfo.AMPLITUDE_RANGE)* 20;
        return Math.max(db, -100);
    }

    public static float getValueForScreen(double value, int height){
        float out = (float)(value * (height/(double)(2*AudioInfo.AMPLITUDE_RANGE)) +height/2);
        System.out.println(" val is " + value + " range is " + AudioInfo.AMPLITUDE_RANGE);
        return out;
    }

    private static int sign(double value){
        if(value > 0){
            return 1;
        } else {
            return -1;
        }
    }
}
