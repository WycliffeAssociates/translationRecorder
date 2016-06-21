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
                    //the file containing the section to insert is the destination name, so move it to a temporary file before we insert
                    File insert = new File(destination);
                    File tempInsertFile = new File(destination + "-temp.wav");
                    insert.renameTo(tempInsertFile);
                    File from = new File(previousRecording);
                    File dir = FileNameExtractor.getDirectoryFromFile(pref, from);
                    FileInputStream fisOrg = new FileInputStream(from);
                    BufferedInputStream bisOrg = new BufferedInputStream(fisOrg);

                    FileInputStream fisInsert = new FileInputStream(tempInsertFile);
                    BufferedInputStream bisInsert = new BufferedInputStream(fisInsert);

                    FileOutputStream fos = new FileOutputStream(new File(destination));
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    WavFile oldWavFile = new WavFile(from);
                    WavFile newWavFile = new WavFile(tempInsertFile);

                    int oldAudioLength = oldWavFile.getTotalAudioLength();
                    int newAudioLength = newWavFile.getTotalAudioLength();

                    int oldWritten = 0;
                    int newWritten = 0;

                    for (int i = 0; i < AudioInfo.HEADER_SIZE; i++) {
                        bos.write(bisOrg.read());
                    }
                    Logger.e(this.toString(), "wrote header");
                    for (int i = 0; i < insertLoc; i++) {
                        bos.write(bisOrg.read());
                        oldWritten++;
                    }
                    Logger.e(this.toString(), "wrote before insert");
                    fisInsert.skip(AudioInfo.HEADER_SIZE);
                    for (int i = 0; i < newAudioLength; i++) {
                        bos.write(bisInsert.read());
                        newWritten++;
                    }
                    Logger.e(this.toString(), "wrote insert");
                    for (int i = insertLoc; i < oldAudioLength; i++) {
                        bos.write(bisOrg.read());
                        oldWritten++;
                    }
                    Logger.e(this.toString(), "wrote after insert");
                    int metadataSize = oldWavFile.getTotalMetadataLength();
                    for(int i = 0; i < metadataSize; i++){
                        bos.write(bisOrg.read());
                    }
//                    byte[] metadata = WavFile.convertToMetadata(oldWavFile.getMetadata());
//                    bos.write(metadata);
                    Logger.e(this.toString(), "wrote metadata");
                    WavFileWriter.overwriteHeaderData(destination, oldAudioLength + newAudioLength, metadataSize);
                    Logger.e(this.toString(), "overwrote header");

                    bos.close(); fos.close();
                    bisInsert.close(); fisInsert.close(); tempInsertFile.delete();
                    bisOrg.close(); fisInsert.close();

                    File vis = new File(AudioInfo.pathToVisFile + "/"+FileNameExtractor.getNameWithoutExtention(insert)+".vis");
                    vis.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                } catch (JSONException e){
//                    e.printStackTrace();
//                }
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