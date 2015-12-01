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
    private ScaleGestureDetector sgd;

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            System.out.println("here trying to scroll");
            if (WavPlayer.exists() && event1.getY() <= getHeight()) {
                int playbackSectionStart = (int) ((distanceX*3) + WavPlayer.getLocation());
                System.out.println("playback start is " + playbackSectionStart + " distance is " + distanceX);
                WavPlayer.seekTo(playbackSectionStart);
                System.out.println("here in the if trying to scroll");
                manager.updateUI();
            }
            return true;
        }
    }
    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            System.out.println("scaled");
            return true;
        }
    }

    public void placeStartMarker(int start){
        markers.setStartTime(start, getWidth());
        invalidate();
        redraw();
    }

    public void placeEndMarker(int end){
        markers.setEndTime(end, getWidth());
        invalidate();
        redraw();
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
        //need to change this to match number of seconds on the screen instead of constant 10
        float mspp = 1000*10/(float)getWidth();
        int offset = (getWidth() / 8);
        float xLoc1 = offset + (markers.getStartLocation() - WavPlayer.getLocation())/mspp;
        float xLoc2 = offset + (markers.getEndLocation() - WavPlayer.getLocation())/mspp;
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.RED);
        System.out.println(xLoc1 + " " + offset + " offset" + markers.getStartLocation() + " start time" + WavPlayer.getLocation() + " start loc" + mspp + " mspp");
        c.drawLine(xLoc1, 0, xLoc1, getHeight(), mPaint);
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.BLUE);
        c.drawLine(xLoc2, 0, xLoc2, getHeight(), mPaint);
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
        if(markers.shouldDrawMarkers()){
            drawSectionMarkers(canvas);
        }
        WavPlayer.checkIfShouldStop();
        if(WavPlayer.exists() && !WavPlayer.isPlaying()){
            manager.enablePlay();
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
