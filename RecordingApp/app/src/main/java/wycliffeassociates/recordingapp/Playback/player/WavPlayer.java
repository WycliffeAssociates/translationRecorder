package wycliffeassociates.recordingapp.Playback.player;

import java.nio.ShortBuffer;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;

/**
 * Created by sarabiaj on 10/28/2016.
 */

public class WavPlayer {

    ShortBuffer mAudioBuffer;
    CutOp mOperationStack;
    BufferPlayer mPlayer;
    AudioBufferProvider mBufferProvider;
    OnCompleteListener mOnCompleteListener;

    public interface OnCompleteListener{
        void onComplete();
    }

    public WavPlayer(ShortBuffer audioBuffer, CutOp operations){
        mOperationStack = operations;
        mAudioBuffer = audioBuffer;
        mBufferProvider = new AudioBufferProvider(mAudioBuffer, mOperationStack);
        mPlayer = new BufferPlayer(mBufferProvider, new BufferPlayer.OnCompleteListener() {
            @Override
            public void onComplete() {
                if(mOnCompleteListener != null){
                    mAudioBuffer.reset();
                    mOnCompleteListener.onComplete();
                }
            }
        });
    }

    public void play(){
        mPlayer.play();
    }

    public void pause(){
        mPlayer.pause();
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener){
        mOnCompleteListener = onCompleteListener;
    }

    public int getLocation(){
        return (int)((mBufferProvider.getStartPosition() + mPlayer.getLocation()) / 44.1);
    }

    public int getDuration(){
        return (int)(mBufferProvider.getDuration() / 44.1);
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }
}
