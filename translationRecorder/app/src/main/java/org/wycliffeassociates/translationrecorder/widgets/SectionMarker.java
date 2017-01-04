package org.wycliffeassociates.translationrecorder.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public class SectionMarker extends DraggableMarker {

    public SectionMarker(SectionMarkerView view, int color, int frame){
        super(view, configurePaint(color), frame);
    }

    private static Paint configurePaint(int color){
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8.f);
        return paint;
    }

    @Override
    public void drawMarkerLine(Canvas canvas) {
        canvas.drawLine(mView.getMarkerX(), 0, mView.getMarkerX(), canvas.getHeight(), mMarkerLinePaint);
    }
}
