package org.wycliffeassociates.translationrecorder.widgets.marker;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.wycliffeassociates.translationrecorder.Playback.markers.MarkerHolder;
import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 11/8/2016.
 */

public class DraggableImageView extends ImageView {

    int mId;

    public interface PositionChangeMediator {
        float onPositionRequested(int id, float x);

        void onPositionChanged(int id, float x);
        //void onRemoveMarker(int id);
    }

    public DraggableImageView(Activity context, int drawableId, int viewId) {
        this(
                context,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                drawableId,
                viewId
        );
    }

    public DraggableImageView(Activity context, int drawableId, int gravity, int viewId) {
        this(
                context,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, gravity),
                drawableId,
                viewId
        );
    }

    public DraggableImageView(Activity context, ViewGroup.LayoutParams params, int drawableId, int viewId) {
        this(context);
        setImageResource(drawableId);
        setLayoutParams(params);
        setMarkerId(viewId);
    }

    protected void setMarkerId(int id) {
        mId = id;
    }

    public DraggableImageView(final Context context) {
        super(context);
        //setOnTouchListener(this);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    String tag = String.valueOf(mId);
                    DraggableImageView.this.setTag(tag);
                    ClipData data = ClipData.newPlainText("marker", tag);
                    Paint paint = new Paint();
                    paint.setStrokeWidth(8f);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(getResources().getColor(R.color.tertiary));
                    MarkerShadow.Orientation orientation;
                    if (mId == MarkerHolder.START_MARKER_ID) {
                        orientation = MarkerShadow.Orientation.RIGHT;
                    } else {
                        orientation = MarkerShadow.Orientation.LEFT;
                    }
                    View.DragShadowBuilder shadowBuilder = new MarkerShadow(DraggableImageView.this, paint, orientation);
                    view.startDrag(data, shadowBuilder, view, 0);
                    return true;
                } else {
                    return false;
                }
            }
        });

    }

    public float getMarkerX() {
        return this.getX();
    }

    public static int mapLocationToScreenSpace(int location, int width) {
        float mspp = 1000 * 10 / (float) width;
        return (int) ((location / 44.1) / mspp);
    }

}
