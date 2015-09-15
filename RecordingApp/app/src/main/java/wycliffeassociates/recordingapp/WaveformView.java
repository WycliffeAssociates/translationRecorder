package wycliffeassociates.recordingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import wycliffeassociates.recordingapp.model.UIDataManager;

/**
 * Created by sarabiaj on 9/10/2015.
 */
public class WaveformView extends CanvasView {

    private Paint mPaint;
    private byte[] buffer;
    private boolean drawingFromBuffer = false;
    private ArrayList<Pair<Double,Double>> samples;


    public void drawMarker(Canvas canvas){
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.RED);
        canvas.drawLine((canvas.getWidth() / 8), 0, (canvas.getWidth() / 8), canvas.getHeight(), mPaint);
    }

    public WaveformView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    public void setDrawingFromBuffer(boolean c){
        this.drawingFromBuffer = c;
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
                System.out.println("hello I'm about to draw");
                UIDataManager.lock.acquire();
                super.drawWaveform(samples, canvas);
                UIDataManager.lock.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void init(){
        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.DKGRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(1f);
        xScale = 0;
        yScale = 0;
    }

    public void setBuffer(byte[] buffer){
        this.buffer = buffer;
    }

    public void drawBuffer(Canvas canvas, byte[] buffer, int blocksize, int numChannels){
        if (buffer == null || canvas == null) {
            System.out.println("returning");
            return;
        }

        //System.out.println("in drawbuffer");
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

    public void setWaveformDataForPlayback(ArrayList<Pair<Double, Double>> samples){
        this.samples = samples;
    }
}
