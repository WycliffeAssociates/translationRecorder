package wycliffeassociates.recordingapp.Playback;

import android.media.MediaPlayer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Plays .Wav audio files
 */
public class WavMediaPlayer {

    private static MediaPlayer m;
    private static boolean paused = false;
    private static volatile boolean prepared = false;
    private static boolean stopped = false;
    private static boolean started = true;
    private static boolean loaded = false;
    private static int duration = 0;
    private static long timePaused = 0;
    private static long startTime = 0;
    private static long totalTimePaused = 0;
    private static boolean onlyPlayingSection = false;
    private static int endPlaybackPosition = 0;
    private static int startPlaybackPosition = 0;

    public static void play(){

        //if prepared, then resume
        if(prepared){
            m.start();
            paused = false;
            started = true;
            stopped = false;
        }
        //case where stop() was called, but the file is still loaded
        else if(!prepared && m != null && loaded){
            try {
                m.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            m.start();
            paused = false;
            started = true;
            stopped = false;
        }
    }
    public static void loadFile(String filename){
        //release a previous media player
        release();
        m = new MediaPlayer();
        try {
            //load a new file- a file descriptor is apparently safer to load
            m.reset();
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            FileDescriptor fd = fis.getFD();
            m.setDataSource(fd);
            fis.close();
            m.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    duration = m.getDuration();
                    prepared = true;
                }
            });
            m.prepare();
            loaded = true;
            started = false;
            stopped = false;
            paused = false;
            duration = m.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pause(){
        if(m != null && (started || paused)) {
            timePaused = System.currentTimeMillis();
            paused = true;
            started = false;
            stopped = false;
            m.pause();
        }
    }

    public static boolean exists(){
        if(m!= null){
            return true;
        }
        else
            return false;
    }

    public static void seekToStart(){
        if(m != null && prepared) {
            startTime = System.currentTimeMillis();
            totalTimePaused = 0;
            if(paused){
                timePaused = System.currentTimeMillis();
            }
            m.seekTo(0);
        }
    }

    public static void seekTo(int x){
        if(m != null &&  x <= m.getDuration() && prepared) {
            startTime = System.currentTimeMillis() - x;
            totalTimePaused = 0;
            if(paused){
                timePaused = System.currentTimeMillis();
            }
            m.seekTo(x);
        }
    }

    public static void stop(){
        if(m != null){
            m.stop();
            prepared = false;
            stopped = true;
            started = false;
            paused = false;
        }
    }

    public static void stopAt(int endPlaybackPosition){
        WavMediaPlayer.endPlaybackPosition = endPlaybackPosition;
        onlyPlayingSection = true;
    }

    public static void selectionStart(int startPlaybackPosition){
        WavMediaPlayer.startPlaybackPosition = startPlaybackPosition;
    }

    public static boolean checkIfShouldStop(){
        if(onlyPlayingSection && WavMediaPlayer.getLocation() >= endPlaybackPosition){
            WavMediaPlayer.pause();
            //WavMediaPlayer.seekTo(WavMediaPlayer.startPlaybackPosition);
            onlyPlayingSection = false;
            return true;
        }
        return false;
    }

    public static void release(){
        if(m != null){
            paused = false;
            m.reset();
            m.release();
            m = null;
            prepared = false;
            stopped = false;
            started = false;
            loaded = false;
        }
    }

    public static boolean isPlaying(){
        if(m != null && prepared)
            return m.isPlaying();
        else
            return false;
    }
    public static int getLocation(){
        if(m == null || !prepared)
            return 0;
        else
            return (int) m.getCurrentPosition();
    }
    public static int getDuration(){
        return duration;
    }

}
