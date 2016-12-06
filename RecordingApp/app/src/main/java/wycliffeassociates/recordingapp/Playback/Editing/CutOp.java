package wycliffeassociates.recordingapp.Playback.Editing;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by sarabiaj on 12/22/2015.
 */
public class CutOp {
    private Vector<Pair<Integer, Integer>> mTimeStack;
    private Vector<Pair<Integer, Integer>> mFlattenedFrameStack;
    private Vector<Pair<Integer, Integer>> mUncompressedFrameStack;
    private Vector<Pair<Integer, Integer>> mCompressedFrameStack;
    private int mSizeTimeCut = 0;
    private int mSizeFrameCutCmp;
    private int mSizeFrameCutUncmp;

    public CutOp() {
        mTimeStack = new Vector<>();
    }

    public synchronized Vector<Pair<Integer,Integer>> getFlattenedStack(){
        return mFlattenedFrameStack;
    }

    public synchronized void cut(int startFrame, int endFrame){
        Pair<Integer, Integer> temp = new Pair<>(startFrame, endFrame);
        mUncompressedFrameStack.add(temp);
        //mSizeTimeCut = totalDataRemoved(); //?
        Logger.w(this.toString(), "Generating location stacks");
        generateTimeStack();
        generateCutStackCmpLoc();
    }

    public synchronized void clear(){
        mTimeStack.clear();
        mFlattenedFrameStack.clear();
        mCompressedFrameStack.clear();
        mUncompressedFrameStack.clear();
        mSizeTimeCut = 0;
        mSizeFrameCutCmp = 0;
        mSizeFrameCutUncmp = 0;
    }

    public synchronized void undo(){
        if(mUncompressedFrameStack.size() == 0){
            return;
        }
        mUncompressedFrameStack.remove(mUncompressedFrameStack.size() - 1);
        //mSizeTimeCut = totalDataRemoved();
        generateTimeStack();
        generateCutStackCmpLoc();
    }

    //change to use flattened stack
    public synchronized int skip(int frame){
        int max = -1;
        for (Pair<Integer, Integer> cut : mUncompressedFrameStack) {
            if (frame >= cut.first && frame < cut.second) {
                max = Math.max(cut.second, max);
            }
        }
        return max;
    }

    public synchronized boolean hasCut(){
        if(mUncompressedFrameStack.size() > 0){
            return true;
        } else {
            return false;
        }
    }

    public synchronized int skipReverse(int frame){
        int min = Integer.MAX_VALUE;
        for (Pair<Integer, Integer> cut : mUncompressedFrameStack) {
            if (frame > cut.first && frame <= cut.second) {
                min = Math.min(cut.first, min);
            }
        }
        return min;
    }

    /**
     * Computes the total time removed from cutting, for use in ACTUAL capacity computations.
     * Also generates a private simplified and ordered version of this stack, eliminating nested
     * cuts.
     * <p/>
     * Begins by sorting the stack by starting cuts in a copied stack. Beginning with the earliest
     * cut, it is added to a list and removed from the stack. For each cut in this list (beginning
     * only with one cut) cuts are added if they start between the current cut's start and end. Each
     * cut that is added is removed from the stack. When the list has been iterated over, the min
     * and max are computed, which forms a new pair to be added to mFlattenedFrameStack. The difference
     * between min and max are added to a sum. This process is repeated until the stack is empty.
     *
     * @return the total time removed from cuts.
     */
    private synchronized int totalFramesRemoved(){
        Vector<Pair<Integer,Integer>> copy = new Vector<>(mUncompressedFrameStack.capacity());
        mFlattenedFrameStack = new Vector<>();
        for (Pair<Integer, Integer> p : mUncompressedFrameStack) {
            copy.add(new Pair<>(p.first, p.second));
        }
        Collections.sort(copy, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public synchronized int compare(Pair<Integer, Integer> lhs, Pair<Integer, Integer> rhs) {
                if(lhs.first == rhs.first) {
                    return 0;
                } else if (lhs.first > rhs.first) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        int sum = 0;
        Logger.w(this.toString(), "Generating flattened stack and computing time removed");
        while (!copy.isEmpty()) {
            Pair<Integer, Integer> pair = copy.firstElement();
            ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
            list.add(pair);
            for(int i = 0; i < list.size(); i++){
                Pair<Integer,Integer> p =  list.get(i);
                copy.remove(p);
                for(int j = copy.size()-1; j >= 0; j--){
                    Pair<Integer,Integer> q = copy.get(j);
                    if((q.first >= p.first && q.first <= p.second) || (p.first >= q.first && p.first <= q.second)){
                        list.add(q);
                        copy.remove(q);
                    }
                }
            }
            int start = list.get(0).first;
            int end = list.get(0).second;
            for (int i = 1; i < list.size(); i++) {
                end = (end < list.get(i).second) ? list.get(i).second : end;
            }
            mFlattenedFrameStack.add(new Pair<Integer, Integer>(start, end));
            sum += end - start;
        }
        return sum;
    }

    /**
     * Since the marker position takes into account total data played by audiotrack, the position
     * is agnostic of the "actual" position. This method computes the time to add back to it,
     * it takes the original time, looks to see if it's greater than or equal to a start cut. If so
     * it adds total time cut out, and adds this to time. Time is then compared to the next cut, and
     * the process is repeated. Break when the next cut takes place at a later time than we're at.
     * <p/>
     * mFlattenedFrameStack is a representation of the cut stack WITHOUT any nested cuts, and based on
     * the way it is computed, we can assume this list is sorted.
     *
     * @param frame location that was computed from BufferPlayer before considering cuts
     * @return inflated time accounting for cuts
     */
    public synchronized int frameAdjusted(int frame){
        if(mFlattenedFrameStack == null) {
            return frame;
        }
        for (Pair<Integer, Integer> p : mFlattenedFrameStack) {
            if (frame >= p.first) {
                frame += p.second - p.first;
            } else {
                break;
            }
        }
        return frame;
    }

    public synchronized int timeAdjusted(int frame, int playbackStart){
        if(mFlattenedFrameStack == null) {
            return frame;
        }
        for (Pair<Integer, Integer> p : mFlattenedFrameStack) {
            if (p.second > playbackStart) {
                if (frame >= p.first) {
                    frame += p.second - p.first;
                } else {
                    break;
                }
            }
        }
        return frame;
    }

    /**
     * Given an absolute time in the uncut waveform, this method
     * returns the adjusted time in the cut waveform.  The given
     * time must not be in an existing cut.
     *
     * @param frame a time in ms in the uncut waveform.  timeMs must not
     *               be in an existing cut.
     * @return the adjusted time in the cut waveform.
     */
    public synchronized int reverseFrameAdjusted(int frame){
        if (mFlattenedFrameStack == null) {
            return frame;
        }
        int adjustedFrame = frame;
        for (Pair<Integer, Integer> p : mFlattenedFrameStack) {
            if (frame >= p.second) {
                adjustedFrame -= p.second - p.first;
            } else {
                break;
            }
        }

        return adjustedFrame;
    }

    private synchronized void generateTimeStack(){
        mSizeTimeCut = 0;
        mTimeStack = new Vector<Pair<Integer, Integer>>();
        for (Pair<Integer, Integer> p : mFlattenedFrameStack) {
            Pair<Integer, Integer> y = new Pair<>(uncompressedFrameToTime(p.first), uncompressedFrameToTime(p.second));
            mTimeStack.add(y);
            mSizeTimeCut += y.second - y.first;
        }
    }

    private synchronized void generateCutStackUncmpLoc(){
        mSizeFrameCutUncmp = 0;
        mUncompressedFrameStack = new Vector<Pair<Integer, Integer>>();
        for (Pair<Integer, Integer> p : mFlattenedFrameStack) {
            Pair<Integer, Integer> y = new Pair<>((p.first), (p.second));
            mUncompressedFrameStack.add(y);
            mSizeFrameCutUncmp += y.second - y.first;
        }
    }

    private synchronized void generateCutStackCmpLoc(){
        mSizeFrameCutCmp = 0;
        mCompressedFrameStack = new Vector<Pair<Integer, Integer>>();
        for (Pair<Integer, Integer> p : mFlattenedFrameStack) {
            Pair<Integer, Integer> y = new Pair<>(uncompressedToCompressed(p.first), uncompressedToCompressed(p.second));
            mCompressedFrameStack.add(y);
            mSizeFrameCutCmp += y.second - y.first;
        }
    }

    public int timeToUncmpLoc(int timeMs){
        int seconds = timeMs/1000;
        int ms = (timeMs-(seconds*1000));
        int tens = ms/10;


        int idx = (AudioInfo.SAMPLERATE * seconds) + (ms * 44) + (tens);
        //idx *= 2;
        return idx;
    }

    public int timeToCmpLoc(int timeMs){
        int seconds = timeMs/1000;
        int ms = (timeMs-(seconds*1000));
        int tens = ms/10;


        int idx = (AudioInfo.SAMPLERATE * seconds) + (ms * 44) + (tens);
        idx /= 25;
        //idx *= 2;
        return idx;
    }

    public int uncompressedFrameToTime(int frame) {
        return (int) Math.floor(frame/44.1);
    }

    public int uncompressedToCompressed(int frame) {
        return frame / 25;
    }

    public synchronized int skipFrame(int frame, boolean compressed){
        int max = -1;
        Vector<Pair<Integer, Integer>> stack = (compressed) ? mCompressedFrameStack : mUncompressedFrameStack;
        for (Pair<Integer, Integer> cut : stack) {
            if (frame >= cut.first && frame < cut.second) {
                max = Math.max(cut.second, max);
            }
        }
        return max;
    }

    public synchronized int relativeLocToAbsolute(int frame, boolean compressed){
        Vector<Pair<Integer,Integer>> stack = (compressed)? mCompressedFrameStack : mUncompressedFrameStack;
        if(stack == null){
            return frame;
        }
        for (Pair<Integer, Integer> cut : stack) {
            if (frame >= cut.first) {
                frame += cut.second - cut.first;
            }
        }
        return frame;
    }

    public synchronized boolean cutExistsInRange(int frame, int range){
        if(hasCut()) {
            for (Pair<Integer, Integer> cut : mUncompressedFrameStack) {
                //if the frame is in the middle of a cut
                if (frame >= cut.first && frame <= cut.second) {
                    return true;
                    //if the cut is between the frame and the end of the range
                } else if (frame < cut.first && (frame + range) >= cut.second) {
                    return true;
                }
            }
        }
        //otherwise no cut
        return false;
    }


    public synchronized int absoluteLocToRelative(int frame, boolean compressed){
        Vector<Pair<Integer,Integer>> stack = (compressed)? mCompressedFrameStack : mUncompressedFrameStack;
        int loc = frame;
        if(stack == null){
            return loc;
        }
        for(int i = stack.size()-1; i >=0; i--) {
            Pair<Integer,Integer> cut = stack.get(i);
            if (frame >= cut.second) {
                loc -= cut.second - cut.first;
            }
        }
        return loc;
    }

    public synchronized int getSizeTimeCut(){
        return mSizeTimeCut;
    }
    public synchronized int getSizeFrameCutCmp(){
        return mSizeFrameCutCmp;
    }
    public synchronized int getSizeFrameCutUncmp(){
        return mSizeFrameCutUncmp;
    }
}
