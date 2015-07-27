package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;

public class CanvasView extends View {

    public int width;
    public int height;
    private Bitmap mBitmap;
    //private Canvas mCanvas;
    private Path mPath;
    //private Context context;
    private Paint mPaint;
    private WavFileLoader wavLoader;
    private WavVisualizer wavVis;
    private short[][] audioData;
    private ArrayList<Pair<Double,Double>> samples;
    ScaleGestureDetector SGD;
    private final int SECONDS_ON_SCREEN = 10;

    double xScale;
    double yScale;
    float userScale;
    private float xTranslation;
    private byte[] buffer;
    private int blockSize = 4;
    private int numChannels = 2;
    private boolean recording;


    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
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
        drawBuffer(canvas, buffer, blockSize, numChannels, recording);

    }

    public void setNumChannels(int numChannels) {
        this.numChannels = numChannels;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public void setBuffer(byte[] buffer){
        this.buffer = buffer;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }
    public boolean getRecording() {
        return this.recording;
    }


    public void drawBuffer(Canvas canvas, byte[] buffer, int blocksize, int numChannels, boolean recording){
        if (!recording || buffer == null || canvas == null) {
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
        this.invalidate();
    }
    public void drawWaveform(Canvas canvas){
        canvas.scale(userScale, 1.f);
        canvas.translate(-xTranslation/Math.abs(userScale), 0.f);

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

    public void loadWavFromFile(String path){
        wavLoader = new WavFileLoader(path);
        audioData = wavLoader.getAudioData();
    }

    public void displayWaveform(int seconds){
        wavVis = new WavVisualizer(audioData, wavLoader.getNumChannels());
        xScale = wavVis.getXScaleFactor(this.getWidth(), seconds);
        int inc = wavVis.getIncrement(xScale);
        wavVis.sampleAudio(inc);
        yScale = wavVis.getYScaleFactor(this.getHeight());
        samples = wavVis.getSamples();
        System.out.println("height is " + this.getHeight());
        this.invalidate();
    }

    //NOTE: Only one instance of canvas view can call this; otherwise two threads will be pulling from the same queue!!
    public void listenForRecording(final Activity ctx){
        final CanvasView mainCanvas = this;
        this.setRecording(true);
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isStopped = false;
                boolean isPaused = false;
                while (!isStopped) {
                    try {
                        RecordingMessage message = RecordingQueues.UIQueue.take();
                        isStopped = message.isStopped();
                        isPaused = message.isPaused();
                        if (!isPaused && message.getData() != null) {
                            setBuffer(message.getData());
                            ctx.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mainCanvas.invalidate();
                                }
                            });
                        }
                        if (isStopped) {
                            mainCanvas.setBuffer(null);
                            mainCanvas.setRecording(false);
                            ctx.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mainCanvas.invalidate();
                                }
                            });
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        uiThread.start();
    }


}
