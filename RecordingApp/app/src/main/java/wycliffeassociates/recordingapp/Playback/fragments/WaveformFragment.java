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

import java.util.HashMap;

import wycliffeassociates.recordingapp.Playback.MarkerLineLayer;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.DragableViewFrame;
import wycliffeassociates.recordingapp.widgets.DraggableImageView;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class WaveformFragment extends Fragment implements DraggableImageView.OnPositionChangedListener, MarkerLineLayer.MarkerLineDrawDelegator{

    DragableViewFrame mDraggableViewFrame;
    MarkerLineLayer mMarkerLineLayer;
    HashMap<Integer, DraggableImageView> mMarkers = new HashMap<>();
    final int START_MARKER_ID = -1;
    final int END_MARKER_ID = -2;

    Paint mMarkerLine;
    FrameLayout mFrame;

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
        mMarkerLine = new Paint();
        mMarkerLine.setColor(getResources().getColor(R.color.dark_moderate_cyan));
        mMarkerLine.setStyle(Paint.Style.STROKE);
        mMarkerLine.setStrokeWidth(2f);
        mMarkerLineLayer = MarkerLineLayer.newInstance(getActivity(), this);
        mFrame.addView(mMarkerLineLayer);
        addStartMarker();
        addEndMarker();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void findViews(){
        View view = getView();
        mDraggableViewFrame = (DragableViewFrame) view.findViewById(R.id.draggable_view_frame);
        mFrame = (FrameLayout) view.findViewById(R.id.waveform_frame);
    }

    public void addStartMarker(){
        DraggableImageView div = DraggableImageView.newInstance(getActivity(), R.drawable.ic_startmarker_cyan, START_MARKER_ID);
        div.setOnPositionChangedListener(this);
        mDraggableViewFrame.addView(div);
        mMarkers.put(START_MARKER_ID, div);

    }

    public void addEndMarker(){
        DraggableImageView div = DraggableImageView.newInstance(getActivity(), R.drawable.ic_endmarker_cyan, Gravity.BOTTOM, END_MARKER_ID);
        div.setOnPositionChangedListener(this);
        mDraggableViewFrame.addView(div);
        mMarkers.put(END_MARKER_ID, div);
    }

    public void addVerseMarker(){

    }

    @Override
    public void onPositionChanged(int id, float x) {
        mMarkerLineLayer.postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mMarkers != null) {
            for (DraggableImageView d : mMarkers.values()) {
                canvas.drawLine(d.getX(), canvas.getWidth(), d.getX(), canvas.getHeight(), mMarkerLine);
            }
        }
    }
}
