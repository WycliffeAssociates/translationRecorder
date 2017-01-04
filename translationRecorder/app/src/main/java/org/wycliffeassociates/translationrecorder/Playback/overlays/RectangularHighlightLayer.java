package org.wycliffeassociates.translationrecorder.Playback.overlays;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 11/9/2016.
 */

public class RectangularHighlightLayer extends View {

    Paint mPaintHighlight = new Paint();

    public interface HighlightDelegator {
        void onDrawHighlight(Canvas canvas, Paint paint);
    }

    public static RectangularHighlightLayer newInstance(Context context, HighlightDelegator drawDelegator){
        RectangularHighlightLayer mll = new RectangularHighlightLayer(context);
        mll.setHighlightDelegator(drawDelegator);
        mll.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mll.setPaint();
        return mll;
    }

    private void setPaint(){
        mPaintHighlight.setColor(getResources().getColor(R.color.bright_blue));
        mPaintHighlight.setAlpha(50);
        mPaintHighlight.setStyle(Paint.Style.FILL);
    }

    HighlightDelegator mHighlightDelegator;

    private RectangularHighlightLayer(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mHighlightDelegator.onDrawHighlight(canvas, mPaintHighlight);
    }

    private void setHighlightDelegator(HighlightDelegator drawDelegator){
        mHighlightDelegator = drawDelegator;
    }
}
