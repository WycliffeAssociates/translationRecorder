package wycliffeassociates.recordingapp.Playback.overlays;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import wycliffeassociates.recordingapp.Playback.VerseMarker;
import wycliffeassociates.recordingapp.widgets.DraggableImageView;

/**
 * Created by sarabiaj on 11/8/2016.
 */

public class DraggableViewFrame extends FrameLayout {

    public DraggableViewFrame(Context context) {
        super(context);
    }

    public DraggableViewFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableViewFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addStartMarker(DraggableImageView startMarker){
        addView(startMarker);
    }

    public void addVerseMarker(VerseMarker verseMarker){
    }

}
