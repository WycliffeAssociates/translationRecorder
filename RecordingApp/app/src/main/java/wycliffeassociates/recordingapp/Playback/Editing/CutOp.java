package wycliffeassociates.recordingapp.Playback.Editing;

import android.util.Pair;

import java.util.Vector;

/**
 * Created by sarabiaj on 12/22/2015.
 */
public class CutOp {
    private Vector<Pair<Integer, Integer>> mStack;

    public CutOp(){
        mStack = new Vector<>();
    }

    public void cut(int start, int end){
        Pair<Integer, Integer> temp = new Pair<>(start, end);
        mStack.add(temp);
    }

    public void undo(){
        mStack.remove(mStack.capacity()-1);
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
}
