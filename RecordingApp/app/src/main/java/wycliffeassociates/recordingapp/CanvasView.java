package wycliffeassociates.recordingapp;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

public class CanvasView extends View {

    private boolean hasDrawnOnce;
    public int width;
    public int height;
    private Bitmap mBitmap;
    //private Canvas mCanvas;
    private Path mPath;
    //private Context context;
    private Paint mPaint;
    private WavFileLoader wavLoader;
    private WavVisualizer wavVis;
    //private short[] audioData;

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
    private boolean shouldDrawLine = false;
    private boolean shouldDrawMiniMarker = false;
    private int increment = 1;
    private boolean isMinimap = false;
    private Canvas mCanvas = null;
    private Drawable background;

    public void setMiniMarkerLoc(float miniMarkerLoc) {
        this.miniMarkerLoc = miniMarkerLoc;
    }

    private float miniMarkerLoc;


    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    public CanvasView(Context c, AttributeSet attrs, Bitmap bmp) {
        super(c, attrs);
        init();
        mBitmap = bmp;
        hasDrawnOnce = false;
    }

    private void init(){
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
        //mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
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
        if(shouldDrawLine){
            drawMarker(canvas);
        }
        if(shouldDrawMiniMarker){
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            minimapMaker(canvas);
        }


    }
    public void setIsMinimap(boolean flag){
        this.isMinimap = flag;
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
        if(!isMinimap || !hasDrawnOnce){
            canvas.scale(userScale, 1.f);
            canvas.translate(-xTranslation / Math.abs(userScale), 0.f);

            mPaint.setColor(Color.WHITE);
            if (samples == null) {
                return;
            }
            int oldY =  (canvas.getHeight() / 2);
            int xIndex;
            int oldX;
            if(isMinimap) {
                xIndex = oldX = 0;
            }
            else{
                xIndex = oldX = wavLoader.getSampleStartIndex();
            }

            int numTs = 0;
            for (int t = 0; t < samples.size(); t++) {

                int y =  ((int) ((canvas.getHeight() / 2) + samples.get(t).first*yScale));
                canvas.drawLine(oldX, oldY, xIndex, y, mPaint);
                oldY = y;
                //System.out.println("y is: " + y + " x is: " + xIndex);
                y = ((int) ((canvas.getHeight() / 2) + samples.get(t).second*yScale));

                canvas.drawLine(xIndex, oldY, xIndex, y, mPaint);
                //System.out.println("at x: " + oldX + ", y: " + oldY + "to X: " + xIndex + ", Y: " + y);

                oldX = xIndex;
                xIndex++;

                oldY = y;
                numTs = t;
            }
            System.out.println("number of draws: " + numTs);
            if(isMinimap && !hasDrawnOnce){
                setBackground(background);
                hasDrawnOnce = true;
            }
        }
        else{
            System.out.println("skipped drawing the minimap");
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
        try {
            wavLoader = new WavFileLoader(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        samples = null;
    }

    public void getMinimap(){
        samples = wavLoader.getMinimap(this.getWidth());
        wavVis = new WavVisualizer(samples, wavLoader.getLargest());
        xScale = wavVis.getXScaleFactor(this.getWidth(), 0);
        yScale = wavVis.getYScaleFactor(this.getHeight());
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
    }

    public void resample(int position){
        samples = wavLoader.getAudioWindow(position, 10, increment);
    }

    public void recomputeIncrement(float xScale){
        increment = wavVis.getIncrement(xScale);
    }


    public void displayWaveform(int seconds){
        wavVis = new WavVisualizer(samples, wavLoader.getLargest());
        xScale = wavVis.getXScaleFactor(this.getWidth(), seconds);
        increment = wavVis.getIncrement(xScale);
        System.out.println(increment + "is the increment");
        //wavVis.sampleAudio(inc);
        yScale = wavVis.getYScaleFactor(this.getHeight());
        //samples = wavVis.getSamples();
        System.out.println("height is " + this.getHeight());
        resample(0);
        this.invalidate();
    }

    //NOTE: Only one instance of canvas view can call this; otherwise two threads will be pulling from the same queue!!
    public void listenForRecording(final Activity ctx){
        final CanvasView mainCanvas = this;
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
                            double max = 0;
                            for(int i =0; i < buffer.length; i+=2) {
                                byte low = buffer[i];
                                byte hi = buffer[i + 1];
                                short value = (short)(((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
                                max = (Math.abs(value) > max)? Math.abs(value) : max;
                            }
                            final double db = Math.log10(max / (double) AudioInfo.AMPLITUDE_RANGE)* 10;
                            System.out.println("db is "+db);
                            ctx.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(recording) {
                                        mainCanvas.invalidate();
                                    }
                                    changeVolumeBar(ctx, db);

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
        else if (db < -7 && db > -7.5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_4);
        }
        else if (db < -7.5 && db > -8){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_3);
        }
        else if (db < -8 && db > -8.5){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_2);
        }
        else if (db < -8.5 && db > -12){
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.vol_1);
        }
        else {
            ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.min);
        }
    }

    public void shouldDrawMaker(boolean yes){
        this.shouldDrawLine = yes;
    }
    public void drawMarker(Canvas canvas){
        mPaint.setStrokeWidth(2.f);
        mPaint.setColor(Color.RED);
        canvas.drawLine(((canvas.getWidth()/8) + xTranslation)/userScale, 0, ((canvas.getWidth()/8) + xTranslation)/userScale, canvas.getHeight(), mPaint);
    }
    public void shouldDrawMiniMarker(boolean yes){
        this.shouldDrawMiniMarker = true;
    }
    public void minimapMaker(Canvas canvas){
        mPaint.setColor(Color.GREEN);
        canvas.drawLine(miniMarkerLoc, 0, miniMarkerLoc, canvas.getHeight(), mPaint);
    }

}

