package org.wycliffeassociates.translationrecorder.Playback.overlays;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by Parrot on 11/22/16.
 */

public class TimecodeLayer extends View {

    private Paint mPaintGrid = new Paint();
    private Paint mPaintText = new Paint();
    private double secondsPerPixel = 230;
    private double timecodeInterval= 200;
    private int audioLength =13040;

    char[] timecodeStr;

    public static TimecodeLayer newInstance(Context context){
        TimecodeLayer view = new TimecodeLayer(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    public TimecodeLayer(Context context) {
        super(context);
        timecodeStr = new char[5];
        mPaintText.setColor(getResources().getColor(R.color.minimap_timecode));
        mPaintText.setTextSize(18.f);

        mPaintGrid = new Paint();
        mPaintGrid.setColor(Color.GRAY);
        mPaintGrid.setStyle(Paint.Style.STROKE);
        mPaintGrid.setStrokeWidth(1f);
    }

    private void computeTimecodeInterval(){
        timecodeInterval = 1.0;
        if(timecodeInterval / secondsPerPixel >= 50){
            return;
        }
        else{
            timecodeInterval = 0.d;
        }
        while(timecodeInterval / secondsPerPixel < 50){
            timecodeInterval += 5.0;
        }
    }

    public void setAudioLength(int lengthMs){
        //System.out.println("Audio data length for timecode is " + length);
        this.audioLength = (int)(lengthMs/1000.0);
        this.secondsPerPixel = audioLength / (double)getWidth();
        computeTimecodeInterval();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //System.out.println("secondsPerPixel is " + secondsPerPixel + " interval is " + timecodeInterval);

        int i = 0;
        double fractionalSecs = secondsPerPixel;
        int integerTimecode = (int) (fractionalSecs / timecodeInterval);
        Rect bounds = new Rect();
        while (i < getWidth()){

            i++;
            fractionalSecs += secondsPerPixel;
            int integerSecs = (int) fractionalSecs;

            int integerTimecodeNew = (int) (fractionalSecs /
                    timecodeInterval);
            if (integerTimecodeNew != integerTimecode) {
                integerTimecode = integerTimecodeNew;

                //System.out.println("integer is " + integerSecs);
                // Turn, e.g. 67 seconds into "1:07"
//                String timecodeMinutes = String.format("%d",integerSecs / 60);
//                String timecodeSeconds = String.format("%02d", integerSecs % 60);
                int timecodeMinutes = integerSecs / 60;
                int timecodeSeconds = integerSecs % 60;

//                if ((integerSecs % 60) < 10) {
//                    timecodeSeconds = "0" + timecodeSeconds;
//                }
                //String timecodeStr = timecodeMinutes + ":" + timecodeSeconds;

                timecodeStr[0] = (char)('0' + timecodeMinutes / 10);
                timecodeStr[1] = (char)('0' + timecodeMinutes % 10);
                timecodeStr[2] = ':';
                timecodeStr[3] = (char)('0' + timecodeSeconds / 10);
                timecodeStr[4] = (char)('0' + timecodeSeconds % 10);


                mPaintText.getTextBounds(timecodeStr, 0, timecodeStr.length, bounds);
                float padding = getResources().getDimension(R.dimen.default_padding_xs);
                float xOffset = mPaintText.measureText(timecodeStr, 0, timecodeStr.length) + padding;
                float yOffset = bounds.height() + padding;

                canvas.drawText(timecodeStr, 0, timecodeStr.length, i - xOffset, yOffset, mPaintText);
                canvas.drawLine(i, 0.f, i, getHeight(), mPaintGrid);
            }
        }
    }
}
