package wycliffeassociates.recordingapp.AudioVisualization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import wycliffeassociates.recordingapp.Playback.WavPlayer;

/**
 * Created by sarabiaj on 9/10/2015.
 */
public class MinimapView extends CanvasView {

    private Bitmap mBitmap;
    private float miniMarkerLoc;
    private Canvas mCanvas = null;
    private Drawable background;
    private boolean initialized = false;
    private int audioLength = 0;
    private double secondsPerPixel = 0;
    private double timecodeInterval = 1.0;

    public MinimapView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mDetector = new GestureDetectorCompat(getContext(), new MyGestureListener());
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private int startPosition = 0;
        private int endPosition = 0;

        @Override
        public boolean onDown(MotionEvent e) {
            if (WavPlayer.exists() && e.getY() <= getHeight()) {
                if(sMarkers.bothSet()){
                    float xPos = e.getX() / getWidth();
                    int timeToSeekTo = Math.round(xPos * WavPlayer.getDuration());
                    if(timeToSeekTo < sMarkers.getStartLocation()){
                        return true;
                    }
                    else if(timeToSeekTo > sMarkers.getEndLocation()){
                        return true;
                    }
                }
                float xPos = e.getX() / getWidth();
                int timeToSeekTo = Math.round(xPos * WavPlayer.getDuration());
                WavPlayer.seekTo(timeToSeekTo);
                mManager.updateUI();
                endPosition = (int) e.getX();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            if (WavPlayer.exists()) {
                startPosition = (int) event1.getX();
                endPosition -= (int) distanceX;
                sMarkers.setMinimapMarkers(startPosition, endPosition);
                int playbackSectionStart = (int) ((startPosition / (double) getWidth()) * WavPlayer.getDuration());
                int playbackSectionEnd = (int) ((endPosition / (double) getWidth()) * WavPlayer.getDuration());
                if (startPosition > endPosition) {
                    int temp = playbackSectionEnd;
                    playbackSectionEnd = playbackSectionStart;
                    playbackSectionStart = temp;
                }
                sMarkers.setMainMarkers(playbackSectionStart, playbackSectionEnd);
                WavPlayer.startSectionAt(playbackSectionStart);
                WavPlayer.seekTo(playbackSectionStart);
                WavPlayer.stopSectionAt(playbackSectionEnd);
                //WavPlayer.selectionStart(playbackSectionStart);
                mManager.updateUI();
            }
            return true;
        }
    }


    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(initialized){
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            minimapMarker(canvas);
            drawTimeCode(canvas);
            if(sMarkers.shouldDrawMarkers() ){
                drawPlaybackSection(canvas, sMarkers.getMinimapMarkerStart(), sMarkers.getMinimapMarkerEnd());
                System.out.println("should have drawn sMarkers on minimap at " + sMarkers.getMinimapMarkerStart());
            }
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
        System.out.println("Audio data length for timecode is " + length);
        this.audioLength = (int)(length/1000.0);
        this.secondsPerPixel = audioLength / (double)getWidth();
        computeTimecodeInterval();
    }

    public void drawTimeCode(Canvas canvas){
        //System.out.println("secondsPerPixel is " + secondsPerPixel + " interval is " + timecodeInterval);
        float mDensity = 2.0f;
        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(18.f);
        int i = 0;
        double fractionalSecs = secondsPerPixel;
        int integerTimecode = (int) (fractionalSecs / timecodeInterval);
        while (i < getWidth()){
            mPaint.setColor(Color.GREEN);

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
                float offset = (float) (
                        0.5 * mPaint.measureText(timecodeStr));
                canvas.drawText(timecodeStr,
                        i - offset,
                        (int)(12 * mDensity),
                        mPaint);
                mPaint.setColor(Color.GRAY);
                canvas.drawLine(i, 0.f, i, getHeight(), mPaint);
            }
        }

    }

    public void init(float[] samples){
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Runtime.getRuntime().freeMemory();
        System.out.println("Saving minimap to BMP...");
        System.out.println("Created a BMP...");
        mCanvas = new Canvas(mBitmap);
        Drawable background = getBackground();
        if(background != null){
            background.draw(mCanvas);
        }
        else
            mCanvas.drawColor(Color.TRANSPARENT);
        drawWaveform(samples, mCanvas);
        setBackground(background);
        initialized = true;
        this.invalidate();
    }

    public void minimapMarker(Canvas canvas){
        mPaint.setColor(Color.GREEN);
        canvas.drawLine(miniMarkerLoc, 0, miniMarkerLoc, canvas.getHeight(), mPaint);
    }

}
