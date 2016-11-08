package wycliffeassociates.recordingapp.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by sarabiaj on 11/8/2016.
 */

public class DraggableImageView extends ImageView implements View.OnTouchListener {

    float dX;
    float dY;
    int lastAction;

    public static DraggableImageView newInstance(Activity context, int drawableId){
        DraggableImageView view = new DraggableImageView(context);
        view.setImageResource(drawableId);
        view.setOnTouchListener(view);
        return view;
    }

    public DraggableImageView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:
                view.setY(event.getRawY() + dY);
                view.setX(event.getRawX() + dX);
                lastAction = MotionEvent.ACTION_MOVE;
                break;

            default:
                return false;
        }
        return true;
    }

}
