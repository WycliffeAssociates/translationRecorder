package org.wycliffeassociates.translationrecorder.widgets.marker;

import android.graphics.Canvas;
import android.view.View;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public abstract class DraggableMarker {

    DraggableImageView mView;
    private volatile int mFrame;

    public DraggableMarker(DraggableImageView view, int frame){
        mView = view;
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
        int delta = mFrame - playbackLocation;
        mView.setX((containerWidth/8) + (mView.mapLocationToScreenSpace(delta, containerWidth)));
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
