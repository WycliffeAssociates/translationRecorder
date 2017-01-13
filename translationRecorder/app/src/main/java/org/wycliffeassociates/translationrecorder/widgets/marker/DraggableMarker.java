package org.wycliffeassociates.translationrecorder.widgets.marker;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public abstract class DraggableMarker {

    protected Paint mMarkerLinePaint;
    DraggableImageView mView;
    private volatile int mFrame;

    public DraggableMarker(DraggableImageView view, Paint paint, int frame){
        mView = view;
        mMarkerLinePaint = paint;
        mFrame = frame;
    }

    public abstract void drawMarkerLine(Canvas canvas);

    public float getMarkerX(){
        return mView.getMarkerX();
    }

    public float getWidth(){
        return mView.getWidth();
    }

    public void updateX(int playbackLocation, int containerWidth){
        mView.setX((containerWidth/8) + (mView.mapLocationToScreenSpace(mFrame, containerWidth) - mView.mapLocationToScreenSpace(playbackLocation, containerWidth)));
    }

    public void updateFrame(int frame) {
        mFrame = frame;
    }

    public int getFrame(){
        return mFrame;
    }

    public View getView(){
        return mView;
    }
}
