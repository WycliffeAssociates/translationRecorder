package org.wycliffeassociates.translationrecorder.AudioVisualization;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import org.wycliffeassociates.translationrecorder.R;

public abstract class CanvasView extends View {

    protected Paint mPaintGrid;
    protected Paint mPaintWaveform;
    protected Paint mPaintBaseLine;
    protected ActiveRecordingRenderer mManager;

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    protected void init(){

        int dpSize =  1;
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);

        mPaintGrid = new Paint();
        mPaintGrid.setColor(Color.GRAY);
        mPaintGrid.setStyle(Paint.Style.STROKE);
        mPaintGrid.setStrokeWidth(strokeWidth);

        mPaintWaveform = new Paint();
        mPaintWaveform.setStrokeWidth(strokeWidth);
        mPaintWaveform.setColor(getResources().getColor(R.color.off_white));

        mPaintBaseLine = new Paint();
        mPaintBaseLine.setColor(getResources().getColor(R.color.secondary));
        mPaintBaseLine.setStrokeWidth(strokeWidth);
    }

    // override onDrawMarkers
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0.f, this.getMeasuredHeight() / 2, this.getMeasuredWidth(), this.getMeasuredHeight() / 2, mPaintBaseLine);
    }

    public synchronized void drawWaveform(float[] samples, Canvas canvas){
        canvas.drawLines(samples, mPaintWaveform);
    }

    public void setUIDataManager(ActiveRecordingRenderer manager){
        mManager = manager;
    }

}

