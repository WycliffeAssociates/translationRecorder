package wycliffeassociates.recordingapp.AudioVisualization;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.WavPlayer;
import wycliffeassociates.recordingapp.R;

/**
 * A canvas view intended for use as the main waveform
 */
public class WaveformView extends CanvasView {

    private byte[] mBuffer;
    private boolean mDrawingFromBuffer = false;
    private float[] mSamples;
    private int mTimeToDraw;
    private int mMarkerStartLoc;
    private int mMarkerEndLoc;
    private ScaleGestureDetector sgd;
    private CutOp mCut;

    public void setCut(CutOp cut){
        mCut = cut;
    }

    /**
     * Sets the location (in time (ms)) for the start marker
     * @param markerStart start marker location in ms
     */
    public void setMarkerToDrawStart(int markerStart) {
        this.mMarkerStartLoc = markerStart;
    }

    /**
     * Sets the location (in time (ms)) for the end marker
     * @param markerEnd end marker location in ms
     */
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
                int playbackSectionStart = (int) (distX * 3) + WavPlayer.getLocation();

                if(distX > 0) {
                    int skip = mCut.skip(playbackSectionStart);
                    if (skip != -1) {
                        playbackSectionStart = skip + 2;
                    }
                } else {
                    int skip = mCut.skipReverse(playbackSectionStart);
                    if(skip != Integer.MAX_VALUE){
                        playbackSectionStart = skip - 2;
                    }
                }

                //Log.i(this.toString(), "start is now " + playbackSectionStart + " duration is " + WavPlayer.getDuration() + " location was " + WavPlayer.getLocation());
                //Ensure scrolling cannot pass an end marker if markers are set.
                //The seek is to ensure responsiveness; without it the waveform will not scroll
                //at all if the user slides their finger too far
                if(SectionMarkers.getEndLocationMs() < playbackSectionStart){
                    WavPlayer.seekTo(SectionMarkers.getEndLocationMs());
                //Same as above but the check is to make sure scrolling will not go before a marker
                } else if(SectionMarkers.getStartLocationMs() > playbackSectionStart){
                    WavPlayer.seekTo(SectionMarkers.getStartLocationMs());
                } else {
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
     * @param startTimeMs time in milliseconds of where to place a start marker
     */
    public void placeStartMarker(int startTimeMs){
        SectionMarkers.setStartTime(startTimeMs, getWidth());
        //if both markers are set, then set the start and end markers in WavPlayer
        if(SectionMarkers.bothSet()){
            setWavPlayerSelectionMarkers();
        }
        //draw the placed marker
        invalidate();
        redraw();
    }

    /**
     * Updates the end position in the marker object. If this means both markers are now set,
     * WavPlayer needs to set start and end locations
     * @param endTimeMS time in milliseconds of where to place an end marker
     */
    public void placeEndMarker(int endTimeMS){
        SectionMarkers.setEndTime(endTimeMS, getWidth());
        if(SectionMarkers.bothSet()){
            setWavPlayerSelectionMarkers();
        }
        invalidate();
        redraw();
    }

    /**
     * Sets the start and end markers in the WavPlayer
     */
    public void setWavPlayerSelectionMarkers(){
        WavPlayer.startSectionAt(SectionMarkers.getStartLocationMs());
        WavPlayer.stopSectionAt(SectionMarkers.getEndLocationMs());
    }

    /**
     * Passes a touch event to the scroll and scale gesture detectors, if they exist
     * @param ev the gesture detected
     * @return returns true to signify the event was handled
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mDetector!= null) {
            mDetector.onTouchEvent(ev);
        }
        if(sgd != null) {
            sgd.onTouchEvent(ev);
        }
        return true;
    }

    //TODO: make a paint variable for the playback line rather than use one and swap colors
    /**
     * Draws the playback line on the canvas passed in
     * @param canvas the canvas to be drawn to
     */
    public void drawMarker(Canvas canvas){
        mPaint.setStrokeWidth(1.f);
        mPaint.setColor(getResources().getColor(R.color.tertiary));
        //positions the playback line 1/8th of the total width from the left of the screen
        canvas.drawLine((canvas.getWidth() / 8), 0,
                (canvas.getWidth() / 8), canvas.getHeight(), mPaint);
    }

    /**
     * Constructs a WaveformView (which is a canvas view, meant for displaying the main waveform)
     * Sets up gesture detectors for interacting with the main waveform
     * @param c is the context of the activity running
     * @param attrs attributes to be passed to the super class
     */
    public WaveformView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mDetector = new GestureDetectorCompat(getContext(), new MyGestureListener());
        sgd = new ScaleGestureDetector(getContext(), new ScaleListener());
        init();
    }

    /**
     * Sets the state of the view to draw waveforms from buffers that will be passed in
     * This implies that the app is recording
     * @param b True to draw from a buffer (from the mic), False to draw samples of the Waveform
     */
    public void setDrawingFromBuffer(boolean b){
        this.mDrawingFromBuffer = b;
    }

    //TODO: make a separate paint variable for the start and end markers, rather than swap colors
    //TODO: change a constant to match the number of seconds on the screen
    /**
     * Draws the start and end markers
     * @param c
     */
    public void drawSectionMarkers(Canvas c){
        //FIXME: need to change this to match number of seconds on the screen instead of constant 10
        //compute the number of milliseconds in one pixel
        float mspp = 1000*10/(float)getWidth();
        //offset refers to the location where playback actually starts (at the playback line)
        int offset = (getWidth() / 8);
        //compute the position on the screen to draw markers. Marker locations and mTimeToDraw
        //are both in ms
        float xLoc1 = offset + (mMarkerStartLoc - mTimeToDraw)/mspp;
        float xLoc2 = offset + (mMarkerEndLoc - mTimeToDraw)/mspp;
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.BLUE);
        c.drawLine(xLoc1, 0, xLoc1, getHeight(), mPaint);
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.RED);
        c.drawLine(xLoc2, 0, xLoc2, getHeight(), mPaint);
        mPaint.setColor(Color.BLUE);
        mPaint.setAlpha(50);
        mPaint.setStyle(Paint.Style.FILL);
        c.drawRect(xLoc1, 0, xLoc2, getHeight(), mPaint);
        mPaint.setAlpha(255);
    }

    /**
     * Sets the time in playback to draw this frame
     * This is set so that both the waveform and the markers make use of the same time,
     * rather than each querying WavPlayer when they get to draw their component.
     * @param timeMs Current time during playback, in milliseconds
     */
    public void setTimeToDraw(int timeMs){
        this.mTimeToDraw = timeMs;
    }

    //TODO: remove the semaphore, replace with either synchronous or try to remove concurrency
    /**
     * Main draw method that is called when the view is invalidated.
     * @param canvas The canvas which can be drawn on. Provided by Android as onDraw is not
     *               called explicitly.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //DrawingFromBuffers will draw data recieved from the microphone during recording
        if(mDrawingFromBuffer){
            try {
                UIDataManager.lock.acquire();
                drawBuffer(canvas, mBuffer, AudioInfo.BLOCKSIZE);
                UIDataManager.lock.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        //Samples is a sampled section of the waveform extracted at mTimeToDraw
        } else if (mSamples != null ){
            try {
                drawWaveform(mSamples, canvas);
                drawMarker(canvas);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        //Creates a drawing loop; redraws only will occur if audio is playing
        redraw();
        if(SectionMarkers.shouldDrawMarkers()){
            drawSectionMarkers(canvas);
        }
        WavPlayer.checkIfShouldStop();
        //Determines whether the play or pause button should be rendered
        //This is done now that there is not a thread dedicated to drawing
        if(WavPlayer.exists() && !WavPlayer.isPlaying()){
            mManager.enablePlay();
        }
    }

    /**
     * Sets a byte buffer to be drawn to the screen
     * @param buffer a byte buffer containing 16 bit pcm data
     */
    public void setBuffer(byte[] buffer){
        mBuffer = buffer;
    }

    //TODO: create a separate paint object for drawing the waveform
    /**
     * Draws a waveform from the buffer produced while recording
     * @param canvas the canvas to draw to
     * @param buffer the byte buffer containing 16 bit pcm data to draw
     * @param blocksize the size of a block of audio data; 2 for 16 bit mono PCM
     */
    public void drawBuffer(Canvas canvas, byte[] buffer, int blocksize){
        mPaint.setStrokeWidth(1.5f);
        mPaint.setColor(Color.WHITE);
        if (buffer == null || canvas == null) {
            return;
        }
        //convert PCM data in a byte array to a short array
        Short[] temp = new Short[buffer.length/blocksize];
        int index = 0;
        for(int i = 0; i<buffer.length; i+=blocksize){
            byte low = buffer[i];
            byte hi = buffer[i + 1];
            //PCM data is stored little endian
            temp[index] = (short)(((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
            index++;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        double xScale = width/(index *.999);
        double yScale = height/65536.0;
        for(int i = 0; i < temp.length-1; i++){
            canvas.drawLine((int)(xScale*i), (int)((yScale*temp[i])+ height/2),
                    (int)(xScale*(i+1)), (int)((yScale*temp[i+1]) + height/2), mPaint);
        }
        this.postInvalidate();
    }

    /**
     * Sets sampled waveform data to draw to the screen
     * @param samples sampled waveform data to draw
     */
    public void setWaveformDataForPlayback(float[] samples){
        this.mSamples = samples;
    }
}
