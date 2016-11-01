package wycliffeassociates.recordingapp.Playback;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.player.WavPlayer;
import wycliffeassociates.recordingapp.WavFileLoader;
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
    View mPlay, mPause, mSeekForward, mSeekBackward;
    Handler mHandler;
    PlaybackTimer mTimer;

    public AudioVisualController(final View play, final View pause, final View seekForward, final View seekBackward,
                                 final TextView elapsed, final TextView duration, WavFile wav) {

        initPlayer(wav);
        initTimer(elapsed, duration);

        mPause = pause;
        mPlay = play;
        mSeekBackward = seekBackward;
        mSeekForward = seekForward;
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

//        mSeekForward.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                seekForward();
//            }
//        });
//
//        mSeekBackward.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                seekBackward();
//            }
//        });

        swapViews(new View[]{mPlay}, new View[]{mPause});
    }

    private void initPlayer(WavFile wav) {
        WavFileLoader loader = new WavFileLoader(wav);
        mAudio = loader.getMappedAudioFile();
        ShortBuffer mAudioShort = mAudio.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        mPlayer = new WavPlayer(mAudioShort, mCutOp);
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
                    mTimer.setElapsed(location);
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

    public void onPause() {
        swapViews(new View[]{mPlay}, new View[]{mPause});
        mPlayer.pause();
        //playing = false;
    }

//    public void seekForward(){
//        mPlayer.seekToEnd();
//    }
//
//    public void seekBackward(){
//        mPlayer.seekToStart();
//    }

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
