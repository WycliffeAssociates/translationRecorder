package org.wycliffeassociates.translationrecorder.Playback.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Parrot on 11/22/16.
 */

public class MinimapLayer extends View {

    private Bitmap mBitmap = null;
    private Paint mPaint = new Paint();
    private MinimapDrawDelegator mMinimapDrawDelegator;

    public interface MinimapDrawDelegator {
        boolean onDelegateMinimapDraw(Canvas canvas, Paint paint);
    }

    public static MinimapLayer newInstance(Context context, MinimapDrawDelegator drawDelegator) {
        MinimapLayer ml = new MinimapLayer(context);
        ml.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        ml.setMinimapDrawDelegator(drawDelegator);
        return ml;
    }

    public MinimapLayer(Context context) {
        super(context);

        int dpSize =  1;
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);

        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
    }

    public void setMinimapDrawDelegator(MinimapDrawDelegator minimapDrawDelegator) {
        mMinimapDrawDelegator = minimapDrawDelegator;
    }

    //synchronized so as not to null out the minimap during a draw
    public synchronized void invalidateMinimap(){
        mBitmap = null;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        } else {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(mBitmap);
            Drawable background = getBackground();
            if (background != null) {
                background.draw(c);
            } else {
                c.drawColor(Color.TRANSPARENT);
            }
            if (mMinimapDrawDelegator.onDelegateMinimapDraw(c, mPaint)) {
                setBackground(background);
            }
        }
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }
}


