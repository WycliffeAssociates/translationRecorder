package org.wycliffeassociates.translationrecorder.Recording;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import org.json.JSONException;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by sarabiaj on 3/10/2016.
 */
public class InsertTaskFragment extends Fragment {
    public interface Insert{
        void writeInsert(WavFile base, WavFile insertClip, int insertLoc);
    }

    private RecordingActivity mCtx;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCtx = (RecordingActivity)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach(){
        super.onDetach();
    }

    public void writeInsert(final WavFile base, final WavFile insertClip, final int insertFrame) {
        Thread write = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WavFile result = WavFile.insertWavFile(base, insertClip, insertFrame);
                    insertClip.getFile().delete();
                    File dir = new File(mCtx.getExternalCacheDir(), "Visualization");
                    File vis = new File(dir, ProjectFileUtils.getNameWithoutExtention(insertClip.getFile())+".vis");
                    vis.delete();
                    result.getFile().renameTo(insertClip.getFile());
                    mCtx.insertCallback(new WavFile(insertClip.getFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e){
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        });
        write.start();
    }
}