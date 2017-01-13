package org.wycliffeassociates.translationrecorder.Playback.overlays;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.wycliffeassociates.translationrecorder.Playback.VerseMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.DraggableImageView;

/**
 * Created by sarabiaj on 11/8/2016.
 */

public class DraggableViewFrame extends FrameLayout {

    OnDragListener dragListener = new OnDragListener() {
        @Override
        public boolean onDrag(View view, DragEvent event) {
            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();
            // Handles each of the expected events
            switch(action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    ClipData cd = event.getClipData();
                    String tag = cd.getItemAt(0).getText().toString();
                    int id = Integer.parseInt(tag);
                    View marker = findViewWithTag(tag);
                    //marker.setX(event.getX());
                    float x = event.getX() - marker.getWidth()/2;
                    float pos = mMarkerMediator.onPositionRequested(id, x);
                    mMarkerMediator.onPositionChanged(id, pos);
                    invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                // An unknown action type was received.
                default:
                    Log.e("DragDrop Example","Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
    };
    PositionChangeMediator mMarkerMediator;

    public interface PositionChangeMediator {
        float onPositionRequested(int id, float x);
        void onPositionChanged(int id, float x);
        //void onRemoveMarker(int id);
    }

    public DraggableViewFrame(Context context) {
        super(context);
        this.setOnDragListener(dragListener);
    }

    public DraggableViewFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnDragListener(dragListener);
    }

    public DraggableViewFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOnDragListener(dragListener);
    }

    public void setPositionChangeMediator(PositionChangeMediator mediator) {
        mMarkerMediator = mediator;
    }

    public void addStartMarker(DraggableImageView startMarker){
        addView(startMarker);
    }

    public void addVerseMarker(VerseMarker verseMarker){
    }

}
