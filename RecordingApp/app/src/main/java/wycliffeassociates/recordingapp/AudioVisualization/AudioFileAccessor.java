package wycliffeassociates.recordingapp.AudioVisualization;

import android.util.Log;

import java.nio.MappedByteBuffer;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.WavPlayer;

/**
 * Created by sarabiaj on 1/12/2016.
 */


/**
 * Keywords:
 * Relative - index or time with cuts abstracted away
 * Absolute - index or time with cut data still existing
 */
public class AudioFileAccessor {
    MappedByteBuffer mCompressed;
    MappedByteBuffer mUncompressed;
    CutOp mCut;
    int mWidth;
    int mUncmpToCmp;
    boolean mUseCmp = false;

    public AudioFileAccessor(MappedByteBuffer compressed, MappedByteBuffer uncompressed, CutOp cut){
        mCompressed = compressed;
        mUncompressed = uncompressed;
        mCut = cut;
        mWidth = AudioInfo.SCREEN_WIDTH;
        //increment to write the compressed file. ~44 indices uncompressed = 2 compressed
        mUseCmp = (compressed == null)? false : true;
    }

    public void switchBuffers(boolean cmpReady){
        if(cmpReady){
            mUseCmp = true;
        } else {
            mUseCmp = false;
        }
    }

    public void setCompressed(MappedByteBuffer compressed){
        mCompressed = compressed;
    }

    public byte get(int idx){
        int loc = mCut.relativeLocToAbsolute(idx, mUseCmp);
        byte val;
        if(mUseCmp){
            val = mCompressed.get(loc);
        } else {
            val = mUncompressed.get(loc);
        }
        return val;
    }

    public int size(){
        if(mUseCmp){
            return mCompressed.capacity() - mCut.getSizeCutCmp();
        }
        return mUncompressed.capacity() - mCut.getSizeCutUncmp();
    }

    //can return an invalid index, negative indices useful for how many zeros to add
    //iterates backwards, checking if time hits a skip
    //OPTIMIZE: optimize by subtracting exactly the times needed between this range, rather than for loop
    public int indexAfterSubtractingTime(int timeToSubtractMs, int currentTimeMs, double numSecondsOnScreen){
        int time = currentTimeMs;
        for(int i = 1; i < timeToSubtractMs; i++){
            time--;
            int skip = mCut.skipReverse(time);
            if(skip != Integer.MAX_VALUE){
                time = skip;
            }
        }
        int loc = absoluteIndexFromAbsoluteTime(time, numSecondsOnScreen);
        loc = absoluteIndexToRelative(loc);

        return loc;
    }

    public int indicesInAPixelMinimap(){
        //get the number of milliseconds in a pixel, map it to an absolute index, then convert to relative
        double fileInc = Math.round((AudioInfo.SAMPLERATE * AudioInfo.COMPRESSED_SECONDS_ON_SCREEN) / (double)AudioInfo.SCREEN_WIDTH ) * 2;
        int incUncmp = (int)Math.round(((AudioInfo.SAMPLERATE * WavPlayer.getAdjustedDuration())/(double)1000)/ (double)AudioInfo.SCREEN_WIDTH) * 2;
        int incCmp = (int)Math.round((incUncmp / (double)44)) * 4;
        int increment = (mUseCmp)? incCmp : incUncmp;
        return increment;
    }

    public int absoluteIndexFromAbsoluteTime(int timeMs, double numSecondsOnScreen){
        int idx = (int)Math.round(timeMs / 1000.0 * AudioInfo.SAMPLERATE) * 2;
        if(mUseCmp){

            idx = (int)Math.round(idx/(double)fileIncrement()) * 4;
        }
        return idx;
    }

    public int relativeIndexToAbsolute(int idx){
        return mCut.relativeLocToAbsolute(idx, mUseCmp);
    }

    public int absoluteIndexToRelative(int idx){
        return mCut.absoluteLocToRelative(idx, mUseCmp);
    }

    public static int fileIncrement(){
        return (int)Math.round((AudioInfo.SAMPLERATE * AudioInfo.COMPRESSED_SECONDS_ON_SCREEN) / (double)AudioInfo.SCREEN_WIDTH ) * 2;
    }

    public static int uncompressedIncrement(double numSecondsOnScreen){
        int increment = (int)Math.round(((AudioInfo.SAMPLERATE * WavPlayer.getAdjustedDuration())/(double)1000)/ (double)AudioInfo.SCREEN_WIDTH) * 2;
        increment = (increment % 2 == 0)? increment : increment+1;
        return increment;
    }

    public static int compressedIncrement(double numSecondsOnScreen){
        int increment = (int)Math.round((numSecondsOnScreen / AudioInfo.COMPRESSED_SECONDS_ON_SCREEN)) * 2 * AudioInfo.SIZE_OF_SHORT;
        increment = (increment % 2 == 0)? increment : increment+1;
        return increment;
    }

    public static int getIncrement(double numSecondsOnScreen, boolean useCmp){
        if(useCmp){
            return compressedIncrement(numSecondsOnScreen);
        } else {
            return uncompressedIncrement(numSecondsOnScreen);
        }
    }
}
