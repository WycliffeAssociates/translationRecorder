package wycliffeassociates.recordingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by sarabiaj on 9/10/2015.
 */
public class MinimapView extends CanvasView {

    private Bitmap mBitmap;
    private float miniMarkerLoc;
    private Canvas mCanvas = null;
    private Drawable background;
    private boolean initialized = false;


    public MinimapView(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(initialized){
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            minimapMaker(canvas);
        }


    }

    public void setMiniMarkerLoc(float miniMarkerLoc) {
        this.miniMarkerLoc = miniMarkerLoc;
        this.postInvalidate();
    }

    public void init(float[] samples){
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Runtime.getRuntime().freeMemory();
        System.out.println("Saving minimap to BMP...");
        System.out.println("Created a BMP...");
        mCanvas = new Canvas(mBitmap);
        Drawable background = getBackground();
        if(background != null){
            background.draw(mCanvas);
        }
        else
            mCanvas.drawColor(Color.TRANSPARENT);
        drawWaveform(samples, mCanvas);
        setBackground(background);
        initialized = true;
        this.invalidate();
    }

  /*public void getMinimap(){
        //samples = wavLoader.getMinimap(this.getWidth());
        xScale = wavVis.getXScaleFactor(this.getWidth(), 0);
        //yScale = wavVis.getYScaleFactor(this.getHeight());
        wavLoader = null;
        wavVis = null;
        Runtime.getRuntime().freeMemory();
        System.out.println("Saving minimap to BMP...");
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        System.out.println("Created a BMP...");
        mCanvas = new Canvas(mBitmap);
        Drawable background = getBackground();
        if(background!= null){
            background.draw(mCanvas);
        }
        else
            mCanvas.drawColor(Color.TRANSPARENT);
        draw(mCanvas);
        this.invalidate();
    }*/

    public void minimapMaker(Canvas canvas){
        mPaint.setColor(Color.GREEN);
        canvas.drawLine(miniMarkerLoc, 0, miniMarkerLoc, canvas.getHeight(), mPaint);
    }

}
