package org.wycliffeassociates.translationrecorder.widgets.marker;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by sarabiaj on 1/13/2017.
 */

public class MarkerShadow extends View.DragShadowBuilder {

    public enum Orientation {
        LEFT,
        RIGHT
    }

    Paint mPaint;
    Orientation mOrientation;

    public MarkerShadow(View view, Paint paint, MarkerShadow.Orientation orientation){
        super(view);
        mPaint = paint;
        mOrientation = orientation;
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        super.onDrawShadow(canvas);
        float x = 0;
        if(mOrientation == Orientation.RIGHT) {
            x = getView().getWidth();
        }
        canvas.drawLine(x, 0, x, canvas.getHeight(), mPaint);
    }
}
