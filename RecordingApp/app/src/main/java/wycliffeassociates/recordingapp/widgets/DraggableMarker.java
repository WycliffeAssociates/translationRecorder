package wycliffeassociates.recordingapp.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public abstract class DraggableMarker {

    protected Paint mMarkerLinePaint;
    DraggableImageView mView;

    public DraggableMarker(DraggableImageView view, Paint paint){
        mView = view;
        mMarkerLinePaint = paint;
    }

    public abstract void drawMarkerLine(Canvas canvas);

    public float getMarkerX(){
        return mView.getMarkerX();
    }

    public float getWidth(){
        return mView.getWidth();
    }
}
