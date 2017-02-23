package org.wycliffeassociates.translationrecorder.AudioVisualization;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import org.wycliffeassociates.translationrecorder.AudioInfo;
import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 3/29/2016.
 */
public class VolumeBar extends CanvasView {

    public int mDb = 0;
    int dbNone;
    int dbLow;
    int dbGood;
    int dbHigh;
    int dbMax;


    public VolumeBar(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        dbNone = getDbLevel(0);
        dbLow = getDbLevel(2067);    // -24 decibel
        dbGood = getDbLevel(4125);   // -18 decibel
        dbHigh = getDbLevel(23197);  // -3 decibel
        dbMax = getDbLevel(AudioInfo.AMPLITUDE_RANGE);
    }

    @Override
    public void onDraw(Canvas c){
        super.onDraw(c);
        drawBar(c);
    }

    public void setDb(int db){
        mDb = db;
    }

    public void drawBar(Canvas c){
        int currentDb = getDbLevel(mDb);
        int currentDbNeg = getDbLevel(mDb * -1);
        chooseColor(currentDb);
        mPaintGrid.setStyle(Paint.Style.FILL);
        c.drawRect(0, dbNone, getWidth(), currentDb, mPaintGrid);
        c.drawRect(0, currentDbNeg, getWidth(), dbNone, mPaintGrid);
    }

    private int getDbLevel(int val){
        return (int)(val / (double)AudioInfo.AMPLITUDE_RANGE * getHeight()/2 + getHeight()/2);
    }

    private void chooseColor(int currentDb){
        if (currentDb < dbLow) {
            mPaintGrid.setColor(getResources().getColor(R.color.volume_base));
        } else if (currentDb < dbGood) {
            mPaintGrid.setColor(getResources().getColor(R.color.volume_low));
        } else if (currentDb < dbHigh) {
            mPaintGrid.setColor(getResources().getColor(R.color.volume_good));
        } else if (currentDb < dbMax) {
            mPaintGrid.setColor(getResources().getColor(R.color.volume_high));
        } else {
            mPaintGrid.setColor(getResources().getColor(R.color.volume_clipped));
        }
    }
}
