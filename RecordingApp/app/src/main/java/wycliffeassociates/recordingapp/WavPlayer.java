package wycliffeassociates.recordingapp;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Plays .Wav audio files
 */
public class WavPlayer {

    private static MediaPlayer m;
    private static boolean paused = false;
    /**
     * Plays audio given a filename.
     * @param filename the absolute path to the file to be played.
     */
    public static void play(String filename){
        if(paused){
            paused = false;
            m.start();
        }
        else {
            m = new MediaPlayer();

            try {
                m.setDataSource(filename);
                m.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            m.start();
        }
    }
    public static void pause(){
        paused = true;
        m.pause();
    }

    public static void seekToStart(){
        if(m != null)
            m.seekTo(0);
    }

    public static void stop(){
        if(m != null){
            m.stop();
        }
    }

    public static void release(){
        if(m != null){
            paused = false;
            m.release();
            m = null;
        }
    }

    public static boolean isPlaying(){
        if(m != null)
            return m.isPlaying();
        else
            return false;
    }
    public static int getLocation(){
        if(m == null)
            return 0;
        else
            return m.getCurrentPosition();
    }
    public static int getDuration(){
        if(m == null)
            return 0;
        else
            return m.getDuration();
    }

}
