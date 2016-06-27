package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by sarabiaj on 3/10/2016.
 */
public class InsertTaskFragment extends Fragment {
    public interface Insert{
        void writeInsert(String to, String from, int insertLoc);
    }

    private RecordingScreen mCtx;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCtx = (RecordingScreen)activity;
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

    public void writeInsert(final String destination, final String previousRecording, final int insertTime, final  SharedPreferences pref) {
        Thread write = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int insertLoc = timeToIndex(insertTime);
                    WavFile base = new WavFile(new File(previousRecording));
                    WavFile insert = new WavFile(new File(destination));
                    WavFile result = WavFile.insertWavFile(base, insert, insertLoc);
                    insert.getFile().delete();
                    File vis = new File(AudioInfo.pathToVisFile + "/" + FileNameExtractor.getNameWithoutExtention(insert.getFile())+".vis");
                    vis.delete();
                    result.getFile().renameTo(insert.getFile());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e){
                    e.printStackTrace();
                }
                mCtx.insertCallback(destination);
            }
        });
        write.start();
    }

    private int timeToIndex(int timeMs){
        int seconds = timeMs/1000;
        int ms = (timeMs-(seconds*1000));
        int tens = ms/10;

        int idx = (AudioInfo.SAMPLERATE * seconds) + (ms * 44) + (tens);
        idx*=2;
        return idx;
    }
}