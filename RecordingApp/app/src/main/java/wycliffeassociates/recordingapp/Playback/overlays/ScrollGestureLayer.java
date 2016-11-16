package wycliffeassociates.recordingapp.Playback.overlays;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sarabiaj on 11/14/2016.
 */

public class ScrollGestureLayer extends View implements GestureDetector.OnGestureListener {

    private float dX;
    private float startX;

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mListener.onScroll(distanceX);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public interface OnScrollListener {
        void onScroll(float distY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScroll.onTouchEvent(event);
        //return super.onTouchEvent(event);
    }

    private OnScrollListener mListener;
    public GestureDetector mScroll;

    public ScrollGestureLayer(Context context) {
        super(context);
        mScroll = new GestureDetector(context, this);

    }

    public static ScrollGestureLayer newInstance(Context context, OnScrollListener osl) {
        ScrollGestureLayer view = new ScrollGestureLayer(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setOnScrollListener(osl);
        return view;
    }

    private void setOnScrollListener(OnScrollListener osl) {
        mListener = osl;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //return mScroll.onTouchEvent(event);
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                startX = event.getRawX();
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                dX = startX - event.getRawX();
//                //startX += dX;
//                mListener.onScroll(dX);
//                break;
//
//            default:
//                return false;
//        }
//        return true;
//    }

//    private class ScrollGesture extends GestureDetector.SimpleOnGestureListener {
//        /**
//         * Detects if the user is scrolling the main waveform horizontally
//         *
//         * @param distX  refers to how far the user scrolled horizontally
//         * @param distY  is ignored for this use as we are only allowing horizontal scrolling
//         * @param event1 not accessed, contains information about the start of the gesture
//         * @param event2 not used, contains information about the end of the gesture
//         * @return must be true for gesture detection
//         */
//        @Override
//        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distX, float distY) {
//            mListener.onScroll(distX);
//            return true;
//        }
//    }
}
