package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Utils;
import wycliffeassociates.recordingapp.wav.WavFile;

/**
 * Created by sarabiaj on 3/10/2016.
 */
public class InsertTaskFragment extends Fragment {
    public interface Insert{
        void writeInsert(WavFile base, WavFile insertClip, int insertLoc);
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

    public void writeInsert(final WavFile base, final WavFile insertClip, final int insertTime) {
        Thread write = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int insertLoc = timeToIndex(insertTime);
                    WavFile result = WavFile.insertWavFile(base, insertClip, insertLoc);
                    insertClip.getFile().delete();
                    File vis = new File(Utils.VISUALIZATION_DIR, FileNameExtractor.getNameWithoutExtention(insertClip.getFile())+".vis");
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

    private int timeToIndex(int timeMs){
        int seconds = timeMs/1000;
        int ms = (timeMs-(seconds*1000));
        int tens = ms/10;

        int idx = (AudioInfo.SAMPLERATE * seconds) + (ms * 44) + (tens);
        idx*=2;
        return idx;
    }
}