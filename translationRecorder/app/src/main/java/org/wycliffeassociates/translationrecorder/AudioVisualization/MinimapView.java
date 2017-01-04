package org.wycliffeassociates.translationrecorder.AudioVisualization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.wycliffeassociates.translationrecorder.Reporting.Logger;
import org.wycliffeassociates.translationrecorder.Playback.Editing.CutOp;
import org.wycliffeassociates.translationrecorder.Playback.Editing.SectionMarkers;
import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 9/10/2015.
 */
public class MinimapView extends CanvasView {

    private Bitmap mBitmap;
    private float miniMarkerLoc;
    private Canvas mCanvas = null;
    private volatile boolean initialized = false;
    private int audioLength = 0;
    private double secondsPerPixel = 0;
    private double timecodeInterval = 1.0;
    private float[] mSamples;
    private CutOp mCut;

    public void setCut(CutOp cut){
        mCut = cut;
    }


    public MinimapView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mDetector = new GestureDetectorCompat(getContext(), new MyGestureListener());
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private int startPosition = 0;
        private int endPosition = 0;

        @Override
        public boolean onDown(MotionEvent e) {
            if (mManager != null && e.getY() <= getHeight()) {
                float xPos = e.getX() / (float)getWidth();
                int timeToSeekTo = mManager.timeAdjusted(Math.round(xPos * mManager.getAdjustedDuration()));
                if(SectionMarkers.bothSet()){
                    if(timeToSeekTo < SectionMarkers.getStartLocationMs()){
                        return true;
                    }
                    else if(timeToSeekTo > SectionMarkers.getEndLocationMs()){
                        return true;
                    }
                }
                mManager.seekTo(timeToSeekTo);
                mManager.updateUI();
                endPosition = (int) e.getX();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            if (mManager != null) {
//                startPosition = (int) event1.getX();
//                endPosition = (int) event2.getX();
//                int startPositionMinimap = startPosition;
//                int endPositionMinimap = endPosition;
//                int playbackSectionStart = (int) mCut.timeAdjusted((int)Math.round((startPositionMinimap / (double) getWidth()) * (mManager.getDuration() - mCut.getSizeCut())));
//                int playbackSectionEnd = (int) mCut.timeAdjusted((int)Math.round((endPositionMinimap / (double) getWidth()) * (mManager.getDuration() - mCut.getSizeCut())));
//                if (startPosition > endPosition) {
//                    int temp = playbackSectionEnd;
//                    playbackSectionEnd = playbackSectionStart;
//                    playbackSectionStart = temp;
//                    temp = endPositionMinimap;
//                    endPositionMinimap = startPositionMinimap;
//                    startPositionMinimap = temp;
//                }
//                SectionMarkers.setMinimapMarkers(startPositionMinimap, endPositionMinimap);
//                SectionMarkers.setMainMarkers(playbackSectionStart, playbackSectionEnd);
//                mManager.startSectionAt(playbackSectionStart);
//                mManager.seekTo(playbackSectionStart);
//                mManager.stopSectionAt(playbackSectionEnd);
//                //BufferPlayer.selectionStart(playbackSectionStart);
//                // TODO: Figure out a way to call PlaybackScreen.placeStartMarker and PlaybackScreen.placeEndMarker instead of re-writing the code here
//                int toShow[] = {R.id.btn_clear, R.id.btn_cut};
//                int toHide[] = {R.id.btn_end_mark, R.id.btn_start_mark};
//                mManager.swapViews(toShow, toHide);
//                mManager.updateUI();
            }
            return true;
        }
    }


    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(initialized) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaintGrid);
        } else if(mSamples != null) {
            drawWaveform(mSamples, canvas);
        }
        minimapMarker(canvas);
        drawTimeCode(canvas);
        if(SectionMarkers.shouldDrawMarkers() ){
            int start = SectionMarkers.getMinimapMarkerStart();
            int end = SectionMarkers.getMinimapMarkerEnd();
//            int start = (int)(mCut.timeAdjusted((int)Math.round((SectionMarkers.getMinimapMarkerStart() / (double) getWidth()) * BufferPlayer.getDuration())) / (double) (BufferPlayer.getDuration() - mCut.getSizeCut()) * getWidth());
//            int end = (int)(mCut.timeAdjusted((int)Math.round((SectionMarkers.getMinimapMarkerEnd() / (double) getWidth()) * BufferPlayer.getDuration())) / (double) (BufferPlayer.getDuration() - mCut.getSizeCut()) * getWidth());
            drawPlaybackSection(canvas, start, end);
            //System.out.println("should have drawn sMarkers on minimap at " + SectionMarkers.getMinimapMarkerStart());

            canvas.drawRect(start, 0, end, getHeight(), mPaintHighlight);
        }
    }

    public void setMiniMarkerLoc(float miniMarkerLoc) {
        this.miniMarkerLoc = miniMarkerLoc;
        this.postInvalidate();
    }

    private void computeTimecodeInterval(){
        timecodeInterval = 1.0;
        if(timecodeInterval / secondsPerPixel >= 50){
            return;
        }
        else{
            timecodeInterval = 0.d;
        }
        while(timecodeInterval / secondsPerPixel < 50){
            timecodeInterval += 5.0;
        }
    }

    public void setAudioLength(int length){
        //System.out.println("Audio data length for timecode is " + length);
        this.audioLength = (int)(length/1000.0);
        this.secondsPerPixel = audioLength / (double)getWidth();
        computeTimecodeInterval();
    }

    public void drawTimeCode(Canvas canvas){
        //System.out.println("secondsPerPixel is " + secondsPerPixel + " interval is " + timecodeInterval);
        mPaintText.setColor(getResources().getColor(R.color.minimap_timecode));
        mPaintText.setTextSize(18.f);
        int i = 0;
        double fractionalSecs = secondsPerPixel;
        int integerTimecode = (int) (fractionalSecs / timecodeInterval);
        Rect bounds = new Rect();
        while (i < getWidth()){

            i++;
            fractionalSecs += secondsPerPixel;
            int integerSecs = (int) fractionalSecs;

            int integerTimecodeNew = (int) (fractionalSecs /
                    timecodeInterval);
            if (integerTimecodeNew != integerTimecode) {
                integerTimecode = integerTimecodeNew;

                //System.out.println("integer is " + integerSecs);
                // Turn, e.g. 67 seconds into "1:07"
                String timecodeMinutes = "" + (integerSecs / 60);
                String timecodeSeconds = "" + (integerSecs % 60);
                if ((integerSecs % 60) < 10) {
                    timecodeSeconds = "0" + timecodeSeconds;
                }
                String timecodeStr = timecodeMinutes + ":" + timecodeSeconds;
                mPaintText.getTextBounds(timecodeStr, 0, timecodeStr.length(), bounds);
                float padding = getResources().getDimension(R.dimen.default_padding_xs);
                float xOffset = mPaintText.measureText(timecodeStr) + padding;
                float yOffset = bounds.height() + padding;
                canvas.drawText(timecodeStr, i - xOffset, yOffset, mPaintText);
                canvas.drawLine(i, 0.f, i, getHeight(), mPaintGrid);
            }
        }

    }

    public void init(final float[] samples){
        mSamples = samples;
        initialized = false;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.w(MinimapView.class.toString(), "Saving minimap to BMP");
                mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
                Drawable background = getBackground();
                if(background != null){
                    background.draw(mCanvas);
                }
                else
                    mCanvas.drawColor(Color.TRANSPARENT);
                drawWaveform(samples, mCanvas);
                setBackground(background);
                Logger.w(MinimapView.class.toString(), "Created a BMP");
                initialized = true;
            }
        });
        t.start();
    }

    public void minimapMarker(Canvas canvas){
        canvas.drawLine(miniMarkerLoc, 0, miniMarkerLoc, canvas.getHeight(), mPaintPlayback);
    }

}
