package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

public abstract class CanvasView extends View {

    protected Paint mPaint;
    int fps = 0;
    protected boolean doneDrawing = false;

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


    public void changeVolumeBar(Activity ctx, double db){
        if(db > -1){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.max);
            System.out.println(db);
        }
        else if (db < -1 && db > -1.5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_16);
        }
        else if (db < -1.5 && db > -2){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_15);
        }
        else if (db < -2 && db > -2.5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_14);
        }
        else if (db < -2.5 && db > -3){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_13);
        }
        else if (db < -3 && db > -3.5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_12);
        }
        else if (db < -3.5 && db > -4){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_11);
        }
        else if (db < -4 && db > -4.5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_10);
        }
        else if (db < -4.5 && db > -5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_9);
        }
        else if (db < -5 && db > -5.5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_8);
        }
        else if (db < -5.5 && db > -6){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_7);
        }
        else if (db < -6 && db > -6.5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_6);
        }
        else if (db < -6.5 && db > -7){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_5);
        }
        else if (db < -7 && db > -9){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_4);
        }
        else if (db < -9 && db > -10){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_3);
        }
        else if (db < -10 && db > -11){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_2);
        }
        else if (db < -11 && db > -12){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_1);
        }
        else {
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.min);
        }
    }




}

