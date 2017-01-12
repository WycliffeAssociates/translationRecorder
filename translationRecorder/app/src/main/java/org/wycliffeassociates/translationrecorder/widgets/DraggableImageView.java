package org.wycliffeassociates.translationrecorder.widgets;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by sarabiaj on 11/8/2016.
 */

public class DraggableImageView extends ImageView {

    int mId;
    float dX;
    float dY;
    float position = 0;
    int lastAction;

    public interface PositionChangeMediator {
        float onPositionRequested(int id, float x);
        void onPositionChanged(int id, float x);
        //void onRemoveMarker(int id);
    }

    private static DraggableImageView newInstance(Activity context, int drawableId, int viewId){
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return newInstance(context, params, drawableId, viewId);
    }

    private static DraggableImageView newInstance(Activity context, int drawableId, int gravity, int viewId){
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, gravity);
        return newInstance(context, params, drawableId, viewId);
    }

    private static DraggableImageView newInstance(Activity context, ViewGroup.LayoutParams params, int drawableId, int viewId){
        DraggableImageView view = new DraggableImageView(context);
        view.setImageResource(drawableId);
        view.setLayoutParams(params);
        view.setMarkerId(viewId);
        return view;
    }

    protected void setMarkerId(int id){
        mId = id;
    }

    public DraggableImageView(final Context context) {
        super(context);
        //setOnTouchListener(this);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    //DraggableImageView.this.setVisibility(INVISIBLE);
                    DraggableImageView.this.setId(mId);
                    ClipData data = ClipData.newPlainText("marker", String.valueOf(getId()));
                    setVisibility(INVISIBLE);
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(DraggableImageView.this);
                    view.startDrag(data, shadowBuilder, view, 0);
                    return true;
                } else {
                    return false;
                }            }
        });

    }

    public float getMarkerX(){
        return this.getX();
    }

    public static int mapLocationToScreenSpace(int location, int width){
        float mspp = 1000 * 10 / (float) width;
        return (int)((location/44.1) / mspp);
    }

}
