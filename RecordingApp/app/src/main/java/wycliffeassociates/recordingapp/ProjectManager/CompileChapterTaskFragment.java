package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import wycliffeassociates.recordingapp.widgets.ChapterCard;


/**
 * Created by sarabiaj on 8/26/2016.
 */
public class CompileChapterTaskFragment extends Fragment implements DelegateCompile{

    ActivityChapterList mProgressUpdateCallback;
    Handler mHandler;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mProgressUpdateCallback = (ActivityChapterList) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mProgressUpdateCallback = null;
    }

    public void onCompileCompleted(int[] modifiedIndices) {
        mProgressUpdateCallback.onCompileCompleted(modifiedIndices);
    }

    public void compile(final List<ChapterCard> cardsToCompile, final int[] toNotify){
        mProgressUpdateCallback.setIsCompiling();
        Thread compileThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < cardsToCompile.size(); i++){
                    cardsToCompile.get(i).compile();
                    mProgressUpdateCallback.setCompilingProgress((int)((i/(float)cardsToCompile.size()) * 100));
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onCompileCompleted(toNotify);
                    }
                });
            }
        });
        compileThread.start();
    }

}
