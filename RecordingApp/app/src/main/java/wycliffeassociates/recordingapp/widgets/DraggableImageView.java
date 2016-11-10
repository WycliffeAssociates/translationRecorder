package wycliffeassociates.recordingapp.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by sarabiaj on 11/8/2016.
 */

public class DraggableImageView extends ImageView implements View.OnTouchListener {

    int mId;
    float dX;
    float dY;
    float position = 0;
    int lastAction;
    PositionChangeMediator mPositionChangeMediator;

    public interface PositionChangeMediator {
        float onPositionRequested(int id, float x);
        void onPositionChanged(int id, float x);
    }

    private static DraggableImageView newInstance(Activity context, int drawableId, int viewId){
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return newInstance(context, params, drawableId, viewId);
    }

    private static DraggableImageView newInstance(Activity context, int drawableId, int gravity, int viewId){
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, gravity);
        return newInstance(context, params, drawableId, viewId);
    }

    private static DraggableImageView newInstance(Activity context, ViewGroup.LayoutParams params, int drawableId, int viewId){
        DraggableImageView view = new DraggableImageView(context);
        view.setImageResource(drawableId);
        view.setLayoutParams(params);
        view.setMarkerId(viewId);
        return view;
    }

    public void setPositionChangeMediator(PositionChangeMediator listener){
        mPositionChangeMediator = listener;
    }

    protected void setMarkerId(int id){
        mId = id;
    }

    public DraggableImageView(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                lastAction = MotionEvent.ACTION_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:
                position = event.getRawX() + dX;
                if(mPositionChangeMediator != null) {
                    position = mPositionChangeMediator.onPositionRequested(mId, position);
                    view.setX(position);
                    mPositionChangeMediator.onPositionChanged(mId, position);
                } else {
                    view.setX(position);
                }
                lastAction = MotionEvent.ACTION_MOVE;
                break;

            default:
                return false;
        }
        return true;
    }

    public float getMarkerX(){
        return this.getX();
    }

}
