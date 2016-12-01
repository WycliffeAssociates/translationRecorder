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

import wycliffeassociates.recordingapp.Playback.interfaces.MarkerMediator;
import wycliffeassociates.recordingapp.Playback.interfaces.ViewCreatedCallback;
import wycliffeassociates.recordingapp.Playback.overlays.DraggableViewFrame;
import wycliffeassociates.recordingapp.Playback.overlays.MarkerLineLayer;
import wycliffeassociates.recordingapp.Playback.overlays.RectangularHighlightLayer;
import wycliffeassociates.recordingapp.Playback.overlays.ScrollGestureLayer;
import wycliffeassociates.recordingapp.Playback.overlays.WaveformLayer;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.DraggableImageView;
import wycliffeassociates.recordingapp.widgets.DraggableMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarker;
import wycliffeassociates.recordingapp.widgets.SectionMarkerView;
import wycliffeassociates.recordingapp.widgets.VerseMarker;
import wycliffeassociates.recordingapp.widgets.VerseMarkerView;

import static wycliffeassociates.recordingapp.Playback.MarkerHolder.END_MARKER_ID;
import static wycliffeassociates.recordingapp.Playback.MarkerHolder.START_MARKER_ID;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class WaveformFragment extends Fragment implements DraggableImageView.PositionChangeMediator,
        MarkerLineLayer.MarkerLineDrawDelegator, WaveformLayer.WaveformDrawDelegator, ScrollGestureLayer.OnScrollListener,
        RectangularHighlightLayer.HighlightDelegator

{

    //------------Views-----------------//
    DraggableViewFrame mDraggableViewFrame;
    MarkerLineLayer mMarkerLineLayer;
    WaveformLayer mWaveformLayer;
    RectangularHighlightLayer mHighlightLayer;
    ScrollGestureLayer mScrollGestureLayer;
    Paint mPaint;
    MarkerMediator mMediator;

    FrameLayout mFrame;
    WaveformDrawDelegator mDrawDelegator;
    OnScrollDelegator mOnScrollDelegator;
    ViewCreatedCallback mViewCreatedCallback;

    public interface WaveformDrawDelegator {
        void onDrawWaveform(Canvas canvas, Paint paint);
    }

    public interface OnScrollDelegator {
        void delegateOnScroll(float distY);
        void onCueScroll(int id, float distY);
    }

    @Override
    public void onScroll(float x1, float x2, float distX) {
        mOnScrollDelegator.delegateOnScroll(distX);
    }

    public static WaveformFragment newInstance(MarkerMediator mediator){
        WaveformFragment f = new WaveformFragment();
        f.setMarkerMediator(mediator);
        return f;
    }

    private void setMarkerMediator(MarkerMediator mediator) {
        mMediator = mediator;
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
        mHighlightLayer = RectangularHighlightLayer.newInstance(getActivity(), this);
        mScrollGestureLayer = ScrollGestureLayer.newInstance(getActivity(), this);
        mViewCreatedCallback.onViewCreated(this);
        mFrame.addView(mWaveformLayer);
        mFrame.addView(mScrollGestureLayer);
        mFrame.addView(mMarkerLineLayer);
        mFrame.addView(mHighlightLayer);
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.bright_yellow));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1f);
        mDraggableViewFrame.bringToFront();
        mMediator.setDraggableViewFrame(mDraggableViewFrame);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDrawDelegator = (WaveformDrawDelegator) activity;
        mOnScrollDelegator = (OnScrollDelegator) activity;
        mViewCreatedCallback = (ViewCreatedCallback) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDrawDelegator = null;
        mOnScrollDelegator = null;
        mViewCreatedCallback = null;
    }

    private void findViews(){
        View view = getView();
        mDraggableViewFrame = (DraggableViewFrame) view.findViewById(R.id.draggable_view_frame);
        mFrame = (FrameLayout) view.findViewById(R.id.waveform_frame);
    }

    //-------------MARKERS----------------------//

    public void addStartMarker(int location){
//        if (!mMarkers.containsKey(START_MARKER_ID)) {
            SectionMarkerView div = SectionMarkerView.newInstance(getActivity(), R.drawable.ic_startmarker_cyan, START_MARKER_ID, SectionMarkerView.Orientation.LEFT_MARKER);
            div.setPositionChangeMediator(this);
            div.setX(div.mapLocationToScreenSpace(location, mFrame.getWidth()));
            //mDraggableViewFrame.addView(div);
            mMediator.onAddStartSectionMarker(new SectionMarker(div, getResources().getColor(R.color.dark_moderate_cyan), location));
        onLocationUpdated(location);
    }

    public void addEndMarker(int location){
                    SectionMarkerView div = SectionMarkerView.newInstance(getActivity(), R.drawable.ic_endmarker_cyan, Gravity.BOTTOM, END_MARKER_ID, SectionMarkerView.Orientation.RIGHT_MARKER);
            div.setPositionChangeMediator(this);
            div.setX(div.mapLocationToScreenSpace(location, mFrame.getWidth()));
            //mDraggableViewFrame.addView(div);
           mMediator.onAddEndSectionMarker(new SectionMarker(div, getResources().getColor(R.color.dark_moderate_cyan), location));
        onLocationUpdated(location);
    }

    public void addVerseMarker(int verseNumber, int frame){
        VerseMarkerView div = VerseMarkerView.newInstance(getActivity(), R.drawable.verse_marker_yellow, verseNumber);
        div.setPositionChangeMediator(this);
        //mDraggableViewFrame.addView(div);
        mMediator.onAddVerseMarker(verseNumber, new VerseMarker(div, getResources().getColor(R.color.yellow), frame));
    }

    @Override
    public float onPositionRequested(int id, float x){
        if(id < 0) {
            if(id == END_MARKER_ID){
                x = Math.max(mMediator.getMarker(END_MARKER_ID).getMarkerX(), x);
            } else {
                x = Math.max(mMediator.getMarker(START_MARKER_ID).getMarkerX() - mMediator.getMarker(START_MARKER_ID).getWidth(), x);
            }
        }
        return x;
    }

    @Override
    public void onPositionChanged(int id, float x) {
        mOnScrollDelegator.onCueScroll(id, x);
        mMarkerLineLayer.postInvalidate();
    }

    @Override
    public void onDrawMarkers(Canvas canvas) {
        Collection<DraggableMarker> markers = mMediator.getMarkers();
        for (DraggableMarker d : markers) {
            d.drawMarkerLine(canvas);
        }
        canvas.drawLine(mWaveformLayer.getWidth()/8, 0, mWaveformLayer.getWidth()/8, mWaveformLayer.getHeight(), mPaint);
        canvas.drawLine(0, mWaveformLayer.getHeight()/2, mWaveformLayer.getWidth(), mWaveformLayer.getHeight()/2, mPaint);
    }

    public void onDrawHighlight(Canvas canvas, Paint paint) {
        if(mMediator.contains(END_MARKER_ID) && mMediator.contains(START_MARKER_ID)) {
            canvas.drawRect(mMediator.getMarker(START_MARKER_ID).getMarkerX(), 0,
                    mMediator.getMarker(END_MARKER_ID).getMarkerX(), mFrame.getHeight(), paint);
        }
    }

    @Override
    public void onDrawWaveform(Canvas canvas, Paint paint){
        mDrawDelegator.onDrawWaveform(canvas, paint);
    }

    public void onLocationUpdated(int location){
        mMediator.updateCurrentFrame(location);
        mWaveformLayer.postInvalidate();
        mDraggableViewFrame.postInvalidate();
        mMarkerLineLayer.postInvalidate();
        mHighlightLayer.postInvalidate();
    }
}
