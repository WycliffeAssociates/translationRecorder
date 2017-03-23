package org.wycliffeassociates.translationrecorder.AudioVisualization;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.AudioInfo;
import org.wycliffeassociates.translationrecorder.Playback.Editing.CutOp;

import java.nio.ShortBuffer;

/**
 * Created by sarabiaj on 1/12/2016.
 */


/**
 * Keywords:
 * Relative - index or time with cuts abstracted away
 * Absolute - index or time with cut data still existing
 */
public class AudioFileAccessor {
    ShortBuffer mCompressed;
    ShortBuffer mUncompressed;
    CutOp mCut;
    int mWidth;
    int mUncmpToCmp;
    boolean mUseCmp = false;

    public AudioFileAccessor(ShortBuffer compressed, ShortBuffer uncompressed, CutOp cut) {
        mCompressed = compressed;
        mUncompressed = uncompressed;
        mCut = cut;
        mWidth = AudioInfo.SCREEN_WIDTH;
        //increment to write the compressed file. ~44 indices uncompressed = 2 compressed
        mUseCmp = (compressed == null) ? false : true;
    }

    public void switchBuffers(boolean cmpReady) {
        if (cmpReady) {
            mUseCmp = true;
        } else {
            mUseCmp = false;
        }
    }

    public void setCompressed(ShortBuffer compressed) {
        mCompressed = compressed;
    }

    //FIXME: should not be returning 0 if out of bounds access, there's a bigger issue here
    public short get(int idx) {
        int loc = mCut.relativeLocToAbsolute(idx, mUseCmp);
        short val;
        if (mUseCmp) {
            if (loc < 0) {
                Logger.e(this.toString(), "ERROR, tried to access a negative location from the compressed buffer!");
                return 0;
            } else if (loc >= mCompressed.capacity()) {
                Logger.e(this.toString(), "ERROR, tried to access a negative location from the compressed buffer!");
                return 0;
            }
            val = mCompressed.get(loc);
        } else {
            if (loc < 0) {
                Logger.e(this.toString(), "ERROR, tried to access a negative location from the compressed buffer!");
                return 0;
            } else if (loc >= mUncompressed.capacity()) {
                Logger.e(this.toString(), "ERROR, tried to access a negative location from the compressed buffer!");
                return 0;
            }
            val = mUncompressed.get(loc);
        }
        return val;
    }

    public int size() {
        if (mUseCmp) {
            return mCompressed.capacity() - mCut.getSizeFrameCutCmp();
        }
        return mUncompressed.capacity() - mCut.getSizeFrameCutUncmp();
    }

    //can return an invalid index, negative indices useful for how many zeros to add
    //iterates backwards, checking if time hits a skip
    //OPTIMIZE: optimize by subtracting exactly the times needed between this range, rather than for loop
    public int[] indexAfterSubtractingTime(int timeToSubtractMs, int currentTimeMs, double numSecondsOnScreen) {
        int time = currentTimeMs;
        for (int i = 1; i < timeToSubtractMs; i++) {
            time--;
            int skip = mCut.skipReverse(mCut.timeToUncmpLoc(time));
            if (skip != Integer.MAX_VALUE) {
                time = (int)(skip / 44.1);
                //System.out.println("here, skip back to " + time);
            }
        }
        int loc = absoluteIndexFromAbsoluteTime(time);
        loc = absoluteIndexToRelative(loc);
        int locAndTime[] = new int[2];
        locAndTime[0] = loc;
        locAndTime[1] = time;
        return locAndTime;
    }

    public int[] indexAfterSubtractingFrame(int framesToSubtract, int currentFrame){
        int frame = currentFrame;
        if(mCut.cutExistsInRange(currentFrame-framesToSubtract-1, framesToSubtract)) {
            for (int i = 1; i < framesToSubtract; i++) {
                frame--;
                int skip = mCut.skipReverse(frame);
                if (skip != Integer.MAX_VALUE) {
                    frame = skip;
                    //System.out.println("here, skip back to " + time);
                }
            }
        } else {
            frame -= framesToSubtract;
        }
        int loc = absoluteIndexFromAbsoluteTime(frame);
        loc = absoluteIndexToRelative(loc);
        int locAndTime[] = new int[2];
        locAndTime[0] = loc;
        locAndTime[1] = frame;
        return locAndTime;    }

    //deprecated
//    public int indicesInAPixelMinimap() {
//        //get the number of milliseconds in a pixel, map it to an absolute index, then convert to relative
//        double fileInc = Math.round((AudioInfo.SAMPLERATE * AudioInfo.COMPRESSED_SECONDS_ON_SCREEN) / (double) AudioInfo.SCREEN_WIDTH) * 2;
//        //int incUncmp = (int) Math.round(((AudioInfo.SAMPLERATE * mManager.getAdjustedDuration()) / (double) 1000) / (double) AudioInfo.SCREEN_WIDTH) * 2;
//        int incCmp = (int) Math.round((incUncmp / (double) 44)) * 4;
//        int increment = (mUseCmp) ? incCmp : incUncmp;
//        return increment;
//    }

    public int absoluteIndexFromAbsoluteTime(int idx) {
//        int seconds = timeMs / 1000;
//        int ms = (timeMs - (seconds * 1000));
//        int tens = ms / 10;
//
//
//        int idx = (AudioInfo.SAMPLERATE * seconds) + (ms * 44) + (tens);
        if (mUseCmp) {
            idx /= 25;
        }
        return idx;
    }

    public int relativeIndexToAbsolute(int idx) {
        return mCut.relativeLocToAbsolute(idx, mUseCmp);
    }

    public int absoluteIndexToRelative(int idx) {
        return mCut.absoluteLocToRelative(idx, mUseCmp);
    }

    public static int fileIncrement() {
        return AudioInfo.COMPRESSION_RATE;
    }

    //used for minimap
    public static double uncompressedIncrement(double adjustedDuration, double screenWidth) {
        double increment = (adjustedDuration / screenWidth);
        //increment = (increment % 2 == 0)? increment : increment+1;
        return increment;
    }

    //used for minimap
    public static double compressedIncrement(double adjustedDuration, double screenWidth) {
        double increment = (uncompressedIncrement(adjustedDuration, screenWidth) / 25.f);
        //increment = (increment % 2 == 0)? increment : increment+1;
        return increment;
    }

    //FIXME: rounding will compound error in long files, resulting in pixels being off
    //used for minimap- this is why the duration matters
    public static double getIncrement(boolean useCmp, double adjustedDuration, double screenWidth) {
        if (useCmp) {
            return compressedIncrement(adjustedDuration, screenWidth);
        } else {
            return uncompressedIncrement(adjustedDuration, screenWidth);
        }
    }
}
