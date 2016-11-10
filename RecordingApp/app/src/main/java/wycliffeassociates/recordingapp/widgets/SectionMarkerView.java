package wycliffeassociates.recordingapp.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sarabiaj on 11/10/2016.
 */

public class SectionMarkerView extends DraggableImageView {

    public static class Orientation {
        public static boolean LEFT_MARKER = true;
        public static boolean RIGHT_MARKER = false;
    }

    boolean mOrientation;

    private void setOrientation(boolean orientation){
        mOrientation = orientation;
    }

    public static SectionMarkerView newInstance(Activity context, int drawableId, int viewId, boolean orientation){
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return newInstance(context, params, drawableId, viewId, orientation);
    }

    public static SectionMarkerView newInstance(Activity context, int drawableId, int gravity, int viewId, boolean orientation){
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, gravity);
        return newInstance(context, params, drawableId, viewId, orientation);
    }

    private static SectionMarkerView newInstance(Activity context, ViewGroup.LayoutParams params, int drawableId, int viewId, boolean orientation){
        SectionMarkerView view = new SectionMarkerView(context);
        view.setImageResource(drawableId);
        view.setLayoutParams(params);
        view.setMarkerId(viewId);
        view.setOrientation(orientation);
        return view;
    }

    public SectionMarkerView(Context context) {
        super(context);
    }

    @Override
    public float getMarkerX(){
        if(mOrientation == Orientation.LEFT_MARKER){
            return (getX() + getWidth());
        } else {
            return getX();
        }
    }
}
