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


    public MinimapView(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(initialized){
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            minimapMarker(canvas);
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
