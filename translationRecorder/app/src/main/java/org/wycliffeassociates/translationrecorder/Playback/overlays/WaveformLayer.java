package org.wycliffeassociates.translationrecorder.Playback.overlays;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 11/11/2016.
 */

public class WaveformLayer extends View {

    public interface WaveformDrawDelegator{
        void onDrawWaveform(Canvas canvas, Paint paint);
    }

    private Paint mPaint;
    private WaveformDrawDelegator mDrawDelegator;

    public static WaveformLayer newInstance(Context context, WaveformDrawDelegator drawDelegator){
        WaveformLayer wav = new WaveformLayer(context);
        wav.setWaveformDrawDelegator(drawDelegator);
        wav.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return wav;
    }

    public WaveformLayer(Context context) {
        super(context);

        int dpSize =  0;
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.off_white));
        mPaint.setStrokeWidth(strokeWidth);
    }

    private void setWaveformDrawDelegator(WaveformDrawDelegator drawDelegator){
        mDrawDelegator = drawDelegator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawDelegator.onDrawWaveform(canvas, mPaint);
    }
}
