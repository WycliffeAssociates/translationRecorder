package wycliffeassociates.recordingapp.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Collection;
import java.util.HashMap;

import wycliffeassociates.recordingapp.Playback.overlays.MarkerLineLayer;
import wycliffeassociates.recordingapp.Playback.overlays.ScrollGestureLayer;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Playback.overlays.DraggableViewFrame;
import wycliffeassociates.recordingapp.widgets.DraggableImageView;
import wycliffeassociates.recordingapp.widgets.DraggableMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarkerView;
import wycliffeassociates.recordingapp.widgets.VerseMarker;
import wycliffeassociates.recordingapp.widgets.VerseMarkerView;
import wycliffeassociates.recordingapp.Playback.overlays.WaveformLayer;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class WaveformFragment extends Fragment implements DraggableImageView.PositionChangeMediator,
        MarkerLineLayer.MarkerLineDrawDelegator, WaveformLayer.WaveformDrawDelegator, ScrollGestureLayer.OnScrollListener {

    //------------Views-----------------//
    DraggableViewFrame mDraggableViewFrame;
    MarkerLineLayer mMarkerLineLayer;
    WaveformLayer mWaveformLayer;
    ScrollGestureLayer mScrollGestureLayer;
    Paint mPaint;
    HashMap<Integer, DraggableMarker> mMarkers = new HashMap<>();
    final int START_MARKER_ID = -1;
    final int END_MARKER_ID = -2;
    FrameLayout mFrame;
    WaveformDrawDelegator mDrawDelegator;
    OnScrollDelegator mOnScrollDelegator;

    public interface WaveformDrawDelegator {
        void onDrawWaveform(Canvas canvas, Paint paint);
    }

    public interface OnScrollDelegator {
        void delegateOnScroll(float distY);
    }

    @Override
    public void onScroll(float distY) {
        mOnScrollDelegator.delegateOnScroll(distY);
    }

    public static WaveformFragment newInstance(){
        WaveformFragment f = new WaveformFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_waveform, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        mWaveformLayer = WaveformLayer.newInstance(getActivity(), this);
        mMarkerLineLayer = MarkerLineLayer.newInstance(getActivity(), this);
        mScrollGestureLayer = ScrollGestureLayer.newInstance(getActivity(), this);
        mFrame.addView(mWaveformLayer);
        mFrame.addView(mMarkerLineLayer);
        mFrame.addView(mScrollGestureLayer);
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.bright_yellow));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1f);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDrawDelegator = (WaveformDrawDelegator) activity;
        mOnScrollDelegator = (OnScrollDelegator) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDrawDelegator = null;
        mOnScrollDelegator = null;
    }

    private void findViews(){
        View view = getView();
        mDraggableViewFrame = (DraggableViewFrame) view.findViewById(R.id.draggable_view_frame);
        mFrame = (FrameLayout) view.findViewById(R.id.waveform_frame);
    }

    //-------------MARKERS----------------------//

    public void addStartMarker(int location){
        SectionMarkerView div = SectionMarkerView.newInstance(getActivity(), R.drawable.ic_startmarker_cyan, START_MARKER_ID, SectionMarkerView.Orientation.LEFT_MARKER);
        div.setPositionChangeMediator(this);
        div.setX(div.mapLocationToScreenSpace(location, mFrame.getWidth()));
        mDraggableViewFrame.addView(div);
        mMarkers.put(START_MARKER_ID, new SectionMarker(div, getResources().getColor(R.color.dark_moderate_cyan)));
    }

    public void addEndMarker(int location){
        SectionMarkerView div = SectionMarkerView.newInstance(getActivity(), R.drawable.ic_endmarker_cyan, Gravity.BOTTOM, END_MARKER_ID, SectionMarkerView.Orientation.RIGHT_MARKER);
        div.setPositionChangeMediator(this);
        div.setX(div.mapLocationToScreenSpace(location, mFrame.getWidth()));
        mDraggableViewFrame.addView(div);
        mMarkers.put(END_MARKER_ID, new SectionMarker(div, getResources().getColor(R.color.dark_moderate_cyan)));
    }

    public void addVerseMarker(){
        VerseMarkerView div = VerseMarkerView.newInstance(getActivity(), R.drawable.bookmark_add, 1);
        div.setPositionChangeMediator(this);
        mDraggableViewFrame.addView(div);
        mMarkers.put(1, new VerseMarker(div, getResources().getColor(R.color.yellow)));
    }

    @Override
    public float onPositionRequested(int id, float x){
        if(id < 0) {
            if(id == END_MARKER_ID){
                return Math.max(mMarkers.get(START_MARKER_ID).getMarkerX(), x);
            } else {
                return Math.min(mMarkers.get(END_MARKER_ID).getMarkerX() - mMarkers.get(END_MARKER_ID).getWidth(), x);
            }
        } else {
            return x;
        }
    }

    @Override
    public void onPositionChanged(int id, float x) {
        mMarkerLineLayer.postInvalidate();
    }

    @Override
    public void onDrawMarkers(Canvas canvas) {
        Collection<DraggableMarker> markers = mMarkers.values();
        for (DraggableMarker d : markers) {
            d.drawMarkerLine(canvas);
        }
        canvas.drawLine(mWaveformLayer.getWidth()/8, 0, mWaveformLayer.getWidth()/8, mWaveformLayer.getHeight(), mPaint);
        canvas.drawLine(0, mWaveformLayer.getHeight()/2, mWaveformLayer.getWidth(), mWaveformLayer.getHeight()/2, mPaint);
    }

    @Override
    public void onDrawWaveform(Canvas canvas, Paint paint){
        mDrawDelegator.onDrawWaveform(canvas, paint);
    }

    public void onLocationUpdated(int location){
        mWaveformLayer.postInvalidate();
        mDraggableViewFrame.postInvalidate();
    }
}
