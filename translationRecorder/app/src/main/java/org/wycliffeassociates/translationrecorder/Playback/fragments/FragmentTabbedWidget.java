package org.wycliffeassociates.translationrecorder.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import org.wycliffeassociates.translationrecorder.Playback.SourceAudio;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MarkerMediator;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MediaController;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.ViewCreatedCallback;
import org.wycliffeassociates.translationrecorder.Playback.overlays.MarkerLineLayer;
import org.wycliffeassociates.translationrecorder.Playback.overlays.MinimapLayer;
import org.wycliffeassociates.translationrecorder.Playback.overlays.RectangularHighlightLayer;
import org.wycliffeassociates.translationrecorder.Playback.overlays.ScrollGestureLayer;
import org.wycliffeassociates.translationrecorder.Playback.overlays.TimecodeLayer;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.widgets.AudioPlayer;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class FragmentTabbedWidget extends Fragment implements MinimapLayer.MinimapDrawDelegator,
        MarkerLineLayer.MarkerLineDrawDelegator, ScrollGestureLayer.OnTapListener, ScrollGestureLayer.OnScrollListener,
        RectangularHighlightLayer.HighlightDelegator
{

    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_FILENAME = "key_filename";
    private static final String KEY_CHAPTER = "key_chapter";

    private ImageButton mSwitchToMinimap;
    private ImageButton mSwitchToPlayback;
    private SourceAudio mSrcPlayer;

    private Paint mLocationPaint;
    private Paint mSectionPaint;
    private Paint mVersePaint;

    ViewCreatedCallback mViewCreatedCallback;
    MediaController mMediaController;
    SourceAudio.OnAudioListener mAudioListener;

    String mFilename = "";
    Project mProject;
    int mChapter = 0;

    private FrameLayout mMinimapFrame;
    private TimecodeLayer mTimecodeLayer;
    private MinimapLayer.MinimapDrawDelegator mMinimapDrawDelegator;
    private MinimapLayer mMinimapLayer;
    private MarkerLineLayer mMarkerLineLayer;
    private ScrollGestureLayer mGestureLayer;
    private DelegateMinimapMarkerDraw mMinimapLineDrawDelegator;
    private RectangularHighlightLayer mHighlightLayer;
    private MarkerMediator mMarkerMediator;

    public interface DelegateMinimapMarkerDraw {
        void onDelegateMinimapMarkerDraw(Canvas canvas, Paint location, Paint section, Paint verse);
    }

    public static FragmentTabbedWidget newInstance(MarkerMediator mediator, Project project, String filename, int chapter){
        FragmentTabbedWidget f = new FragmentTabbedWidget();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, project);
        args.putString(KEY_FILENAME, filename);
        args.putInt(KEY_CHAPTER, chapter);
        f.setArguments(args);
        f.setMediator(mediator);
        return f;
    }

    private void setMediator(MarkerMediator mediator) {
        mMarkerMediator = mediator;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mViewCreatedCallback = (ViewCreatedCallback) activity;
        mMediaController = (MediaController) activity;
        mMinimapDrawDelegator = (MinimapLayer.MinimapDrawDelegator) activity;
        mMinimapLineDrawDelegator = (DelegateMinimapMarkerDraw) activity;
        mAudioListener = (SourceAudio.OnAudioListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_tabbed_widget, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parseArgs(getArguments());
        findViews();
        initializePaints();
        mViewCreatedCallback.onViewCreated(this);
        if(!view.isInEditMode()){
            mSrcPlayer.initSrcAudio(mProject, mFilename, mChapter);
        }
        attachListeners();
        mSwitchToMinimap.setSelected(true);
        mTimecodeLayer = TimecodeLayer.newInstance(getActivity());
        mMinimapLayer = MinimapLayer.newInstance(getActivity(), this);
        mMarkerLineLayer = MarkerLineLayer.newInstance(getActivity(), this);
        mGestureLayer = ScrollGestureLayer.newInstance(getActivity(), this, this);
        mHighlightLayer = RectangularHighlightLayer.newInstance(getActivity(), this);
        mMinimapFrame.addView(mMinimapLayer);
        mMinimapFrame.addView(mTimecodeLayer);
        mMinimapFrame.addView(mGestureLayer);
        mMinimapFrame.addView(mMarkerLineLayer);
        mMinimapFrame.addView(mHighlightLayer);
    }

    private void initializePaints(){
        mLocationPaint = new Paint();
        mLocationPaint.setStyle(Paint.Style.STROKE);
        mLocationPaint.setStrokeWidth(2f);
        mLocationPaint.setColor(getResources().getColor(R.color.primary));

        mSectionPaint = new Paint();
        mSectionPaint.setColor(getResources().getColor(R.color.dark_moderate_cyan));
        mSectionPaint.setStyle(Paint.Style.STROKE);
        mSectionPaint.setStrokeWidth(2f);

        mVersePaint = new Paint();
        mVersePaint.setColor(getResources().getColor(R.color.tertiary));
        mVersePaint.setStyle(Paint.Style.STROKE);
        mVersePaint.setStrokeWidth(2f);
    }

    public int getWidgetWidth(){
        return getView().getWidth() - mSwitchToMinimap.getWidth();
    }

    void parseArgs(Bundle args){
        mProject = args.getParcelable(KEY_PROJECT);
        mFilename = args.getString(KEY_FILENAME);
        mChapter = args.getInt(KEY_CHAPTER);
    }

    void findViews(){
        View view = getView();
        mSwitchToMinimap = (ImageButton) view.findViewById(R.id.switch_minimap);
        mSwitchToPlayback = (ImageButton) view.findViewById(R.id.switch_source_playback);
        mSrcPlayer = (SourceAudio) view.findViewById(R.id.srcAudioPlayer);
        mMinimapFrame = (FrameLayout) view.findViewById(R.id.minimap);
    }

    void attachListeners(){
        mSwitchToMinimap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Refactor? Maybe use radio button to select one and exclude the other?
                v.setSelected(true);
                v.setBackgroundColor(Color.parseColor("#00000000"));
                mMinimapFrame.setVisibility(View.VISIBLE);
                mSrcPlayer.setVisibility(View.INVISIBLE);
                mSwitchToPlayback.setSelected(false);
                mSwitchToPlayback.setBackgroundColor(getResources().getColor(R.color.mostly_black));
            }
        });

        mSwitchToPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Refactor? Maybe use radio button to select one and exclude the other?
                v.setSelected(true);
                v.setBackgroundColor(Color.parseColor("#00000000"));
                mSrcPlayer.setVisibility(View.VISIBLE);
                mMinimapFrame.setVisibility(View.INVISIBLE);
                mSwitchToMinimap.setSelected(false);
                mSwitchToMinimap.setBackgroundColor(getResources().getColor(R.color.mostly_black));
            }
        });

        mSrcPlayer.setSourceAudioListener(mAudioListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSrcPlayer.pauseSource();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSrcPlayer.cleanup();
        mViewCreatedCallback = null;
    }

    public SourceAudio getSrcPlayer() {
        return mSrcPlayer;
    }

    public void initializeTimecode(int durationMs){
        mTimecodeLayer.setAudioLength(durationMs);
    }

    @Override
    public boolean onDelegateMinimapDraw(Canvas canvas, Paint paint) {
//        if(minimap == null) {
//            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
//            minimap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), conf);
//            Canvas temp = new Canvas(minimap);
//            mPaint = new Paint();
            boolean success =  mMinimapDrawDelegator.onDelegateMinimapDraw(canvas, paint);
            if(!success){
                invalidateMinimap();
                return false;
            }
        //}
        //canvas.drawBitmap(minimap, 0, 0, mPaint);
        return true;
    }

    public void invalidateMinimap(){
        mMinimapLayer.invalidateMinimap();
    }

    public void onLocationChanged(){
        mMinimapLayer.postInvalidate();
        initializeTimecode(mMediaController.getDurationMs());
        mTimecodeLayer.postInvalidate();
        mMarkerLineLayer.postInvalidate();
        mHighlightLayer.postInvalidate();
    }

    @Override
    public void onDrawMarkers(Canvas canvas) {
        mMinimapLineDrawDelegator.onDelegateMinimapMarkerDraw(canvas, mLocationPaint, mSectionPaint, mVersePaint);
    }

    @Override
    public void onDrawHighlight(Canvas canvas, Paint paint) {
        if(mMediaController.hasSetMarkers()) {
            float left = (mMediaController.getStartMarkerFrame() / (float) mMediaController.getDurationInFrames()) * canvas.getWidth();
            float right = (mMediaController.getEndMarkerFrame() / (float) mMediaController.getDurationInFrames()) * canvas.getWidth();
            canvas.drawRect(left, 0, right, canvas.getHeight(), paint);
        }
    }

    @Override
    public void onScroll(float rawX1, float rawX2, float distX) {
        if(mMediaController.isInEditMode()) {
            if (rawX1 > rawX2) {
                float temp = rawX2;
                rawX2 = rawX1;
                rawX1 = temp;
            }
            mMediaController.setStartMarkerAt((int) (rawX1 / (float) getWidgetWidth() * mMediaController.getDurationInFrames()));
            mMediaController.setEndMarkerAt((int) (rawX2 / (float) getWidgetWidth() * mMediaController.getDurationInFrames()));
            mMarkerMediator.updateStartMarkerFrame(mMediaController.getStartMarkerFrame());
            mMarkerMediator.updateEndMarkerFrame(mMediaController.getEndMarkerFrame());
            onLocationChanged();
        }
    }

    @Override
    public void onScrollComplete() {}

    @Override
    public void onTap(float x) {
        mMediaController.onSeekTo((x/(float)getWidgetWidth()));
    }

    public void invalidate(int ms) {

    }
}
