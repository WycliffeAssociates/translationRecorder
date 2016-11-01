package wycliffeassociates.recordingapp.Playback.player;

import java.nio.ShortBuffer;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;

/**
 * Created by sarabiaj on 10/28/2016.
 */

/**
 * Controls interaction between BufferPlayer and AudioBufferProvider. The BufferPlayer simply plays audio
 * that is passed to it, and the BufferProvider manages processing audio to get the proper buffer to onPlay
 * based on performing operations on the audio buffer (such as cut).
 */
public class WavPlayer {

    ShortBuffer mAudioBuffer;
    CutOp mOperationStack;
    BufferPlayer mPlayer;
    AudioBufferProvider mBufferProvider;
    WavPlayer.OnCompleteListener mOnCompleteListener;

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
                    mBufferProvider.reset();
                    mOnCompleteListener.onComplete();
                }
            }
        });
    }

    public void play(){
        mPlayer.play(mBufferProvider.getSizeOfNextSession());
    }

    public void pause(){
        mPlayer.pause();
    }

    public void setOnCompleteListener(WavPlayer.OnCompleteListener onCompleteListener){
        mOnCompleteListener = onCompleteListener;
    }

    public int getLocation(){
        return (int)((mBufferProvider.getStartPosition() + mPlayer.getPlaybackHeadPosition()) / 44.1);
    }

    public int getDuration(){
        return (int)(mBufferProvider.getDuration() / 44.1);
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }
}
