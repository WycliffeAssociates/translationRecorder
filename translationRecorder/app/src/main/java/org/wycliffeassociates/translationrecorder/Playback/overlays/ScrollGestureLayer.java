package org.wycliffeassociates.translationrecorder.Playback.overlays;

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

    private boolean isScrolling = false;

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mTap != null) {
            mTap.onTap(e.getX());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        isScrolling = true;
        mListener.onScroll(e1.getX(), e2.getX(), distanceX);
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
        void onScroll(float rawX1, float rawX2, float distX);
        void onScrollComplete();
    }

    public interface OnTapListener {
        void onTap(float x);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(isScrolling)
            {
                mListener.onScrollComplete();
                isScrolling = false;
            }
        }
        return mScroll.onTouchEvent(event);
        //return super.onTouchEvent(event);
    }

    private OnScrollListener mListener;
    private OnTapListener mTap;
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

    public static ScrollGestureLayer newInstance(Context context, OnScrollListener osl, OnTapListener otl){
        ScrollGestureLayer view = newInstance(context, osl);
        view.setOnTapListener(otl);
        return view;
    }

    private void setOnScrollListener(OnScrollListener osl) {
        mListener = osl;
    }

    private void setOnTapListener(OnTapListener otl) {
        mTap = otl;
    }
}
