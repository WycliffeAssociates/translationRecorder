package wycliffeassociates.recordingapp.Playback;

import java.nio.ByteBuffer;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;

/**
 * Created by sarabiaj on 10/27/2016.
 */

public class AudioBufferProvider implements WavPlayer.BufferProvider {

    ByteBuffer mAudio;
    CutOp mCutOp;
    private int SAMPLERATE = 44100;

    public AudioBufferProvider(ByteBuffer audio, CutOp cutOp){
        mAudio = audio;
        mCutOp = cutOp;
    }

    @Override
    public void requestBuffer(byte[] bytes) {
        get(bytes);
    }

    private void get(byte[] bytes){
        if(mCutOp.cutExistsInRange(mAudio.position(), bytes.length)){
            getWithSkips(bytes);
        } else {
            getWithoutSkips(bytes);
        }
    }

    private void getWithoutSkips(byte[] bytes){
        int size = bytes.length;
        int end = 0;
        boolean brokeEarly = false;
        for(int i = 0; i < size; i++){
            if(!mAudio.hasRemaining()){
                brokeEarly = true;
                end = i;
                break;
            }
            bytes[i] = mAudio.get();
        }
        if(brokeEarly){
            for(int i = end; i < size; i++){
                bytes[i] = 0;
            }
        }
    }

    private void getWithSkips(byte[] bytes){
        int size = bytes.length;
        int skip = 0;
        int end = 0;
        boolean brokeEarly = false;
        for(int i = 0; i < size; i++){
            if(!mAudio.hasRemaining()){
                brokeEarly = true;
                end = i;
                break;
            }
            skip = mCutOp.skip((int)(mAudio.position()/88.2));
            if(skip != -1 && i % 2 == 0){
                //Logger.i(this.toString(), "Location is " + getLocation() + "position is " + mAudio.position());
                int start = (int) (skip * (SAMPLERATE / 500.0));
                //make sure the playback start is within the bounds of the file's capacity
                start = Math.max(Math.min(mAudio.capacity(), start), 0);
                int position = (start % 2 == 0) ? start : start + 1;
                mAudio.position(position);
                //Logger.i(this.toString(), "Location is now " + getLocation() + "position is " + mAudio.position());
            }
            bytes[i] = mAudio.get();
        }
        if(brokeEarly){
            for(int i = end; i < size; i++){
                bytes[i] = 0;
            }
        }
    }
}
