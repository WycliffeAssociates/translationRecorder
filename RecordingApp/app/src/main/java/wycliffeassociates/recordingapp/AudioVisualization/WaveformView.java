package wycliffeassociates.recordingapp.AudioVisualization;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Playback.WavPlayer;

/**
 * Created by sarabiaj on 9/10/2015.
 */
public class WaveformView extends CanvasView {

    private byte[] buffer;
    private boolean drawingFromBuffer = false;
    private float[] samples;
    private int timeToDraw;
    private int mMarkerStartLoc;
    private int mMarkerEndLoc;
    private ScaleGestureDetector sgd;

    public void setMarkerToDrawStart(int markerStart) {
        this.mMarkerStartLoc = markerStart;
    }

    public void setMarkerToDrawEnd(int markerEnd) {
        this.mMarkerEndLoc = markerEnd;
    }


    /**
     * Detects gestures on the main canvas
     */
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * Detects if the user is scrolling the main waveform horizontally
         * @param distX refers to how far the user scrolled horizontally
         * @param distY is ignored for this use as we are only allowing horizontal scrolling
         * @param event1 not accessed, contains information about the start of the gesture
         * @param event2 not used, contains information about the end of the gesture
         * @return must be true for gesture detection
         */
        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distX, float distY) {
            //Should only perform a scroll if the WavPlayer exists, since scrolling performs a seek
            if (WavPlayer.exists()) {
                //moves playback by the distance (distX is multiplied so as to scroll at a more
                //reasonable speed. 3 seems to work well, but is mostly arbitrary.
                int playbackSectionStart = (int) ((distX*3) + WavPlayer.getLocation());

                //Ensure scrolling cannot pass an end marker if markers are set.
                //The seek is to ensure responsiveness; without it the waveform will not scroll
                //at all if the user slides their finger too far
                if(sMarkers.getEndLocation() < playbackSectionStart){
                    WavPlayer.seekTo(sMarkers.getEndLocation());
                }
                //Same as above but the check is to make sure scrolling will not go before a marker
                else if(sMarkers.getStartLocation() > playbackSectionStart){
                    WavPlayer.seekTo(sMarkers.getStartLocation());
                }
                else {
                    WavPlayer.seekTo(playbackSectionStart);
                }
                //Redraw in order to display the waveform in the scrolled position
                mManager.updateUI();
            }
            return true;
        }
    }

    //TODO: scale should adjust userscale in the WavVisualizer class
    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            System.out.println("scaled");
            return true;
        }
    }

    /**
     * Updates the start position in the marker object. If this means both markers are now set,
     * WavPlayer needs to set start and stop locations
     * @param startTimeMS
     */
    public void placeStartMarker(int startTimeMS){
        sMarkers.setStartTime(startTimeMS, getWidth());
        //if both markers are set, then set the start and end markers in WavPlayer via notify
        if(sMarkers.bothSet()){
            notifyWavPlayer();
        }
        //draw the placed marker
        invalidate();
        redraw();
    }

    /**
     * Updates the end position in the marker object. If this means both markers are now set,
     * WavPlayer needs to set start and end locations
     * @param endTimeMS
     */
    public void placeEndMarker(int endTimeMS){
        sMarkers.setEndTime(endTimeMS, getWidth());
        if(sMarkers.bothSet()){
            notifyWavPlayer();
        }
        invalidate();
        redraw();
    }

    public void notifyWavPlayer(){
        WavPlayer.selectionStart(sMarkers.getStartLocation());
        WavPlayer.stopAt(sMarkers.getEndLocation());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mDetector!= null)
            mDetector.onTouchEvent(ev);
        if(sgd != null)
            sgd.onTouchEvent(ev);
        return true;
    }

    public void drawMarker(Canvas canvas){
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.RED);
        canvas.drawLine((canvas.getWidth() / 8), 0, (canvas.getWidth() / 8), canvas.getHeight(), mPaint);
    }

    public WaveformView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mDetector = new GestureDetectorCompat(getContext(), new MyGestureListener());
        sgd = new ScaleGestureDetector(getContext(), new ScaleListener());
        init();
    }

    public void setDrawingFromBuffer(boolean c){
        this.drawingFromBuffer = c;
    }

    public void drawSectionMarkers(Canvas c){
        System.out.println(WavPlayer.getLocation() + " is the location for the last section marker draw");
        //need to change this to match number of seconds on the screen instead of constant 10
        float mspp = 1000*10/(float)getWidth();
        int offset = (getWidth() / 8);
//        float xLoc1 = offset + (markers.getStartLocation() - WavPlayer.getLocation())/mspp;
//        float xLoc2 = offset + (markers.getEndLocation() - WavPlayer.getLocation())/mspp;
        float xLoc1 = offset + (mMarkerStartLoc - timeToDraw)/mspp;
        float xLoc2 = offset + (mMarkerEndLoc - timeToDraw)/mspp;
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.BLUE);
        System.out.println(xLoc1 + " " + offset + " offset" + sMarkers.getStartLocation() + " start time" + WavPlayer.getLocation() + " start loc" + mspp + " mspp");
        c.drawLine(xLoc1, 0, xLoc1, getHeight(), mPaint);
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.RED);
        c.drawLine(xLoc2, 0, xLoc2, getHeight(), mPaint);
    }

    public void setTimeToDraw(int timeMS){
        this.timeToDraw = timeMS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(drawingFromBuffer){
            try {
                UIDataManager.lock.acquire();
                drawBuffer(canvas, buffer, AudioInfo.BLOCKSIZE, AudioInfo.NUM_CHANNELS);
                UIDataManager.lock.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (samples != null ){
            try {
                UIDataManager.lock.acquire();
                drawWaveform(samples, canvas);
                UIDataManager.lock.release();
                drawMarker(canvas);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        redraw();
        if(sMarkers.shouldDrawMarkers()){
            drawSectionMarkers(canvas);
        }
        WavPlayer.checkIfShouldStop();
        if(WavPlayer.exists() && !WavPlayer.isPlaying()){
            mManager.enablePlay();
        }
    }

    public void setBuffer(byte[] buffer){
        this.buffer = buffer;
    }

    public void drawBuffer(Canvas canvas, byte[] buffer, int blocksize, int numChannels){
        mPaint.setColor(Color.WHITE);
        if (buffer == null || canvas == null) {
            System.out.println("returning");
            return;
        }
        Short[] temp = new Short[buffer.length/blocksize];
        int index = 0;
        for(int i = 0; i<buffer.length; i+=blocksize){
            byte low = buffer[i];
            byte hi = buffer[i + 1];
            temp[index] = (short)(((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
            index++;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();
        double xScale = width/(index *.999);
        double yScale = height/65536.0;
        for(int i = 0; i < temp.length-1; i++){
            canvas.drawLine((int)(xScale*i), (int)((yScale*temp[i])+ height/2), (int)(xScale*(i+1)), (int)((yScale*temp[i+1]) + height/2), mPaint);
        }
        this.postInvalidate();
    }

    public void setWaveformDataForPlayback(float[] samples){
        this.samples = samples;
    }
}
