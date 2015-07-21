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
import android.view.ScaleGestureDetector;
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
    ScaleGestureDetector SGD;

    double xScale;
    double yScale;
    float userScale;
    private float xTranslation;


    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        userScale = 1.f;
        xTranslation = 0.f;

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
        canvas.drawLine(0.f, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2, mPaint);
        canvas.drawPath(mPath, mPaint);
        drawWaveform(canvas);

    }

    public void drawWaveform(Canvas canvas){
        canvas.scale(userScale, 1.f);
        canvas.translate(-xTranslation, 0.f);

        mPaint.setColor(Color.WHITE);
        if (samples == null) {
            return;
        }
        int oldX = 0;
        int oldY =  (canvas.getHeight() / 2);
        int xIndex = 0;

        for (int t = 0; t < samples.size(); t++) {
            int y =  ((int) ((canvas.getHeight() / 2) + samples.get(t).first*yScale));
            canvas.drawLine(oldX, oldY, xIndex, y, mPaint);
            oldY = y;
            y = ((int) ((canvas.getHeight() / 2) + samples.get(t).second*yScale));

            canvas.drawLine(xIndex, oldY, xIndex, y, mPaint);
            //System.out.println("at x: " + oldX + ", y: " + oldY + "to X: " + xIndex + ", Y: " + y);

            oldX = xIndex;
            xIndex++;
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
    public void setUserScale(float userScale){
        this.userScale = userScale;
    }
    public void setXTranslation(float xTranslation){
        this.xTranslation = xTranslation;
    }

}
