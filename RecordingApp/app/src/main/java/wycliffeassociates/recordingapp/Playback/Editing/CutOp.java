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

    public void cut(int start, int end){
        Pair<Integer, Integer> temp = new Pair<>(start, end);
        mStack.add(temp);
        mSizeCut = totalDataRemoved();
    }

    public void undo(){
        mStack.remove(mStack.capacity() - 1);
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

    public int timeAdjusted(int timeMs){
        if(mFlattenedStack == null) {
            return timeMs;
        }
        int time = timeMs;
        for(Pair<Integer,Integer> p : mFlattenedStack){
            if(time >= p.first) {
                time += p.second - p.first;
            }
        }
        return time;
    }

    public int getSizeCut(){
        return mSizeCut;
    }
}
