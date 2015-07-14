package wycliffeassociates.recordingapp;


        import android.media.MediaPlayer;

        import java.io.IOException;

/**
 * Plays .Wav audio files
 */
public class WavPlayer {

    /**
     * Plays audio given a filename.
     * @param filename the absolute path to the file to be played.
     */
    public static void play(String filename){
        MediaPlayer m = new MediaPlayer();

        /*
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        m.setAudioStreamType(AudioManager.STREAM_MUSIC);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);
        */

        try {
            m.setDataSource(filename);
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            m.prepare();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        m.start();
    }
}
