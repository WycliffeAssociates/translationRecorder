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
    private Paint mPaint;
    private Canvas mCanvas = null;
    private Drawable background;




    public MinimapView(Context c, AttributeSet attrs) {
        super(c, attrs);
        //init();
        //mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        //canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        //minimapMaker(canvas);
    }

    public void setMiniMarkerLoc(float miniMarkerLoc) {
        this.miniMarkerLoc = miniMarkerLoc;
    }

    public void init(ArrayList<Pair<Double,Double>> samples, WavVisualizer wavVis){
//        xScale = wavVis.getXScaleFactor(this.getWidth(), 0);
//        yScale = wavVis.getYScaleFactor(this.getHeight());
//        Runtime.getRuntime().freeMemory();
//        System.out.println("Saving minimap to BMP...");
//        System.out.println("Created a BMP...");
//        mCanvas = new Canvas(mBitmap);
//        Drawable background = getBackground();
//        if(background != null){
//            background.draw(mCanvas);
//        }
//        else
//            mCanvas.drawColor(Color.TRANSPARENT);
//        drawWaveform(samples, mCanvas);
//        setBackground(background);
//        this.invalidate();
    }



    public void minimapMaker(Canvas canvas){
        mPaint.setColor(Color.GREEN);
        canvas.drawLine(miniMarkerLoc, 0, miniMarkerLoc, canvas.getHeight(), mPaint);
    }

}
