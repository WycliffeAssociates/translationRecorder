package org.wycliffeassociates.translationrecorder.widgets.marker;

import android.graphics.Canvas;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public class SectionMarker extends DraggableMarker {

    public SectionMarker(SectionMarkerView view, int frame){
        super(view, frame);
    }

    @Override
    public void drawMarkerLine(Canvas canvas) {
        canvas.drawLine(mView.getMarkerX(), 0, mView.getMarkerX(), canvas.getHeight(), mView.getPaint());
    }
}
