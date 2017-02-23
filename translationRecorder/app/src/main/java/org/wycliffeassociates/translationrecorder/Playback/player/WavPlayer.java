package org.wycliffeassociates.translationrecorder.Playback.player;

import org.wycliffeassociates.translationrecorder.Playback.Editing.CutOp;
import org.wycliffeassociates.translationrecorder.wav.WavCue;

import java.nio.ShortBuffer;
import java.util.List;

/**
 * Created by sarabiaj on 10/28/2016.
 */

/**
 * Controls interaction between BufferPlayer and AudioBufferProvider. The BufferPlayer simply plays audio
 * that is passed to it, and the BufferProvider manages processing audio to get the proper buffer to onPlay
 * based on performing operations on the audio buffer (such as cut).
 */
public class WavPlayer {

    private List<WavCue> mCueList;
    ShortBuffer mAudioBuffer;
    CutOp mOperationStack;
    BufferPlayer mPlayer;
    AudioBufferProvider mBufferProvider;
    WavPlayer.OnCompleteListener mOnCompleteListener;
    private int EPSILON = 200;

    public interface OnCompleteListener{
        void onComplete();
    }

    public WavPlayer(ShortBuffer audioBuffer, CutOp operations, List<WavCue> cueList){
        mOperationStack = operations;
        mAudioBuffer = audioBuffer;
        mBufferProvider = new AudioBufferProvider(mAudioBuffer, mOperationStack);
        mCueList = cueList;
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

    public synchronized void seekNext(){
        int seekLocation = getAbsoluteDurationInFrames();
        int currentLocation = getAbsoluteLocationInFrames();
        if(mCueList != null) {
            int location;
            for(int i = 0; i < mCueList.size(); i++){
                location = mCueList.get(i).getLocation();
                if(currentLocation < location){
                    seekLocation = location;
                    break;
                }
            }
        }
        seekToAbsolute(Math.min(seekLocation, mBufferProvider.getLimit()));
    }

    public synchronized void seekPrevious(){
        int seekLocation = 0;
        int currentLocation = getAbsoluteLocationInFrames();
        if(mCueList != null){
            int location;
            for(int i = mCueList.size()-1; i >= 0; i--){
                location = mCueList.get(i).getLocation();
                //if playing, you won't be able to keep pressing back, it will clamp to the last marker
                if(!isPlaying() && currentLocation > location) {
                    seekLocation = location;
                    break;
                } else if (currentLocation - EPSILON > location) { //epsilon here is to prevent that clamping
                    seekLocation = location;
                    break;
                }
            }
        }
        seekToAbsolute(Math.max(seekLocation, mBufferProvider.getMark()));
    }

    public synchronized void seekToAbsolute(int absoluteFrame){
        if(absoluteFrame > getAbsoluteDurationInFrames() || absoluteFrame < 0){
            return;
        }
        absoluteFrame = Math.max(absoluteFrame, mBufferProvider.getMark());
        absoluteFrame = Math.min(absoluteFrame, mBufferProvider.getLimit());
        boolean wasPlaying = mPlayer.isPlaying();
        pause();
        mBufferProvider.setPosition(absoluteFrame);
        if(wasPlaying){
            play();
        }
    }

    public synchronized void setCueList(List<WavCue> cueList) {
        mCueList = cueList;
    }

    public void play(){
        if(getAbsoluteLocationInFrames() == getLoopEnd()) {
            mBufferProvider.reset();
        }
        mPlayer.play(mBufferProvider.getSizeOfNextSession());
    }

    public void pause(){
        mPlayer.pause();
    }

    public void setOnCompleteListener(WavPlayer.OnCompleteListener onCompleteListener){
        mOnCompleteListener = onCompleteListener;
    }

    public int getAbsoluteLocationMs(){
        return (int)(getAbsoluteLocationInFrames() / 44.1);
    }

    public int getAbsoluteDurationMs(){
        return (int)(mBufferProvider.getDuration() / 44.1);
    }

    public int getAbsoluteLocationInFrames(){
        int relativeLocationOfHead = mOperationStack.absoluteLocToRelative(mBufferProvider.getStartPosition(), false) + mPlayer.getPlaybackHeadPosition();
        int absoluteLocationOfHead = mOperationStack.relativeLocToAbsolute(relativeLocationOfHead, false);
        return absoluteLocationOfHead;
    }

    public int getAbsoluteDurationInFrames(){
        return mBufferProvider.getDuration();
    }

    public int getRelativeLocationMs(){
        return (int) (getRelativeLocationInFrames() / 44.1);
    }

    public int getRelativeDurationMs(){
        return (int)((mBufferProvider.getDuration() - mOperationStack.getSizeFrameCutUncmp()) / 44.1);
    }

    public int getRelativeDurationInFrames() {
        return mBufferProvider.getDuration() - mOperationStack.getSizeFrameCutUncmp();
    }

    public int getRelativeLocationInFrames(){
        return mOperationStack.absoluteLocToRelative(getAbsoluteLocationInFrames(), false);
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public void setLoopStart(int frame){
        if(frame > mBufferProvider.getLimit()) {
            int oldLimit = mBufferProvider.getLimit();
            clearLoopPoints();
            mBufferProvider.mark(oldLimit);
            mBufferProvider.setLimit(frame);
            mBufferProvider.reset();
        } else {
            mBufferProvider.mark(frame);
        }
    }

    public int getLoopStart(){
        return mBufferProvider.getMark();
    }

    public void setLoopEnd(int frame){
        if(frame < mBufferProvider.getMark()) {
            int oldMark = mBufferProvider.getMark();
            clearLoopPoints();
            mBufferProvider.mark(frame);
            mBufferProvider.setLimit(oldMark);
        } else {
            mBufferProvider.setLimit(frame);
            mBufferProvider.reset();
        }
    }

    public int getLoopEnd(){
        return mBufferProvider.getLimit();
    }

    public void clearLoopPoints(){
        mBufferProvider.clearMark();
        mBufferProvider.clearLimit();
    }
}
