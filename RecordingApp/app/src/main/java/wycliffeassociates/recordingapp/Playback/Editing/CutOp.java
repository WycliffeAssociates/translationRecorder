package wycliffeassociates.recordingapp.Playback.Editing;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * Created by sarabiaj on 12/22/2015.
 */
public class CutOp {
    private Vector<Pair<Integer, Integer>> mStack;
    private Vector<Pair<Integer, Integer>> mFlattenedStack;
    private int mSizeCut = 0;

    public CutOp(){
        mStack = new Vector<>();
    }

    public Vector<Pair<Integer,Integer>> getFlattenedStack(){
        return mFlattenedStack;
    }

    public void cut(int start, int end){
        Pair<Integer, Integer> temp = new Pair<>(start, end);
        mStack.add(temp);
        mSizeCut = totalDataRemoved();
    }

    public void undo(){
        if(mStack.size() == 0){
            return;
        }
        mStack.remove(mStack.size() - 1);
        mSizeCut = totalDataRemoved();
    }

    public int skip(int time){
        int max = -1;
        for(Pair<Integer,Integer> cut : mStack) {
            if (time >= cut.first && time < cut.second) {
                max = Math.max(cut.second, max);
            }
        }
        return max;
    }

    /**
     * Computes the total time removed from cutting, for use in ACTUAL capacity computations.
     * Also generates a private simplified and ordered version of this stack, eliminating nested
     * cuts.
     *
     * Begins by sorting the stack by starting cuts in a copied stack. Beginning with the earliest
     * cut, it is added to a list and removed from the stack. For each cut in this list (beginning
     * only with one cut) cuts are added if they start between the current cut's start and end. Each
     * cut that is added is removed from the stack. When the list has been iterated over, the min
     * and max are computed, which forms a new pair to be added to mFlattenedStack. The difference
     * between min and max are added to a sum. This process is repeated until the stack is empty.
     *
     * @return the total time removed from cuts.
     */
    private int totalDataRemoved(){
        Vector<Pair<Integer,Integer>> copy = new Vector<>(mStack.capacity());
        mFlattenedStack = new Vector<>();
        for(Pair<Integer,Integer> p : mStack){
            copy.add(new Pair<>(p.first,p.second));
        }
        Collections.sort(copy, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> lhs, Pair<Integer, Integer> rhs) {
                if(lhs.first == rhs.first) {
                    return 0;
                } else if (lhs.first > rhs.first){
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        int sum = 0;
        while(!copy.isEmpty()){
            Pair<Integer,Integer> pair = copy.firstElement();
            ArrayList<Pair<Integer,Integer>> list = new ArrayList<>();
            list.add(pair);
            for(Pair<Integer,Integer> p : list){
                copy.remove(p);
                for(Pair<Integer,Integer> q : copy){
                    if(q.first >= p.first || q.first <= p.second){
                        list.add(q);
                        copy.remove(q);
                    }
                }
            }
            int start = list.get(0).first;
            int end = list.get(0).second;
            for(int i = 1; i < list.size(); i++){
                end = (end < list.get(i).second)? list.get(i).second : end;
            }
            mFlattenedStack.add(new Pair<Integer, Integer>(start, end));
            sum += end-start;
        }
        return sum;
    }

    /**
     * Since the marker position takes into account total data played by audiotrack, the position
     * is agnostic of the "actual" position. This method computes the time to add back to it,
     * it takes the original time, looks to see if it's greater than or equal to a start cut. If so
     * it adds total time cut out, and adds this to time. Time is then compared to the next cut, and
     * the process is repeated. Break when the next cut takes place at a later time than we're at.
     *
     * mFlattenedStack is a representation of the cut stack WITHOUT any nested cuts, and based on
     * the way it is computed, we can assume this list is sorted.
     *
     * @param timeMs location that was computed from WavPlayer before considering cuts
     * @return inflated time accounting for cuts
     */
    public int timeAdjusted(int timeMs){
        if(mFlattenedStack == null) {
            return timeMs;
        }
        int time = timeMs;
        for(Pair<Integer,Integer> p : mFlattenedStack){
            if(time >= p.first) {
                time += p.second - p.first;
            } else {
                break;
            }
        }
        return time;
    }

    public int getSizeCut(){
        return mSizeCut;
    }
}
