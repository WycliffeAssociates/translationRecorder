package wycliffeassociates.recordingapp.Playback;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sarabiaj on 11/9/2016.
 */

public class MarkerLineLayer extends View {

    public interface MarkerLineDrawDelegator {
        void onDraw(Canvas canvas);
    }

    public static MarkerLineLayer newInstance(Context context, MarkerLineDrawDelegator drawDelegator){
        MarkerLineLayer mll = new MarkerLineLayer(context);
        mll.setMarkerLineDrawDelegator(drawDelegator);
        mll.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return mll;
    }

    MarkerLineDrawDelegator mMarkerLineDrawDelegator;

    private MarkerLineLayer(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mMarkerLineDrawDelegator.onDraw(canvas);
    }

    private void setMarkerLineDrawDelegator(MarkerLineDrawDelegator drawDelegator){
        mMarkerLineDrawDelegator = drawDelegator;
    }
}
