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
    View mPlay, mPause, mSeekForward, mSeekBackward, mDropStartMarker, mDropEndMarker, mClear;
    Handler mHandler;
    PlaybackTimer mTimer;
    private List<WavCue> mCues;

    public AudioVisualController(final View play, final View pause, final View seekForward, final View seekBackward,
                                 final TextView elapsed, final TextView duration,
                                 final View dropStartMarker, final View dropEndMarker, final View clear, WavFile wav) {

        wav.addMarker("Test 1", 44100);
        wav.addMarker("Test 2", 88200);
        wav.addMarker("Test 3", 132300);

        initPlayer(wav);
        initTimer(elapsed, duration);

        mPause = pause;
        mPlay = play;
        mSeekBackward = seekBackward;
        mSeekForward = seekForward;
        mDropStartMarker = dropStartMarker;
        mDropEndMarker = dropEndMarker;
        mClear = clear;
        mTimer = new PlaybackTimer(elapsed, duration);
        mHandler = new Handler(Looper.getMainLooper());

        mPlayer.setOnCompleteListener(new WavPlayer.OnCompleteListener() {
            @Override
            public void onComplete() {
                swapViews(new View[]{mPlay}, new View[]{mPause});
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay();
            }
        });

        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();
            }
        });

        mSeekForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekNext();
            }
        });

        mSeekBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekPrevious();
            }
        });

        mDropStartMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setLoopStart(mPlayer.getLocation());
                swapViews(new View[]{mDropEndMarker}, new View[]{mDropStartMarker});
            }
        });

        mDropEndMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setLoopEnd(mPlayer.getLocation());
                swapViews(new View[]{mClear}, new View[]{mDropEndMarker});
            }
        });

        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.clearLoopPoints();
                swapViews(new View[]{mDropStartMarker}, new View[]{mClear});
            }
        });

        swapViews(new View[]{mPlay}, new View[]{mPause});
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
        swapViews(new View[]{mPause}, new View[]{mPlay});
        mPlayer.play();
        Thread playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int location = mPlayer.getLocation();
                while (mPlayer.isPlaying()) {
                    location = mPlayer.getLocation();
                    updateElapsedTime(location);
                    //getLocation();
                    //draw();
                    //             System.out.println(mPlayer.getLocation());
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
        swapViews(new View[]{mPlay}, new View[]{mPause});
        mPlayer.pause();
        //playing = false;
    }

    public void seekNext(){
        mPlayer.seekNext();
        updateElapsedTime(mPlayer.getLocation());
    }

    public void seekPrevious(){
        mPlayer.seekPrevious();
        updateElapsedTime(mPlayer.getLocation());
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
