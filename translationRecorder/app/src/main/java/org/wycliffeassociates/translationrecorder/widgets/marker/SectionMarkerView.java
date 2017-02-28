package org.wycliffeassociates.translationrecorder.widgets.marker;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public class SectionMarkerView extends DraggableImageView {

    public enum Orientation {
        LEFT_MARKER,
        RIGHT_MARKER
    }

    Orientation mOrientation;

    private void setOrientation(Orientation orientation){
        mOrientation = orientation;
    }

    public SectionMarkerView(Activity context, int drawableId, int viewId, Orientation orientation, int color, float strokeWidth){
        this(
                context,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT),
                drawableId,
                viewId,
                orientation,
                color,
                strokeWidth
        );
    }

    public SectionMarkerView(Activity context, int drawableId, int gravity, int viewId, Orientation orientation, int color, float strokeWidth){
        this(
                context,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, gravity),
                drawableId,
                viewId,
                orientation,
                color,
                strokeWidth
        );
    }

    private SectionMarkerView(Activity context, ViewGroup.LayoutParams params, int drawableId, int viewId, Orientation orientation, int color, float strokeWidth){
        super(context, params, drawableId, viewId, configurePaint(color, strokeWidth));
        setOrientation(orientation);
        if(orientation == Orientation.LEFT_MARKER) {
            setScaleType(ScaleType.FIT_START);
        } else {
            setScaleType(ScaleType.FIT_END);
        }
    }

    public SectionMarkerView(Context context) {
        super(context);
    }

    @Override
    public float getMarkerX(){
        if(mOrientation == Orientation.LEFT_MARKER){
            return (getX() + getWidth());
        } else {
            return getX();
        }
    }

    @Override
    public void setX(float x) {
        if(mOrientation == Orientation.LEFT_MARKER){
            x -= this.getWidth();
        }
        super.setX(x);
    }
}
