package org.wycliffeassociates.translationrecorder.Recording;


import android.app.Service;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.AudioInfo;


/**
 * Contains the ability to record audio and output to a .wav file.
 * The file is written to a temporary .raw file, then upon a stop call
 * the file is copied into a .wav file with a UUID name.
 * <p>
 * Recorded files can be renamed with a call to toSave()
 */
public class WavRecorder extends Service {
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private volatile boolean isRecording = false;
    private boolean mVolumeTest = false;
    private byte data[];
    private boolean permissionsError = false;

    public static String KEY_VOLUME_TEST = "key_volume_test";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        isRecording = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mVolumeTest = intent.getBooleanExtra(KEY_VOLUME_TEST, false);
        record();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRecording = false;
        if(!permissionsError) {
            recorder.stop();
            recorder.release();
        }
        super.onDestroy();
    }

    private void record() {
        bufferSize = AudioRecord.getMinBufferSize(AudioInfo.SAMPLERATE, AudioInfo.CHANNEL_TYPE, AudioInfo.ENCODING);
        try {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    AudioInfo.SAMPLERATE, AudioInfo.CHANNEL_TYPE, AudioInfo.ENCODING, bufferSize);
            int i = recorder.getState();
            if (i == 1)
                recorder.startRecording();

            isRecording = true;

            recordingThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    feedToQueues();
                }
            }, "AudioRecorder Thread");
            recordingThread.start();
        } catch (IllegalArgumentException e) {
            //The lenovo tab 2 can deny app permissions in a weird way and will cause setting up the
            //AudioRecord object to throw an illegal argument exception, and crash the app. It will report
            //having the permission, so the only way to check for it being denied is to check for this exception
            //In this case, start the following activity to provide a dialog to the user
            permissionsError = true;
            startActivity(new Intent(this, PermissionsDeniedActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void feedToQueues() {
        data = new byte[bufferSize];
        int read = 0;
        while (isRecording) {
            read = recorder.read(data, 0, bufferSize);

            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                RecordingMessage temp = new RecordingMessage(data, false, false);
                try {
                    if (mVolumeTest) {
                        RecordingQueues.UIQueue.put(temp);
                    }
                    RecordingQueues.writingQueue.put(temp);
                    RecordingQueues.compressionQueue.put(temp);
                } catch (InterruptedException e) {
                    Logger.e(this.toString(), "InterruptedException in feeding to queues", e);
                    e.printStackTrace();
                }
            }
        }
    }


}