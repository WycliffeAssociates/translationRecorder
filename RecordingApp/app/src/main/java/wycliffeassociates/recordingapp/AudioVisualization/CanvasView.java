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
import wycliffeassociates.recordingapp.R;

public abstract class CanvasView extends View {

    protected Paint mPaintGrid;
    protected Paint mPaintText;
    protected Paint mPaintStartMarker;
    protected Paint mPaintEndMarker;
    protected Paint mPaintHighlight;
    protected Paint mPaintPlayback;
    protected Paint mPaintWaveform;
    protected Paint mPaintBaseLine;
    int fps = 0;
    protected boolean doneDrawing = false;
    protected UIDataManager mManager;
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
        mPaintGrid = new Paint();
        mPaintGrid.setColor(Color.GRAY);
        mPaintGrid.setStyle(Paint.Style.STROKE);
        mPaintGrid.setStrokeWidth(1f);

        mPaintText = new Paint();
        mPaintText.setTextSize(28.f);
        mPaintText.setColor(Color.GREEN);

        mPaintStartMarker = new Paint();
        mPaintStartMarker.setColor(getResources().getColor(R.color.dark_moderate_lime_green));
        mPaintStartMarker.setStyle(Paint.Style.STROKE);
        mPaintStartMarker.setStrokeWidth(2f);

        mPaintEndMarker = new Paint();
        mPaintEndMarker.setColor(getResources().getColor(R.color.vivid_red));
        mPaintEndMarker.setStyle(Paint.Style.STROKE);
        mPaintEndMarker.setStrokeWidth(2f);

        mPaintHighlight = new Paint();
        mPaintHighlight.setColor(Color.BLUE);
        mPaintHighlight.setAlpha(35);
        mPaintHighlight.setStyle(Paint.Style.FILL);

        mPaintPlayback = new Paint();
        mPaintPlayback.setStrokeWidth(1f);
        mPaintPlayback.setColor(getResources().getColor(R.color.bright_yellow));

        mPaintWaveform = new Paint();
        mPaintWaveform.setStrokeWidth(1.5f);
        mPaintWaveform.setColor(getResources().getColor(R.color.off_white));

        mPaintBaseLine = new Paint();
        mPaintBaseLine.setColor(getResources().getColor(R.color.bright_blue));
        mPaintBaseLine.setStrokeWidth(3f);
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
        canvas.drawLine(0.f, this.getMeasuredHeight() / 2, this.getMeasuredWidth(), this.getMeasuredHeight() / 2, mPaintBaseLine);
    }

    public synchronized void drawWaveform(float[] samples, Canvas canvas){
        canvas.drawLines(samples, mPaintWaveform);
        fps++;
        doneDrawing = true;
    }

    public void redraw(){
        if(WavPlayer.isPlaying())
        mManager.updateUI();
    }

    public void drawPlaybackSection(Canvas c, int start, int end){
        c.drawLine(start, 0, start, c.getHeight(), mPaintStartMarker);
        c.drawLine(end, 0, end, c.getHeight(), mPaintEndMarker);
    }

    public void setUIDataManager(UIDataManager manager){
        mManager = manager;
    }

}

