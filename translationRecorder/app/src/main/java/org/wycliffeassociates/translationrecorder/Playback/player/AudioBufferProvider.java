package org.wycliffeassociates.translationrecorder.Playback.player;

import org.wycliffeassociates.translationrecorder.Playback.Editing.CutOp;
import com.door43.tools.reporting.Logger;

import java.nio.ShortBuffer;

/**
 * Created by sarabiaj on 10/27/2016.
 */

class AudioBufferProvider implements BufferPlayer.BufferProvider {

    ShortBuffer mAudio;
    CutOp mCutOp;
    private int SAMPLERATE = 44100;
    private int mLocationAtLastRequest;
    private int mStartPosition = 0;
    private int mMark = 0;

    AudioBufferProvider(ShortBuffer audio, CutOp cutOp){
        //audio is written in little endian, 16 bit PCM. Read as shorts therefore to comply with
        //Android's AudioTrack Spec
        mCutOp = cutOp;
        mAudio = audio;
        mAudio.position(0);
    }

    synchronized void reset(){
        mAudio.position(mMark);
        mStartPosition = mMark;
    }

    //Keep a variable for mark rather than use the Buffer api- a call to position
    synchronized void mark(int position){
        mMark = position;
    }

    /**
     * Clears the mark by setting it to zero and resuming the position
     */
    synchronized void clearMark(){
       mMark = 0;
    }

    synchronized void clearLimit(){
        mAudio.limit(mAudio.capacity());
    }

    public void onPauseAfterPlayingXSamples(int pausedHeadPosition){
        int samplesPlayed = pausedHeadPosition;
        mAudio.position(mStartPosition);
        short[] skip = new short[samplesPlayed];
        get(skip);
        if(mAudio.position() == mAudio.limit()){
            reset();
            Logger.e(this.toString(), "Paused right at the limit");
        }
        mStartPosition = mAudio.position();
    }

    int getSizeOfNextSession(){
        int size = mCutOp.absoluteLocToRelative(mAudio.limit(), false) - mCutOp.absoluteLocToRelative(mAudio.position(), false);
        return size;
    }

    @Override
    public int onBufferRequested(short[] shorts) {
        mLocationAtLastRequest = mAudio.position();
        return get(shorts);
    }

    private int get(short[] shorts){
        //System.out.println("Requesting " + shorts.length + " shorts at position " + mAudio.position());
        int shortsWritten = 0;
        if(mCutOp.cutExistsInRange(mAudio.position(), shorts.length)){
            shortsWritten = getWithSkips(shorts);
        } else {
            shortsWritten = getWithoutSkips(shorts);
        }
        if(shortsWritten < shorts.length){
            for(int i = shortsWritten; i < shorts.length; i++){
                shorts[i] = 0;
            }
        }
        return shortsWritten;
    }

    private int getWithoutSkips(short[] shorts){
        int size = shorts.length;
        int shortsWritten = 0;
        boolean brokeEarly = false;
        for(int i = 0; i < size; i++){
            if(!mAudio.hasRemaining()){
                brokeEarly = true;
                shortsWritten = i;
                break;
            }
            shorts[i] = mAudio.get();
        }
        if(brokeEarly){
            return shortsWritten;
        } else {
            return size;
        }
    }

    private int getWithSkips(short[] shorts){
        int size = shorts.length;
        int skip = 0;
        int end = 0;
        boolean brokeEarly = false;
        for(int i = 0; i < size; i++){
            if(!mAudio.hasRemaining()){
                brokeEarly = true;
                end = i;
                break;
            }
            skip = mCutOp.skip(mAudio.position());
            if(skip != -1){
                //Logger.i(this.toString(), "Location is " + getLocationMs() + "position is " + mAudio.position());
                int start = skip;
                //make sure the playback start is within the bounds of the file's capacity
                start = Math.max(Math.min(mAudio.capacity(), start), 0);
                mAudio.position(start);
                //Logger.i(this.toString(), "Location is now " + getLocationMs() + "position is " + mAudio.position());
            }
            //check a second time incase there was a skip
            if(!mAudio.hasRemaining()){
                brokeEarly = true;
                end = i;
                break;
            }
            shorts[i] = mAudio.get();
        }
        if(brokeEarly){
            return end;
        } else {
            return shorts.length;
        }
    }

    int getLastRequestedPosition(){
        return mLocationAtLastRequest;
    }

    synchronized void setPosition(int position){
        mAudio.position(position);
        mStartPosition = position;
    }

    int getStartPosition(){
        return mStartPosition;
    }

    int getDuration(){
        return mAudio.capacity();
    }

    synchronized void setLimit(int limit){
        mAudio.limit(limit);
        reset();
    }

    public int getLimit(){
        return mAudio.limit();
    }

    public int getMark(){
        return mMark;
    }
}
