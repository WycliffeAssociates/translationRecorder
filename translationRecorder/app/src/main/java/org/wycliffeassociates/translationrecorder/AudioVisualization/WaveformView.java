package org.wycliffeassociates.translationrecorder.AudioVisualization;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import org.wycliffeassociates.translationrecorder.AudioInfo;

/**
 * A canvas view intended for use as the main waveform
 */
public class WaveformView extends CanvasView {

    private float[] mBuffer;
    private boolean mDrawingFromBuffer = false;
    private float[] mSamples;
    private int mDb = 0;

//    public void drawDbLines(Canvas c) {
//        int db3 = dBLine(23197);
//        int ndb3 = dBLine(-23197);
//        c.drawLine(0, db3, getWidth(), db3, mPaintGrid);
//        c.drawLine(0, ndb3, getWidth(), ndb3, mPaintGrid);
//        c.drawText(Integer.toString(-3), 0, db3, mPaintText);
//        c.drawText(Integer.toString(-3), 0, ndb3, mPaintText);
//
//        int db18 = dBLine(4125);
//        int ndb18 = dBLine(-4125);
//        c.drawLine(0, db18, getWidth(), db18, mPaintGrid);
//        c.drawLine(0, ndb18, getWidth(), ndb18, mPaintGrid);
//        c.drawText(Integer.toString(-18), 0, db18, mPaintText);
//        c.drawText(Integer.toString(-18), 0, ndb18, mPaintText);
//    }


    private int dBLine(int val) {
        return (int) (val / (double) AudioInfo.AMPLITUDE_RANGE * getHeight() / 2 + getHeight() / 2);
    }

    /**
     * Constructs a WaveformView (which is a canvas view, meant for displaying the main waveform)
     * Sets up gesture detectors for interacting with the main waveform
     *
     * @param c     is the context of the activity running
     * @param attrs attributes to be passed to the super class
     */
    public WaveformView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    /**
     * Sets the state of the view to draw waveforms from buffers that will be passed in
     * This implies that the app is recording
     *
     * @param b True to draw from a buffer (from the mic), False to draw samples of the Waveform
     */
    public void setDrawingFromBuffer(boolean b) {
        this.mDrawingFromBuffer = b;
    }


    /**
     * Main draw method that is called when the view is invalidated.
     *
     * @param canvas The canvas which can be drawn on. Provided by Android as onDrawMarkers is not
     *               called explicitly.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //DrawingFromBuffers will draw data received from the microphone during recording
        if (mDrawingFromBuffer) {
            //drawDbLines(canvas);
            drawBuffer(canvas, mBuffer, AudioInfo.BLOCKSIZE);

            //Samples is a sampled section of the waveform extracted at mTimeToDraw
        } else if (mSamples != null) {
            //drawDbLines(canvas);
            try {
                drawWaveform(mSamples, canvas);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        //Creates a drawing loop; redraws only will occur if audio is playing
        //redraw();
    }

    /**
     * Sets a byte buffer to be drawn to the screen
     *
     * @param buffer a byte buffer containing 16 bit pcm data
     */
    public synchronized void setBuffer(float[] buffer) {
        mBuffer = buffer;
    }

    public void setDb(int db) {
        mDb = db;
    }

    //TODO: create a separate paint object for drawing the waveform

    /**
     * Draws a waveform from the buffer produced while recording
     *
     * @param canvas    the canvas to draw to
     * @param buffer    the byte buffer containing 16 bit pcm data to draw
     * @param blocksize the size of a block of audio data; 2 for 16 bit mono PCM
     */
    public synchronized void drawBuffer(Canvas canvas, float[] buffer, int blocksize) {
//        if (buffer == null || canvas == null) {
//            return;
//        }
//        //convert PCM data in a byte array to a short array
//        Short[] temp = new Short[buffer.length / blocksize];
//        int index = 0;
//        for (int i = 0; i < buffer.length; i += blocksize) {
//            byte low = buffer[i];
//            byte hi = buffer[i + 1];
//            //PCM data is stored little endian
//            temp[index] = (short) (((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
//            index++;
//        }
//        int width = canvas.getWidth();
//        int height = canvas.getHeight();
//        double xScale = width / (index * .999);
//        double yScale = height / 65536.0;
//        for (int i = 0; i < temp.length - 1; i++) {
////            canvas.drawLine((int)(xScale*i), (int)((yScale*temp[i])+ height/2),
////                    (int)(xScale*(i+1)), (int)((yScale*temp[i+1]) + height/2), mPaint);
//            canvas.drawLine((int) (xScale * i), (int) U.getValueForScreen(temp[i], height),
//                    (int) (xScale * (i + 1)), (int) U.getValueForScreen(temp[i + 1], height), mPaintWaveform);
//
//        }
//        this.postInvalidate();

        if (buffer == null || canvas == null) {
            return;
        }
//        int width = canvas.getWidth();
//        int height = canvas.getHeight();
//        double xScale = width / (index * .999);
//        double yScale = height / 65536.0;
//        for (int i = 0; i < temp.length - 1; i++) {
////            canvas.drawLine((int)(xScale*i), (int)((yScale*temp[i])+ height/2),
////                    (int)(xScale*(i+1)), (int)((yScale*temp[i+1]) + height/2), mPaint);
//            canvas.drawLine((int) (xScale * i), (int) U.getValueForScreen(temp[i], height),
//                    (int) (xScale * (i + 1)), (int) U.getValueForScreen(temp[i + 1], height), mPaintWaveform);
//
//        }
        canvas.drawLines(buffer, mPaintWaveform);
        this.postInvalidate();
    }
}
