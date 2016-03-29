package wycliffeassociates.recordingapp.AudioVisualization;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;


import wycliffeassociates.recordingapp.AudioInfo;

/**
 * Created by sarabiaj on 3/29/2016.
 */
public class VolumeBar extends CanvasView{

    public int mDb = 0;
    int db3;
    int ndb3;
    int db6;
    int ndb6;
    int db12;
    int ndb12;
    int db18;
    int ndb18;
    int db24;
    int ndb24;
    int db0;

    public VolumeBar(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public void initDB(){
        db3 = dBLine(23197);
        ndb3 = dBLine(-23197);
        db6 = dBLine(16422);
        ndb6 = dBLine(-16422);
        db12 = dBLine(8230);
        ndb12 = dBLine(-8230);
        db18 = dBLine(4125);
        ndb18 = dBLine(-4125);
        db24 = dBLine(2067);
        ndb24 = dBLine(-2067);
        db0 = dBLine(0);
    }

    public void setDb(int db){
        mDb = db;
    }

    public void showVolumeBar(int db, int rectBase){
        if(dBLine(db) > rectBase){
            if(rectBase == db3){
                mPaintGrid.setStyle(Paint.Style.FILL);
                mPaintGrid.setAlpha(30);
                mPaintGrid.setColor(Color.RED);
            } else if(rectBase == db6){
                mPaintGrid.setStyle(Paint.Style.FILL);
                mPaintGrid.setAlpha(30);
                mPaintGrid.setColor(Color.YELLOW);
            } else if(rectBase == db12){
                mPaintGrid.setStyle(Paint.Style.FILL);
                mPaintGrid.setAlpha(30);
                mPaintGrid.setColor(Color.argb(100,50,205,50));
            } else if (rectBase == db18){
                mPaintGrid.setStyle(Paint.Style.FILL);
                mPaintGrid.setAlpha(30);
                mPaintGrid.setColor(Color.argb(100,0,100,0));
            } else if (rectBase == db24){
                mPaintGrid.setStyle(Paint.Style.FILL);
                mPaintGrid.setAlpha(30);
                mPaintGrid.setColor(Color.CYAN);
            } else if (rectBase == db0){
                mPaintGrid.setStyle(Paint.Style.FILL);
                mPaintGrid.setAlpha(30);
                mPaintGrid.setColor(Color.BLUE);
            }
        } else {
            mPaintGrid.setColor(Color.GRAY);
            mPaintGrid.setStyle(Paint.Style.STROKE);
        }
    }

    public void drawDbLines(Canvas c){

        showVolumeBar(mDb, db0);
        c.drawRect(0, db0, getWidth(), db24, mPaintGrid);
        c.drawRect(0, ndb24, getWidth(), db0, mPaintGrid);

        showVolumeBar(mDb, db24);
        c.drawRect(0, db24, getWidth(), db18, mPaintGrid);
        c.drawRect(0, ndb18, getWidth(), ndb24, mPaintGrid);

        showVolumeBar(mDb, db18);
        c.drawRect(0, db18, getWidth(), db12, mPaintGrid);
        c.drawRect(0, ndb12, getWidth(), ndb18, mPaintGrid);

        showVolumeBar(mDb, db12);
        c.drawRect(0, db12, getWidth(), db6, mPaintGrid);
        c.drawRect(0, ndb6, getWidth(), ndb12, mPaintGrid);

        showVolumeBar(mDb, db6);
        c.drawRect(0, db6, getWidth(), db3, mPaintGrid);
        c.drawRect(0, ndb3, getWidth(), ndb6, mPaintGrid);

        showVolumeBar(mDb, db3);
        c.drawRect(0, 10, getWidth(), ndb3, mPaintGrid);
        c.drawRect(0, db3, getWidth(), getHeight() - 2, mPaintGrid);

        mPaintGrid.setStyle(Paint.Style.STROKE);
        mPaintGrid.setColor(Color.GRAY);
    }

    private int dBLine(int val){
        return (int)(val/ (double) AudioInfo.AMPLITUDE_RANGE * getHeight()/2 + getHeight()/2);
    }

    @Override
    public void onDraw(Canvas c){
        super.onDraw(c);
        drawDbLines(c);
    }
}
