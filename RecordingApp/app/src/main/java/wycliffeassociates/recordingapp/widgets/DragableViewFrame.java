package wycliffeassociates.recordingapp.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import wycliffeassociates.recordingapp.Playback.VerseMarker;

/**
 * Created by sarabiaj on 11/8/2016.
 */

public class DragableViewFrame extends FrameLayout {

    public DragableViewFrame(Context context) {
        super(context);
    }

    public DragableViewFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragableViewFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addStartMarker(DraggableImageView startMarker){
        addView(startMarker);
    }

    public void addVerseMarker(VerseMarker verseMarker){
    }

}
