package org.wycliffeassociates.translationrecorder.widgets.marker;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    OnMarkerMovementRequest markerMovementRequest;
    Paint mPaint;

    public interface OnMarkerMovementRequest {
        boolean onMarkerMovementRequest(int markerId);
    }

    public DraggableImageView(Activity context, int drawableId, int viewId, Paint paint) {
        this(
                context,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                drawableId,
                viewId,
                paint
        );
    }

    public DraggableImageView(Activity context, int drawableId, int gravity, int viewId, Paint paint) {
        this(
                context,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, gravity),
                drawableId,
                viewId,
                paint
        );
    }

    public DraggableImageView(Activity context, ViewGroup.LayoutParams params, int drawableId, int viewId, Paint paint) {
        this(context);
        mPaint = paint;

        int dp = (int)context.getResources().getDimension(R.dimen.icon_xl);
        Drawable d = context.getResources().getDrawable(drawableId);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPurgeable = true;
        opts.inScaled = true;
        Bitmap bmp = Bitmap.createScaledBitmap(drawableToBitmap(d), dp, dp, true);
        setImageBitmap(bmp);

        //setImageResource(drawableId);
        setLayoutParams(params);
        setMarkerId(viewId);
        if(context instanceof OnMarkerMovementRequest) {
            markerMovementRequest = (OnMarkerMovementRequest)context;
        } else {
            throw new RuntimeException("Activity used to create DraggableImageView does not implement OnMarkerMovementRequest");
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    protected void setMarkerId(int id) {
        mId = id;
    }

    public int getMarkerId(){
        return mId;
    }

    public DraggableImageView(final Context context) {
        super(context);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (markerMovementRequest.onMarkerMovementRequest(mId)) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        String tag = String.valueOf(mId);
                        DraggableImageView.this.setTag(tag);
                        ClipData data = ClipData.newPlainText("marker", tag);
                        MarkerShadow.Orientation orientation;
                        if (mId == MarkerHolder.START_MARKER_ID) {
                            orientation = MarkerShadow.Orientation.RIGHT;
                        } else {
                            orientation = MarkerShadow.Orientation.LEFT;
                        }
                        View.DragShadowBuilder shadowBuilder = new MarkerShadow(DraggableImageView.this, mPaint, orientation);
                        view.startDrag(data, shadowBuilder, view, 0);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public float getMarkerX() {
        return this.getX();
    }

    Paint getPaint(){
        return mPaint;
    }

    protected static Paint configurePaint(int color, float strokeWidth){
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        return paint;
    }

    public static int mapLocationToScreenSpace(int location, int width) {
        float fpp = (float)Math.floor(441000 / (float) width);
        return (int) Math.round(location / fpp);
    }
}