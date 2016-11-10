package wycliffeassociates.recordingapp.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public class VerseMarkerView extends DraggableImageView {

    public static VerseMarkerView newInstance(Activity context, int drawableId, int viewId){
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return newInstance(context, params, drawableId, viewId);
    }

    public static VerseMarkerView newInstance(Activity context, int drawableId, int gravity, int viewId){
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, gravity);
        return newInstance(context, params, drawableId, viewId);
    }

    private static VerseMarkerView newInstance(Activity context, ViewGroup.LayoutParams params, int drawableId, int viewId){
        VerseMarkerView view = new VerseMarkerView(context);
        view.setImageResource(drawableId);
        view.setLayoutParams(params);
        view.setMarkerId(viewId);
        return view;
    }

    public VerseMarkerView(Context context) {
        super(context);
    }
}
