package wycliffeassociates.recordingapp;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class CanvasView extends View {

    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    Context context;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;
    ArrayList<Pair<Double,Double>> samples;
    double xScale;
    double yScale;

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        mPath = new Path();

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.DKGRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(1f);
        samples = null;
        xScale = 0;
        yScale = 0;

    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw the mPath with the mPaint on the canvas when onDraw
        mPaint.setColor(Color.DKGRAY);
        canvas.drawLine(0.f, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight() / 2, mPaint);
        canvas.drawPath(mPath, mPaint);
        drawWaveform(canvas);
    }

    public void drawWaveform(Canvas canvas){
        mPaint.setColor(Color.WHITE);
        if (samples == null) {
            return;
        }
        System.out.println("In the draw waveform function");
        int oldX = 0;
        int oldY =  (canvas.getHeight() / 2);
        int xIndex = 0;

        for (int t = 0; t < samples.size(); t++) {
            int y =  ((int) ((canvas.getHeight() / 2) - 15*(-samples.get(t).first)));
            canvas.drawLine(oldX, oldY, xIndex, y, mPaint);
            oldY = y;
            y = ((int) ((canvas.getHeight() / 2) - 15*(-samples.get(t).second)));
            canvas.drawLine(xIndex, oldY, xIndex, y, mPaint);
            //System.out.println("at x: " + oldX + ", y: " + oldY + "to X: " + xIndex + ", Y: " + y);
            xIndex++;
            oldX = xIndex;
            oldY = y;
        }
    }
    public void setSamples(ArrayList<Pair<Double,Double>> samples){
        this.samples = samples;
    }
    public void setXScale(double xScale){
        this.xScale = xScale;
    }
    public void setYScale(double yScale){
        this.yScale = yScale;
    }


    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearCanvas() {
        mPath.reset();
        invalidate();
    }

    // when ACTION_UP stop touch
    private void upTouch() {
        mPath.lineTo(mX, mY);
    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }
}

/*
*     public void drawWaveform(Canvas canvas){
        if (samples == null) {
            return;
        }
        System.out.println("In the draw waveform function");
        int oldX = 0;
        int oldY =  (canvas.getHeight() / 2);
        int xIndex = 0;
        int test[] = {243,634,42,453,62,745,12,53,756,656,7474,535,24,36,24,25,636,436,363,24,24,24,24,24,24,24};
        for (int t = 0; t < test.length; t++) {
            int y = (canvas.getHeight()) - test[t]/2;//(int) ((height / 2) - (samples[t]));
            canvas.drawLine(oldX, oldY, xIndex, y, mPaint);
            System.out.println("at x: " + oldX + ", y: " + oldY + "to X: " + xIndex + ", Y: " + y);
            xIndex+=2;
            oldX = xIndex;
            oldY = y;
        }
    }
*
* */