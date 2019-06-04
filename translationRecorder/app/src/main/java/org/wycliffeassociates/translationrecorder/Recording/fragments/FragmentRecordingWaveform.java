package org.wycliffeassociates.translationrecorder.Recording.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wycliffeassociates.translationrecorder.AudioVisualization.WaveformView;
import org.wycliffeassociates.translationrecorder.R;

/**
 * Created by sarabiaj on 2/20/2017.
 */

public class FragmentRecordingWaveform extends Fragment {

    private WaveformView mainWave;
    private Handler mHandler;

    public static FragmentRecordingWaveform newInstance(){
        FragmentRecordingWaveform f = new FragmentRecordingWaveform();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_recording_waveform, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        findViews();
    }

    private void findViews(){
        View view = getView();
        mainWave = (WaveformView) view.findViewById(R.id.main_canvas);
    }

    public void updateWaveform(float[] buffer) {
        mainWave.setBuffer(buffer);
        mainWave.postInvalidate();
    }

    public void setDrawingFromBuffer(boolean drawFromBuffer) {
        mainWave.setDrawingFromBuffer(drawFromBuffer);
    }

    public int getWidth(){
        View view = getView();
        if(view != null) {
            return view.getWidth();
        } else {
            return 0;
        }
    }

    public int getHeight() {
        View view = getView();
        if(view != null) {
            return view.getHeight();
        } else {
            return 0;
        }
    }
}
