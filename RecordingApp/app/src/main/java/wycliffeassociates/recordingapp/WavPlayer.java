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
        if(paused && m != null || !m.isPlaying() && m!=null && !paused){
            paused = false;
            m.start();
        }
        else if(paused && m == null){
            paused = false;
        }
        else if(m != null && m.isPlaying()){
            return;
        }
        else {
            m = new MediaPlayer();

            try {
                m.setDataSource(filename);
                System.out.println(filename + " is the loaded file to play.");
                m.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            m.start();
        }
    }
    public static void loadFile(String filename){
        release();
        m = new MediaPlayer();
        try {
            m.setDataSource(filename);
            m.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pause(){
        if(m != null) {
            paused = true;
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
        if(m != null)
            m.seekTo(0);
    }

    public static void seekTo(int x){
        if(m != null &&  x < m.getDuration())
            m.seekTo(x);
    }

    public static void stop(){
        if(m != null){
            m.stop();
        }
    }

    public static void release(){
        if(m != null){
            paused = false;
            m.reset();
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
