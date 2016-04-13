package wycliffeassociates.recordingapp.Recording;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import wycliffeassociates.recordingapp.AudioInfo;
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

    public void writeInsert(final String to, final String from, final int insertTime) {
        Thread write = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int insertLoc = timeToIndex(insertTime) + AudioInfo.HEADER_SIZE;
                    File old = new File(AudioInfo.fileDir + "/" + to + ".wav");
                    File original = new File(AudioInfo.fileDir + "/" + to + "-temp.wav");
                    old.renameTo(original);
                    File insert = new File(from);
                    FileInputStream fisOrg = new FileInputStream(original);
                    BufferedInputStream bisOrg = new BufferedInputStream(fisOrg);

                    FileInputStream fisInsert = new FileInputStream(insert);
                    BufferedInputStream bisInsert = new BufferedInputStream(fisInsert);

                    String result = AudioInfo.fileDir + "/" + to + ".wav";
                    FileOutputStream fos = new FileOutputStream(new File(result));
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    for (int i = 0; i < AudioInfo.HEADER_SIZE; i++) {
                        bos.write(bisOrg.read());
                    }
                    Logger.e(this.toString(), "wrote header");
                    for (int i = AudioInfo.HEADER_SIZE; i < insertLoc; i++) {
                        bos.write(bisOrg.read());
                    }
                    Logger.e(this.toString(), "wrote before insert");
                    fisInsert.skip(AudioInfo.HEADER_SIZE);
                    for (int i = AudioInfo.HEADER_SIZE; i < insert.length(); i++) {
                        bos.write(bisInsert.read());
                    }
                    Logger.e(this.toString(), "wrote insert");
                    for (int i = insertLoc; i < original.length(); i++) {
                        bos.write(bisOrg.read());
                    }
                    Logger.e(this.toString(), "wrote after insert");
                    WavFileWriter.overwriteHeaderData(to, insert.length() + original.length() - AudioInfo.HEADER_SIZE);
                    Logger.e(this.toString(), "overwrote header");

                    bos.close(); fos.close();
                    bisInsert.close(); fisInsert.close(); insert.delete();
                    bisOrg.close(); fisInsert.close(); original.delete();

                    File vis = new File(AudioInfo.pathToVisFile + "/visualization.vis");
                    vis.delete();
                    vis = new File(AudioInfo.pathToVisFile + "/"+to+".vis");
                    vis.delete();
                } catch (IOException e){
                    e.printStackTrace();
                }
                mCtx.insertCallback(to);
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