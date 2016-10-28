package wycliffeassociates.recordingapp.Playback;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.player.WavPlayer;
import wycliffeassociates.recordingapp.WavFileLoader;
import wycliffeassociates.recordingapp.wav.WavFile;

/**
 * Created by sarabiaj on 10/27/2016.
 */

public class AudioController {

    private volatile boolean playing = false;
    WavPlayer mPlayer;
    MappedByteBuffer mAudio;
    CutOp mCutOp = new CutOp();
    View mPlay, mPause, mSeekForward, mSeekBackward;
    Handler mHandler;

    public AudioController(final View play, final View pause, final View seekForward, final View seekBackward,
                           WavFile wav){
        WavFileLoader loader = new WavFileLoader(wav);
        mAudio = loader.getMappedAudioFile();
        ShortBuffer mAudioShort = mAudio.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        mAudioShort.position(0);
        mAudioShort.mark();
        mPlayer = new WavPlayer(mAudioShort, mCutOp);
        mPause = pause; mPlay = play; mSeekBackward = seekBackward; mSeekForward = seekForward;

        mHandler = new Handler(Looper.getMainLooper());

        mPlayer.setOnCompleteListener(new WavPlayer.OnCompleteListener(){
            @Override
            public void onComplete(){
                swapViews(new View[]{mPlay}, new View[]{mPause});
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
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

    public void play(){
        swapViews(new View[]{mPause}, new View[]{mPlay});
        Thread playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mPlayer.play();
                while(mPlayer.isPlaying()){
                    //getLocation();
                    //draw();
                    System.out.println(mPlayer.getLocation());
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

    public void pause(){
        swapViews(new View[]{mPlay}, new View[]{mPause});
        mPlayer.pause();
        playing = false;
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
