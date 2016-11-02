package wycliffeassociates.recordingapp.Playback;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.player.WavPlayer;
import wycliffeassociates.recordingapp.WavFileLoader;
import wycliffeassociates.recordingapp.wav.WavCue;
import wycliffeassociates.recordingapp.wav.WavFile;
import wycliffeassociates.recordingapp.widgets.PlaybackTimer;

/**
 * Created by sarabiaj on 10/27/2016.
 */

public class AudioVisualController {

    private volatile boolean playing = false;
    WavPlayer mPlayer;
    MappedByteBuffer mAudio;
    CutOp mCutOp = new CutOp();
    View mPlayBtn, mPauseBtn, mSeekForwardBtn, mSeekBackwardBtn, mDropStartMarkerBtn,
            mDropEndMarkerBtn, mClearBtn, mCutBtn, mUndoBtn;
    Handler mHandler;
    PlaybackTimer mTimer;
    private List<WavCue> mCues;

    public AudioVisualController(final View playBtn, final View pauseBtn, final View seekForwardBtn, final View seekBackwardBtn,
                                 final TextView elapsedBtn, final TextView durationBtn,
                                 final View dropStartMarkerBtn, final View dropEndMarkerBtn, final View clearBtn,
                                 final View cutBtn, final View undoBtn, WavFile wav) {

        wav.addMarker("Test 1", 44100);
        wav.addMarker("Test 2", 88200);
        wav.addMarker("Test 3", 132300);

        initPlayer(wav);
        initTimer(elapsedBtn, durationBtn);

        mPauseBtn = pauseBtn;
        mPlayBtn = playBtn;
        mSeekBackwardBtn = seekBackwardBtn;
        mSeekForwardBtn = seekForwardBtn;
        mDropStartMarkerBtn = dropStartMarkerBtn;
        mDropEndMarkerBtn = dropEndMarkerBtn;
        mClearBtn = clearBtn;
        mCutBtn = cutBtn;
        mUndoBtn = undoBtn;

        mTimer = new PlaybackTimer(elapsedBtn, durationBtn);
        mHandler = new Handler(Looper.getMainLooper());

        mPlayer.setOnCompleteListener(new WavPlayer.OnCompleteListener() {
            @Override
            public void onComplete() {
                swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
            }
        });

        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay();
            }
        });

        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();
            }
        });

        mSeekForwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekNext();
            }
        });

        mSeekBackwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekPrevious();
            }
        });

        mDropStartMarkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setLoopStart(mPlayer.getLocationInFrames());
                swapViews(new View[]{mDropEndMarkerBtn}, new View[]{mDropStartMarkerBtn});
            }
        });

        mDropEndMarkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setLoopEnd(mPlayer.getLocationInFrames());
                swapViews(new View[]{mClearBtn, mCutBtn}, new View[]{mDropEndMarkerBtn});
            }
        });

        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.clearLoopPoints();
                swapViews(new View[]{mDropStartMarkerBtn}, new View[]{mClearBtn, mCutBtn});
            }
        });

        mCutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCutOp.cut(mPlayer.getLoopStart(), mPlayer.getLoopEnd());
                mPlayer.clearLoopPoints();
                swapViews(new View[]{mUndoBtn}, new View[]{mCutBtn});
            }
        });

        mUndoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCutOp.undo();
            }
        });

        swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
    }

    private void initPlayer(WavFile wav) {
        WavFileLoader loader = new WavFileLoader(wav);

        mCues = wav.getMetadata().getCuePoints();
        if (mCues != null) {
            sortCues(mCues);
        }
        mAudio = loader.getMappedAudioFile();
        ShortBuffer mAudioShort = mAudio.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        mPlayer = new WavPlayer(mAudioShort, mCutOp, mCues);
    }

    private void sortCues(List<WavCue> cues) {
        Collections.sort(cues, new Comparator<WavCue>() {
            @Override
            public int compare(WavCue lhs, WavCue rhs) {
                return Integer.compare(lhs.getLocation(), rhs.getLocation());
            }
        });
    }

    private void initTimer(final TextView elapsed, final TextView duration) {
        mTimer = new PlaybackTimer(elapsed, duration);
        mTimer.setElapsed(0);
        mTimer.setDuration(mPlayer.getDuration());
    }

    public void onPlay() {
        swapViews(new View[]{mPauseBtn}, new View[]{mPlayBtn});
        mPlayer.play();
        Thread playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int location = mPlayer.getLocationMs();
                while (mPlayer.isPlaying()) {
                    location = mPlayer.getLocationMs();
                    updateElapsedTime(location);
                    //getLocationMs();
                    //draw();
                    //             System.out.println(mPlayer.getLocationMs());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        playbackThread.start();
    }

    private void updateElapsedTime(int location) {
        mTimer.setElapsed(location);
    }

    public void onPause() {
        mPlayer.pause();
        swapViews(new View[]{mPlayBtn}, new View[]{mPauseBtn});
        //playing = false;
    }

    public void seekNext(){
        mPlayer.seekNext();
        updateElapsedTime(mPlayer.getLocationMs());
    }

    public void seekPrevious(){
        mPlayer.seekPrevious();
        updateElapsedTime(mPlayer.getLocationMs());
    }

    public void swapViews(final View[] toShow, final View[] toHide) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (View v : toShow) {
                    if (v != null) {
                        v.setVisibility(View.VISIBLE);
                    }
                }
                for (View v : toHide) {
                    if (v != null) {
                        v.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }


}
