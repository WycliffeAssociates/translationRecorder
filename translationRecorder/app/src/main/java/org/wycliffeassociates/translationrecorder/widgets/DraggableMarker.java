package org.wycliffeassociates.translationrecorder.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public abstract class DraggableMarker {

    protected Paint mMarkerLinePaint;
    DraggableImageView mView;
    private int mLocation;

    public DraggableMarker(DraggableImageView view, Paint paint, int location){
        mView = view;
        mMarkerLinePaint = paint;
        mLocation = location;
    }

    public abstract void drawMarkerLine(Canvas canvas);

    public float getMarkerX(){
        return mView.getMarkerX();
    }

    public float getWidth(){
        return mView.getWidth();
    }

    public void updateX(int playbackLocation, int containerWidth){
        mView.setX((containerWidth/8) + (mView.mapLocationToScreenSpace(mLocation, containerWidth) - mView.mapLocationToScreenSpace(playbackLocation, containerWidth)));
    }

    public void updateFrame(int frame) {
        mLocation = frame;
    }

    public int getFrame(){
        return mLocation;
    }

    public View getView(){
        return mView;
    }
}
