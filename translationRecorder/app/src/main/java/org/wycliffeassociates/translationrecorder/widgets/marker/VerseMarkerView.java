package org.wycliffeassociates.translationrecorder.widgets.marker;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public class VerseMarkerView extends DraggableImageView {

    public VerseMarkerView(Activity context, int drawableId, int viewId){
        this(
                context,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT),
                drawableId,
                viewId
        );
    }

    public VerseMarkerView(Activity context, int drawableId, int gravity, int viewId){
        this(
                context,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, gravity),
                drawableId,
                viewId
        );
    }

    private VerseMarkerView(Activity context, ViewGroup.LayoutParams params, int drawableId, int viewId){
        super(context, params, drawableId, viewId);
        setScaleType(ScaleType.FIT_START);
    }
}
