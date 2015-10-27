package wycliffeassociates.recordingapp.AudioVisualization;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import wycliffeassociates.recordingapp.AudioInfo;

/**
 * Created by sarabiaj on 9/10/2015.
 */
public class WaveformView extends CanvasView {

    private byte[] buffer;
    private boolean drawingFromBuffer = false;
    private float[] samples;


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
                UIDataManager.lock.acquire();
                drawWaveform(samples, canvas);
                UIDataManager.lock.release();
                drawMarker(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        redraw();
    }


    public void setBuffer(byte[] buffer){
        this.buffer = buffer;
    }

    public void drawBuffer(Canvas canvas, byte[] buffer, int blocksize, int numChannels){
        mPaint.setColor(Color.WHITE);
        if (buffer == null || canvas == null) {
            System.out.println("returning");
            return;
        }
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

    public void setWaveformDataForPlayback(float[] samples){
        this.samples = samples;
    }
}
