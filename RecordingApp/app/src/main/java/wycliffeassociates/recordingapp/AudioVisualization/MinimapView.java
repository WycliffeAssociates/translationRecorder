package wycliffeassociates.recordingapp.AudioVisualization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created by sarabiaj on 9/10/2015.
 */
public class MinimapView extends CanvasView {

    private Bitmap mBitmap;
    private float miniMarkerLoc;
    private Canvas mCanvas = null;
    private Drawable background;
    private boolean initialized = false;
    private boolean playSelectedSection;
    private int startOfPlaybackSection;
    private int endOfPlaybackSection;
    private int audioLength = 0;
    private double secondsPerPixel = 0;
    private double timecodeInterval = 1.0;

    public MinimapView(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(initialized){
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            minimapMarker(canvas);
            drawTimeCode(canvas);
            if(playSelectedSection){
                drawPlaybackSection(canvas, startOfPlaybackSection, endOfPlaybackSection);
            }
        }
    }

    public void setPlaySelectedSection(boolean x){
        playSelectedSection = x;
    }

    public void setStartOfPlaybackSection(int x){
        startOfPlaybackSection = x;
    }

    public void setEndOfPlaybackSection(int x){
        endOfPlaybackSection = x;
    }

    public void setMiniMarkerLoc(float miniMarkerLoc) {
        this.miniMarkerLoc = miniMarkerLoc;
        this.postInvalidate();
    }

    private void computeTimecodeInterval(){
        timecodeInterval = 1.0;
        if(timecodeInterval / secondsPerPixel >= 50){
            return;
        }
        else{
            timecodeInterval = 0.d;
        }
        while(timecodeInterval / secondsPerPixel < 50){
            timecodeInterval += 5.0;
        }
    }

    public void setAudioLength(int length){
        //System.out.println("length is " + length);
        this.audioLength = (int)(length/1000.0);
        this.secondsPerPixel = audioLength / (double)getWidth();
        computeTimecodeInterval();
    }

    public void drawTimeCode(Canvas canvas){
        //System.out.println("secondsPerPixel is " + secondsPerPixel + " interval is " + timecodeInterval);
        float mDensity = 2.0f;
        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(18.f);
        int i = 0;
        double fractionalSecs = secondsPerPixel;
        int integerTimecode = (int) (fractionalSecs / timecodeInterval);
        while (i < getWidth()){
            mPaint.setColor(Color.GREEN);

            i++;
            fractionalSecs += secondsPerPixel;
            int integerSecs = (int) fractionalSecs;

            int integerTimecodeNew = (int) (fractionalSecs /
                    timecodeInterval);
            if (integerTimecodeNew != integerTimecode) {
                integerTimecode = integerTimecodeNew;

                //System.out.println("integer is " + integerSecs);
                // Turn, e.g. 67 seconds into "1:07"
                String timecodeMinutes = "" + (integerSecs / 60);
                String timecodeSeconds = "" + (integerSecs % 60);
                if ((integerSecs % 60) < 10) {
                    timecodeSeconds = "0" + timecodeSeconds;
                }
                String timecodeStr = timecodeMinutes + ":" + timecodeSeconds;
                float offset = (float) (
                        0.5 * mPaint.measureText(timecodeStr));
                canvas.drawText(timecodeStr,
                        i - offset,
                        (int)(12 * mDensity),
                        mPaint);
                mPaint.setColor(Color.GRAY);
                canvas.drawLine(i, 0.f, i, getHeight(), mPaint);
            }
        }

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

    public void drawPlaybackSection(Canvas c, int start, int end){
        mPaint.setColor(Color.BLUE);
        c.drawLine(start, 0, start, mCanvas.getHeight(), mPaint);
        mPaint.setColor(Color.RED);
        c.drawLine(end, 0, end, mCanvas.getHeight(), mPaint);
    }

    public void minimapMarker(Canvas canvas){
        mPaint.setColor(Color.GREEN);
        canvas.drawLine(miniMarkerLoc, 0, miniMarkerLoc, canvas.getHeight(), mPaint);
    }

}
