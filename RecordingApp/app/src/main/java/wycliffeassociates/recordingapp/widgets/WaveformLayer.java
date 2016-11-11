package wycliffeassociates.recordingapp.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.FrameLayout;

import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 11/11/2016.
 */

public class WaveformLayer extends FrameLayout {

    public interface WaveformDrawDelegator{
        void onDrawWaveform(Canvas canvas);
    }

    private Paint mPaint;
    private WaveformDrawDelegator mDrawDelegator;

    public static WaveformLayer newInstance(Context context, WaveformDrawDelegator drawDelegator){
        WaveformLayer wav = new WaveformLayer(context);
        wav.setWaveformDrawDelegator(drawDelegator);
        return wav;
    }

    public WaveformLayer(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.off_white));
        mPaint.setStrokeWidth(0);
    }

    private void setWaveformDrawDelegator(WaveformDrawDelegator drawDelegator){
        mDrawDelegator = drawDelegator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
