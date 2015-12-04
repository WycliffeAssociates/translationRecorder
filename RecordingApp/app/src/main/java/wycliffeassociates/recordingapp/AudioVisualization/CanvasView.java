package wycliffeassociates.recordingapp.AudioVisualization;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import wycliffeassociates.recordingapp.Playback.WavPlayer;

public abstract class CanvasView extends View {

    protected Paint mPaint;
    int fps = 0;
    protected boolean doneDrawing = false;
    protected UIDataManager manager;
    protected static SectionMarkers markers = null;
    protected GestureDetectorCompat mDetector;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mDetector!= null)
            mDetector.onTouchEvent(ev);
        return true;
    }

    public boolean isDoneDrawing(){
        return doneDrawing;
    }

    public void setIsDoneDrawing(boolean c){
        doneDrawing = c;
    }

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    public int getFps(){
        return fps;
    }

    public void resetFPS(){
        fps = 0;
    }

    protected void init(){
        mPaint = new Paint();
        mPaint.setColor(Color.DKGRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1f);
        markers = new SectionMarkers();
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.DKGRAY);
        canvas.drawLine(0.f, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2, mPaint);
    }

    public void drawWaveform(float[] samples, Canvas canvas){
        mPaint.setColor(Color.WHITE);
        canvas.drawLines(samples, mPaint);
        fps++;
        doneDrawing = true;
    }

    public void redraw(){
        if(WavPlayer.isPlaying())
        manager.updateUI();
    }

    public void drawPlaybackSection(Canvas c, int start, int end){
        mPaint.setColor(Color.BLUE);
        c.drawLine(start, 0, start, c.getHeight(), mPaint);
        mPaint.setColor(Color.RED);
        c.drawLine(end, 0, end, c.getHeight(), mPaint);
    }

    public void setUIDataManager(UIDataManager manager){
        this.manager = manager;
    }

    public static int getStartMarker(){
        int loc = (int)(markers.getStartLocation()*88.2);
        return (loc % 2 == 0)? loc : loc + 1;
    }

    public static int getEndMarker(){
        int loc = (int)(markers.getEndLocation()*88.2);
        return (loc % 2 == 0)? loc : loc + 1;
    }

    public static void clearMarkers(){
        markers.clearMarkers();
    }
}

