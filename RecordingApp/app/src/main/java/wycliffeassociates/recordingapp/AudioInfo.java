package wycliffeassociates.recordingapp;


import android.media.AudioFormat;

/**
 * Created by Jsarabia on 7/30/15.
 */
public class AudioInfo{
    public static final int BPP = 16;
    public static final String FILE_EXT = ".wav";
    public static final String APP_FOLDER = "AudioRecorder";
    public static final String TEMP_FILE = "record_temp.raw";
    public static final int SAMPLERATE = 44100;
    public static final int CHANNEL_TYPE = AudioFormat.CHANNEL_IN_MONO;
    public static final int NUM_CHANNELS = 1;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BLOCKSIZE = 2;
    public static final int HEADER_SIZE = 44;
    public static final int SIZE_OF_SHORT = 2;
    public static final int AMPLITUDE_RANGE = 32767;
    public static final int COMPRESSION_RATE = 100;
    public static String pathToVisFile = "";
    public static int COMPRESSED_SECONDS_ON_SCREEN = 1;
    public static String fileDir = "";
    public static int SCREEN_WIDTH;

    private AudioInfo(){}
}
