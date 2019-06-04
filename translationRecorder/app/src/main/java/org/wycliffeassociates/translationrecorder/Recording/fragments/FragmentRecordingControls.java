package org.wycliffeassociates.translationrecorder.Recording.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.wycliffeassociates.translationrecorder.AudioVisualization.RecordingTimer;
import org.wycliffeassociates.translationrecorder.R;
import com.door43.tools.reporting.Logger;

/**
 * Created by sarabiaj on 2/20/2017.
 */

public class FragmentRecordingControls extends Fragment {


    public enum Mode {
        RECORDING_MODE,
        INSERT_MODE;
    }

    private RelativeLayout mToolBar;
    private TextView mTimerView;
    RecordingTimer timer;
    Handler mHandler;
    RecordingControlCallback mRecordingControlCallback;
    private boolean isRecording = false;
    private boolean isPausedRecording = false;
    private Mode mMode;

    public interface RecordingControlCallback {
        void onStartRecording();
        void onPauseRecording();
        void onStopRecording();
    }

    public static FragmentRecordingControls newInstance(Mode mode){
        FragmentRecordingControls f = new FragmentRecordingControls();
        f.setMode(mode);
        return f;
    }

    private void setMode(Mode mode){
        mMode = mode;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof RecordingControlCallback) {
            mRecordingControlCallback = (RecordingControlCallback)activity;
        } else {
            throw new RuntimeException("Attempted to attach to an activity" +
                    " that does not implement RecordingControlCallback");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecordingControlCallback = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_recording_controls, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        findViews();
        timer = new RecordingTimer();
        if(mMode == Mode.INSERT_MODE) {
            mToolBar.setBackgroundColor(getResources().getColor(R.color.secondary));
        }
    }

    private void findViews(){
        View view = getView();
        mTimerView = (TextView) view.findViewById(R.id.timer_view);
        mToolBar = (RelativeLayout) view.findViewById(R.id.toolbar);
        view.findViewById(R.id.btnRecording).setOnClickListener(btnClick);
        view.findViewById(R.id.btnStop).setOnClickListener(btnClick);
        view.findViewById(R.id.btnPauseRecording).setOnClickListener(btnClick);
    }

    public void startTimer(){
        timer.startTimer();
    }

    public void pauseTimer(){
        timer.pause();
    }

    public void resumeTimer(){
        timer.resume();
    }

    public void updateTime() {
        long t = timer.getTimeElapsed();
        final String time = String.format("%02d:%02d:%02d", t / 3600000, (t / 60000) % 60, (t / 1000) % 60);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTimerView.setText(time);
                mTimerView.invalidate();
            }
        });
    }

    public void startRecording(){
        isRecording = true;
        if(!isPausedRecording) {
            startTimer();
        } else {
            resumeTimer();
            isPausedRecording = false;
        }
        int toShow[] = {R.id.btnPauseRecording};
        int toHide[] = {R.id.btnRecording, R.id.btnStop};
        swapViews(toShow, toHide);
        mRecordingControlCallback.onStartRecording();
    }

    public void stopRecording(){
        if (isPausedRecording || isRecording) {
            mRecordingControlCallback.onStopRecording();
            isRecording = false;
            isPausedRecording = false;
        }
    }

    public void pauseRecording(){
        isPausedRecording = true;
        isRecording = false;
        pauseTimer();
        int toShow[] = {R.id.btnRecording, R.id.btnStop};
        int toHide[] = {R.id.btnPauseRecording};
        swapViews(toShow, toHide);
        mRecordingControlCallback.onPauseRecording();
    }

    public void swapViews(int[] toShow, int[] toHide) {
        for (int v : toShow) {
            View view = getView().findViewById(v);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }
        for (int v : toHide) {
            View view = getView().findViewById(v);
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnRecording: {
                    Logger.w(this.toString(), "User pressed Record");
                    startRecording();
                    break;
                }
                case R.id.btnStop: {
                    Logger.w(this.toString(), "User pressed Stop");
                    stopRecording();
                    break;
                }
                case R.id.btnPauseRecording: {
                    Logger.w(this.toString(), "User pressed Pause");
                    pauseRecording();
                    break;
                }
            }
        }
    };
}
