package wycliffeassociates.recordingapp.AudioVisualization;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import wycliffeassociates.recordingapp.Playback.WavMediaPlayer;

public abstract class CanvasView extends View {

    protected Paint mPaint;
    int fps = 0;
    protected boolean doneDrawing = false;
    UIDataManager manager;

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
        if(WavMediaPlayer.isPlaying())
        manager.updateUI();
    }

    public void setUIDataManager(UIDataManager manager){
        this.manager = manager;
    }

}

