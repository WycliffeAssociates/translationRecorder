package org.wycliffeassociates.translationrecorder.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.wycliffeassociates.translationrecorder.AudioVisualization.WavVisualizer;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MarkerMediator;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MediaController;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.ViewCreatedCallback;
import org.wycliffeassociates.translationrecorder.Playback.markers.MarkerHolder;
import org.wycliffeassociates.translationrecorder.Playback.overlays.DraggableViewFrame;
import org.wycliffeassociates.translationrecorder.Playback.overlays.MarkerLineLayer;
import org.wycliffeassociates.translationrecorder.Playback.overlays.RectangularHighlightLayer;
import org.wycliffeassociates.translationrecorder.Playback.overlays.ScrollGestureLayer;
import org.wycliffeassociates.translationrecorder.Playback.overlays.WaveformLayer;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.widgets.marker.DraggableMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.SectionMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.SectionMarkerView;
import org.wycliffeassociates.translationrecorder.widgets.marker.VerseMarker;
import org.wycliffeassociates.translationrecorder.widgets.marker.VerseMarkerView;

import java.util.Collection;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class WaveformFragment extends Fragment implements DraggableViewFrame.PositionChangeMediator,
        MarkerLineLayer.MarkerLineDrawDelegator, WaveformLayer.WaveformDrawDelegator, ScrollGestureLayer.OnScrollListener,
        RectangularHighlightLayer.HighlightDelegator

{

    //------------Views-----------------//
    DraggableViewFrame mDraggableViewFrame;
    MarkerLineLayer mMarkerLineLayer;
    WaveformLayer mWaveformLayer;
    RectangularHighlightLayer mHighlightLayer;
    ScrollGestureLayer mScrollGestureLayer;
    MarkerMediator mMarkerMediator;
    Handler mHandler;
    FrameLayout mFrame;

    OnScrollDelegator mOnScrollDelegator;
    ViewCreatedCallback mViewCreatedCallback;
    private Paint mPaintPlaback;
    private Paint mPaintBaseLine;
    private int mCurrentRelativeFrame;
    private WavVisualizer mWavVis;
    private int mCurrentMs;
    private long mStart;
    private MediaController mMediaController;
    private int mCurrentAbsoluteFrame;


    public interface OnScrollDelegator {
        void delegateOnScroll(float distY);
        void delegateOnScrollComplete();
        void onCueScroll(int id, float distY);
    }

    @Override
    public void onScroll(float x1, float x2, float distX) {
        mOnScrollDelegator.delegateOnScroll(distX);
    }

    @Override
    public void onScrollComplete() {
        mOnScrollDelegator.delegateOnScrollComplete();
    }

    public static WaveformFragment newInstance(MarkerMediator mediator){
        WaveformFragment f = new WaveformFragment();
        f.setMarkerMediator(mediator);
        return f;
    }

    private void setMarkerMediator(MarkerMediator mediator) {
        mMarkerMediator = mediator;
    }

    public void setWavRenderer(WavVisualizer vis){
        mWavVis = vis;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_waveform, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
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

        int dpSize =  2;
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);

        mPaintPlaback = new Paint();
        mPaintPlaback.setColor(getResources().getColor(R.color.primary));
        mPaintPlaback.setStyle(Paint.Style.STROKE);
        mPaintPlaback.setStrokeWidth(dpSize);
        mPaintBaseLine = new Paint();
        mPaintBaseLine.setColor(getResources().getColor(R.color.secondary));
        mPaintBaseLine.setStyle(Paint.Style.STROKE);
        mPaintBaseLine.setStrokeWidth(dpSize);
        mDraggableViewFrame.bringToFront();
        mDraggableViewFrame.setPositionChangeMediator(this);
        mMarkerMediator.setDraggableViewFrame(mDraggableViewFrame);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnScrollDelegator = (OnScrollDelegator) activity;
        mViewCreatedCallback = (ViewCreatedCallback) activity;
        mMediaController = (MediaController) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOnScrollDelegator = null;
        mViewCreatedCallback = null;
        mMediaController = null;
    }

    private void findViews(){
        View view = getView();
        mDraggableViewFrame = (DraggableViewFrame) view.findViewById(R.id.draggable_view_frame);
        mFrame = (FrameLayout) view.findViewById(R.id.waveform_frame);
    }

    //-------------MARKERS----------------------//

    public void addStartMarker(int frame){
        int dpSize =  4;
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);

        int color =  getResources().getColor(R.color.dark_moderate_cyan);
        SectionMarkerView div = new SectionMarkerView(getActivity(), R.drawable.ic_startmarker_cyan, MarkerHolder.START_MARKER_ID, SectionMarkerView.Orientation.LEFT_MARKER, color, strokeWidth);
        div.setX(div.mapLocationToScreenSpace(frame, mFrame.getWidth())-div.getWidth());
        mMarkerMediator.onAddStartSectionMarker(new SectionMarker(div, frame));
        invalidateFrame(mCurrentAbsoluteFrame, mCurrentRelativeFrame, mCurrentMs);
    }

    public void addEndMarker(int frame){
        int dpSize =  4;
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);

        int color =  getResources().getColor(R.color.dark_moderate_cyan);
        SectionMarkerView div = new SectionMarkerView(getActivity(), R.drawable.ic_endmarker_cyan, Gravity.BOTTOM, MarkerHolder.END_MARKER_ID, SectionMarkerView.Orientation.RIGHT_MARKER, color, strokeWidth);
        div.setX(div.mapLocationToScreenSpace(frame, mFrame.getWidth()));
        mMarkerMediator.onAddEndSectionMarker(new SectionMarker(div, frame));
        invalidateFrame(mCurrentAbsoluteFrame, mCurrentRelativeFrame, mCurrentMs);
    }

    public void addVerseMarker(int verseNumber, int frame){
        int dpSize =  4;
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);

        int color =  getResources().getColor(R.color.yellow);
        VerseMarkerView div = new VerseMarkerView(getActivity(), R.drawable.verse_marker_yellow, verseNumber, color, strokeWidth);
        mMarkerMediator.onAddVerseMarker(verseNumber, new VerseMarker(div, frame));
    }

    @Override
    public float onPositionRequested(int id, float x){
        if(id < 0) {
            if(id == MarkerHolder.END_MARKER_ID){
                x = Math.max(mMarkerMediator.getMarker(MarkerHolder.START_MARKER_ID).getMarkerX(), x);
            } else {
                x += (mMarkerMediator.getMarker(MarkerHolder.START_MARKER_ID).getWidth());
                if(mMarkerMediator.contains(MarkerHolder.END_MARKER_ID)) {
                    x = Math.min(mMarkerMediator.getMarker(MarkerHolder.END_MARKER_ID).getMarkerX(), x);
                }
            }
        }
        return x;
    }

    @Override
    public void onPositionChanged(int id, float x) {
        mOnScrollDelegator.onCueScroll(id, x);
    }

    @Override
    public void onDrawMarkers(Canvas canvas) {
        Collection<DraggableMarker> markers = mMarkerMediator.getMarkers();
        for (DraggableMarker d : markers) {
            if(d instanceof VerseMarker && mMediaController.isInEditMode()) {
                continue;
            }
            d.drawMarkerLine(canvas);
        }
        canvas.drawLine(mWaveformLayer.getWidth()/8, 0, mWaveformLayer.getWidth()/8, mWaveformLayer.getHeight(), mPaintPlaback);
        canvas.drawLine(0, mWaveformLayer.getHeight()/2, mWaveformLayer.getWidth(), mWaveformLayer.getHeight()/2, mPaintBaseLine);
        //System.out.println("Markers " + (System.currentTimeMillis() - mStart) + "ms");
    }

    public void onDrawHighlight(Canvas canvas, Paint paint) {
        if(mMarkerMediator.contains(MarkerHolder.END_MARKER_ID) && mMarkerMediator.contains(MarkerHolder.START_MARKER_ID)) {
            canvas.drawRect(mMarkerMediator.getMarker(MarkerHolder.START_MARKER_ID).getMarkerX(), 0,
                    mMarkerMediator.getMarker(MarkerHolder.END_MARKER_ID).getMarkerX(), mFrame.getHeight(), paint);
        }
        //System.out.println("Highlight (should be last):" + (System.currentTimeMillis() - mStart) + "ms");
    }

    @Override
    public void onDrawWaveform(Canvas canvas, Paint paint){
        if(mWavVis != null) {
            canvas.drawLines(mWavVis.getDataToDraw(mCurrentAbsoluteFrame), paint);
        }
        //System.out.println("Waveform: " + (System.currentTimeMillis() - mStart) + "ms");
    }

    public void invalidateFrame(int absoluteFrame, int relativeFrame, int ms) {
        mCurrentRelativeFrame = relativeFrame;
        mCurrentAbsoluteFrame = absoluteFrame;
        mCurrentMs = ms;
        mStart = System.currentTimeMillis();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mMarkerMediator.updateCurrentFrame(mCurrentRelativeFrame);
                mWaveformLayer.invalidate();
                mDraggableViewFrame.invalidate();
                mMarkerLineLayer.invalidate();
                mHighlightLayer.invalidate();
            }
        });
    }
}
