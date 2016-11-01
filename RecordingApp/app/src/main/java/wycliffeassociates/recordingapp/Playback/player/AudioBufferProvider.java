package wycliffeassociates.recordingapp.Playback.player;

import java.nio.ShortBuffer;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by sarabiaj on 10/27/2016.
 */

public class AudioBufferProvider implements BufferPlayer.BufferProvider {

    ShortBuffer mAudio;
    CutOp mCutOp;
    private int SAMPLERATE = 44100;
    private int mLocationAtLastRequest;
    private int mStartPosition = 0;

    public AudioBufferProvider(ShortBuffer audio, CutOp cutOp){
        //audio is written in little endian, 16 bit PCM. Read as shorts therefore to comply with
        //Android's AudioTrack Spec
        mCutOp = cutOp;
        mAudio = audio;
        mAudio.position(0);
        mAudio.mark();
    }

    void reset(){
        mAudio.reset();
    }

    void reset(int position){
        mAudio.position(position);
        mAudio.reset();
    }

    public void resumeAt(int pausedHeadPosition){
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

    public int getSizeOfNextSession(){
        int size = mAudio.limit() - mAudio.position();
        return size;
    }

    @Override
    public int requestBuffer(short[] shorts) {
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
            skip = mCutOp.skip((int)(mAudio.position()/44.1));
            if(skip != -1){
                //Logger.i(this.toString(), "Location is " + getLocation() + "position is " + mAudio.position());
                int start = (int) (skip * (SAMPLERATE / 1000.0));
                //make sure the playback start is within the bounds of the file's capacity
                start = Math.max(Math.min(mAudio.capacity(), start), 0);
                mAudio.position(start);
                //Logger.i(this.toString(), "Location is now " + getLocation() + "position is " + mAudio.position());
            }
            shorts[i] = mAudio.get();
        }
        if(brokeEarly){
            return end;
        } else {
            return shorts.length;
        }
    }

    public int getPosition(){
        return mLocationAtLastRequest;
    }

    public void setPosition(int position){
        mAudio.position(position);
        mStartPosition = position;
    }

    public int getStartPosition(){
        return mStartPosition;
    }

    public int getDuration(){
        return mAudio.capacity();
    }

    public void setLimit(int limit){
        mAudio.limit(limit);
    }
}
