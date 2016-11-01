package wycliffeassociates.recordingapp.widgets;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

/**
 * Created by sarabiaj on 11/1/2016.
 */

public class PlaybackTimer {

    TextView mElapsed, mDuration;
    Handler mHandler;

    public PlaybackTimer(TextView elapsed, TextView duration){
        mElapsed = elapsed;
        mDuration = duration;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setDuration(int duration){
        final String durationString = convertTimeToString(duration);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDuration.setText(durationString);
                mDuration.invalidate();
            }
        });
    }

    public void setElapsed(int elapsed){
        final String durationString = convertTimeToString(elapsed);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mElapsed.setText(durationString);
                mElapsed.postInvalidate();
            }
        });
    }

    private String convertTimeToString(int time) {
        return String.format("%02d:%02d:%02d", time / 3600000, (time / 60000) % 60, (time / 1000) % 60);
    }
}
