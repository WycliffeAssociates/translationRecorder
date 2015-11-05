package wycliffeassociates.recordingapp.Playback;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

import wycliffeassociates.recordingapp.AudioInfo;

/**
 * Plays .Wav audio files
 */
public class WavPlayer {

    private static boolean paused = false;
    private static boolean stopped = false;
    private static boolean started = true;
    private static boolean loaded = false;
    private static int duration = 0;
    private static boolean onlyPlayingSection = false;
    private static int endPlaybackPosition = 0;
    private static int startPlaybackPosition = 0;
    private static MappedByteBuffer audioData = null;
    private static AudioTrack player = null;
    private static int minBufferSize = 0;
    private static boolean keepPlaying = false;
    private static Thread playbackThread;
    private static int playbackStart = 0;
    //private static volatile int location= 0;

    public static void play(){
        if(WavPlayer.isPlaying()){
            return;
        }
        keepPlaying = true;
        player.flush();
        player.play();
        playbackThread = new Thread(){
            public void run(){
                int position = (playbackStart % 2 == 0)? playbackStart : playbackStart+1;
                audioData.position(position);
                int limit =audioData.capacity();
                short[] mBuffer = new short[minBufferSize/2];
                byte[] bytes = new byte[minBufferSize];
                while(audioData.position() < limit && keepPlaying){
                    checkIfShouldStop();
                    //location = audioData.position();
                    int numSamplesLeft = limit - audioData.position();
                    if(numSamplesLeft >= mBuffer.length) {
                        //need to grab data from the mapped file, then convert it into a short array
                        //since AudioTrack requires writing shorts for playing PCM16
                        audioData.get(bytes);
                        ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                        bytesBuffer.asShortBuffer().get(mBuffer);
                    } else {
                        for(int i=numSamplesLeft; i<mBuffer.length; i++) {
                            mBuffer[i] = 0;
                        }
                        audioData.get(bytes, 0, numSamplesLeft);
                        ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                        bytesBuffer.asShortBuffer().get(mBuffer);
                    }
                    player.write(mBuffer, 0, mBuffer.length);
                    System.out.println("location is " + getLocation() + " out of " + getDuration());
                }
                while(getLocation() != getDuration()){}
                System.out.println("end thread");
                //System.out.println("location is " + getLocation() + " out of " + getDuration());
            }
        };
        playbackThread.start();
    }

    /**
     * Sets the audio data to play back; this expects a mapped buffer of PCM data
     * Header of .wav files should not be included in this mapped buffer
     * Initializes the audio track to play this file
     * @param file
     */
    public static void loadFile(MappedByteBuffer file){
        audioData = file;
        minBufferSize = AudioTrack.getMinBufferSize(AudioInfo.SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        System.out.println("buffer size for playback is "+ minBufferSize);

        player = new AudioTrack(AudioManager.STREAM_MUSIC, AudioInfo.SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM);
    }

    public static void pause(){
        if(player != null){
            player.pause();
        }
    }

    public static boolean exists(){
        if(player != null){
            return true;
        }
        else
            return false;
    }

    public static void seekToStart(){
        if(player != null ) {
            seekTo(0);
        }
    }

    public static void seekTo(int x){
        boolean wasPlaying = isPlaying();
        stop();
        playbackStart = (int)(x * (AudioInfo.SAMPLERATE/1000.0));
        player.setNotificationMarkerPosition(duration - playbackStart - 1);
        if(wasPlaying){
            play();
        }
    }

    public static void stop(){
        if(isPlaying() || isPaused()){
            keepPlaying = false;
            player.pause();
            player.stop();
            if(playbackThread != null){
                try {
                    playbackThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playbackThread = null;
            }
            player.flush();
        }
    }

    public static void stopAt(int end){
        endPlaybackPosition = end;
        onlyPlayingSection = true;
    }

    public static void selectionStart(int start){
        startPlaybackPosition = start;
    }

    public static boolean checkIfShouldStop(){
        if(onlyPlayingSection && WavMediaPlayer.getLocation() >= endPlaybackPosition
                || getDuration() == getLocation()){
            pause();
            //WavMediaPlayer.seekTo(WavMediaPlayer.startPlaybackPosition);
            onlyPlayingSection = false;
            return true;
        }
        return false;
    }

    public static void release(){
        stop();
        audioData = null;
        player.release();
        player = null;
    }

    public static boolean isPlaying(){
        if(player != null)
            return player.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
        else
            return false;
    }

    public static boolean isPaused(){
        if(player != null)
            return player.getPlayState() == AudioTrack.PLAYSTATE_PAUSED;
        else
            return false;
    }

    public static int getLocation(){
        if(player != null)
            return Math.min((int)((playbackStart + player.getPlaybackHeadPosition()) *
                    (1000.0 / AudioInfo.SAMPLERATE)), getDuration());
        else
            return 0;
    }
    public static int getDuration(){
        if(player != null)
            return (int)(audioData.capacity()/((AudioInfo.SAMPLERATE/1000.0) * AudioInfo.BLOCKSIZE));
        else
            return 0;
    }

}
