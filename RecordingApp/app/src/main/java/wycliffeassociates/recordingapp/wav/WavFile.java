package wycliffeassociates.recordingapp.wav;

import android.media.AudioFormat;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.Recording.WavFileWriter;
import wycliffeassociates.recordingapp.Reporting.Logger;

/**
 * Created by sarabiaj on 6/2/2016.
 */
public class WavFile implements Parcelable {

    public static final int SAMPLERATE = 44100;
    public static final int CHANNEL_TYPE = AudioFormat.CHANNEL_IN_MONO;
    public static final int NUM_CHANNELS = 1;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BLOCKSIZE = 2;
    public static final int HEADER_SIZE = 44;
    public static final int SIZE_OF_SHORT = 2;
    public static final int AMPLITUDE_RANGE = 32767;
    public static final int BPP = 16;

    File mFile;
    Metadata mMetadata;
    private int mTotalAudioLength = 0;
    private int mTotalDataLength = 0;
    private int mMetadataLength = 0;

    public static WavFile createNewWavFile(File file, Project project, String chapter, String startVerse, String endVerse) throws IOException {
        WavFile wavFile = new WavFile(file);
        wavFile.setMetadata(project, chapter, startVerse, endVerse);
        wavFile.overwriteHeaderData();
        return wavFile;
    }

    //Files without a valid wav header will be blown away and replaced with an empty wav file
    //not sure if this is good, but should the assumption be that the alternative is a file containing
    //raw PCM data?
    public WavFile(File file) {
        mFile = file;
        try {
            boolean properForm = parseHeader();
            if (mFile.length() > 0) {
                if (properForm) {
                    byte[] metadataBytes = parseInfo();
                    mMetadata = new Metadata(readTrackInfo(metadataBytes));
                } else {
                    rawPcmToWav();
                }
            } else {
                initializeWavFile();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WavFile(File file, Project project, String chapter, String startVerse, String endVerse) throws JSONException, IOException {
        this(file);
        setMetadata(project, chapter, startVerse, endVerse);
    }

    public WavFile(Parcel in) {
        mFile = new File(in.readString());
        try {
            mMetadata = new Metadata(new JSONObject(in.readString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTotalAudioLength = 0;
        mTotalDataLength = 0;
        mMetadataLength = 0;
        if (mFile.length() > 0) {
            parseChunkSizes();
        }
    }

    public WavFile(File file, String jsonMetadata) {
        mFile = file;
        mMetadataLength = 0;
        mTotalAudioLength = (int) file.length() - HEADER_SIZE;
        mTotalDataLength = (int) file.length() - 8;
        try {
            mMetadata = new Metadata(new JSONObject(jsonMetadata));
            writeMetadata();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseChunkSizes() {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(mFile, "r");
            byte[] size = new byte[4];
            raf.seek(4);
            raf.read(size);
            mTotalDataLength = littleEndianToDecimal(size);
            raf.seek(40);
            raf.read(size);
            mTotalAudioLength = littleEndianToDecimal(size);
            //check if this is okay
            raf.seek(44 + mTotalAudioLength);
            raf.read(size);
            String tag = new String(size, StandardCharsets.US_ASCII);
            if (tag.compareTo("LIST") == 0) {
                raf.seek(44 + mTotalAudioLength + 16);
                raf.read(size);
                mMetadataLength = littleEndianToDecimal(size);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                Logger.e(this.toString(), "IOException while closing stream", e);
                e.printStackTrace();
            }
        }
    }

    public File getFile() {
        return mFile;
    }

    public int getTotalAudioLength() {
        return mTotalAudioLength;
    }

    public int getTotalDataLength() {
        return mTotalDataLength;
    }

    public int getTotalMetadataLength() {
        return mMetadataLength + 20;
    }

    public void initializeWavFile() {
        try (FileOutputStream fos = new FileOutputStream(mFile, false)) {
            mFile.getParentFile().mkdirs();

            mTotalDataLength = HEADER_SIZE - 8;
            mTotalAudioLength = 0;
            byte[] header = new byte[44];
            long longSampleRate = SAMPLERATE;
            long byteRate = (BPP * SAMPLERATE * NUM_CHANNELS) / 8;

            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (mTotalDataLength & 0xff);
            header[5] = (byte) ((mTotalDataLength >> 8) & 0xff);
            header[6] = (byte) ((mTotalDataLength >> 16) & 0xff);
            header[7] = (byte) ((mTotalDataLength >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f'; // fmt  chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16; // 4 bytes: size of fmt chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1; // format = 1
            header[21] = 0;
            header[22] = (byte) NUM_CHANNELS; // number of channels
            header[23] = 0;
            header[24] = (byte) (longSampleRate & 0xff);
            header[25] = (byte) ((longSampleRate >> 8) & 0xff);
            header[26] = (byte) ((longSampleRate >> 16) & 0xff);
            header[27] = (byte) ((longSampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) ((NUM_CHANNELS * BPP) / 8); // block align
            header[33] = 0;
            header[34] = BPP; // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = 0;
            header[41] = 0;
            header[42] = 0;
            header[43] = 0;

            fos.write(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO: loading screen
    private void rawPcmToWav() {
        File temp = null;
        FileInputStream pcmIn = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            temp = File.createTempFile("temp", "wav");
            pcmIn = new FileInputStream(mFile);
            bis = new BufferedInputStream(pcmIn);
            fos = new FileOutputStream(temp);
            bos = new BufferedOutputStream(fos);

            bos.write(new byte[44]);

            int in;
            while ((in = bis.read()) != -1) {
                bos.write(in);
            }

            bos.close();
            fos.close();
            bis.close();
            pcmIn.close();

            mTotalAudioLength = (int) mFile.length();
            mTotalDataLength = mTotalAudioLength + HEADER_SIZE - 8;

            mFile.delete();
            temp.renameTo(mFile);

            overwriteHeaderData();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                pcmIn.close();
                bos.close();
                fos.close();
            } catch (IOException e) {
                Logger.e(this.toString(), "IOException while closing streams", e);
                e.printStackTrace();
            }
        }
    }

    public void setMetadata(Project project, String chapter, String startVerse, String endVerse) {
        mMetadata = new Metadata(project, chapter, startVerse, endVerse);
    }

    private int writeMetadata(String metadata) throws IOException {
        byte[] data = convertToMetadata(metadata);
        BufferedOutputStream bof = null;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mFile, true);
            //truncates existing metadata- new metadata may not be as long
            out.getChannel().truncate(HEADER_SIZE + mTotalAudioLength);
            bof = new BufferedOutputStream(out);
            bof.write(data);
        } finally {
            try {
                bof.close();
                out.close();
            } catch (IOException e) {
                Logger.e(this.toString(), "IOException while closing streams", e);
                e.printStackTrace();
            }
        }
        mMetadataLength = data.length;
        mTotalDataLength = mTotalAudioLength + mMetadataLength + HEADER_SIZE - 8;
        overwriteHeaderData();
        return data.length;
    }

    public int writeMetadata() throws IOException {
        return writeMetadata(getMetadata());
    }

    public static byte[] convertToMetadata(String metadata) {
        //word align
        int padding = metadata.length() % 4;
        if (padding != 0) {
            padding = 4 - padding;
        }
        byte[] infoTag = new byte[metadata.length() + padding + 20];

        int metadataSize = metadata.length() + padding;
        int chunkSize = 12 + metadataSize;

        infoTag[0] = 'L';
        infoTag[1] = 'I';
        infoTag[2] = 'S';
        infoTag[3] = 'T';
        infoTag[4] = (byte) (chunkSize & 0xff);
        infoTag[5] = (byte) ((chunkSize >> 8) & 0xff);
        infoTag[6] = (byte) ((chunkSize >> 16) & 0xff);
        infoTag[7] = (byte) ((chunkSize >> 24) & 0xff);
        infoTag[8] = 'I';
        infoTag[9] = 'N';
        infoTag[10] = 'F';
        infoTag[11] = 'O';
        infoTag[12] = 'I'; // fmt  chunk
        infoTag[13] = 'A';
        infoTag[14] = 'R';
        infoTag[15] = 'T';
        infoTag[16] = (byte) (metadataSize & 0xff);
        infoTag[17] = (byte) ((metadataSize >> 8) & 0xff);
        infoTag[18] = (byte) ((metadataSize >> 16) & 0xff);
        infoTag[19] = (byte) ((metadataSize >> 24) & 0xff);

        for (int i = 20; i < metadata.length() + 20; i++) {
            infoTag[i] = (metadata.getBytes(StandardCharsets.US_ASCII))[i - 20];
        }
        for (int i = metadata.length() + 20; i < infoTag.length; i++) {
            infoTag[i] = '\0';
        }
        return infoTag;
    }

    public void overwriteHeaderData() {
        RandomAccessFile fileAccessor = null;
        try {
            //if total length is still just the header, then check the file size
            if (mTotalDataLength == (HEADER_SIZE - 8)) {
                mTotalAudioLength = (int) mFile.length() - HEADER_SIZE - mMetadataLength;
                mTotalDataLength = mTotalAudioLength + HEADER_SIZE - 8 + mMetadataLength;
            }

            fileAccessor = new RandomAccessFile(mFile, "rw");
            //seek to header[4] to overwrite data length
            long longSampleRate = SAMPLERATE;
            long byteRate = (BPP * SAMPLERATE * NUM_CHANNELS) / 8;
            byte[] header = new byte[44];

            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (mTotalDataLength & 0xff);
            header[5] = (byte) ((mTotalDataLength >> 8) & 0xff);
            header[6] = (byte) ((mTotalDataLength >> 16) & 0xff);
            header[7] = (byte) ((mTotalDataLength >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f'; // fmt  chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16; // 4 bytes: size of fmt chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1; // format = 1
            header[21] = 0;
            header[22] = (byte) NUM_CHANNELS; // number of channels
            header[23] = 0;
            header[24] = (byte) (longSampleRate & 0xff);
            header[25] = (byte) ((longSampleRate >> 8) & 0xff);
            header[26] = (byte) ((longSampleRate >> 16) & 0xff);
            header[27] = (byte) ((longSampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) ((NUM_CHANNELS * BPP) / 8); // block align
            header[33] = 0;
            header[34] = BPP; // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (mTotalAudioLength & 0xff);
            header[41] = (byte) ((mTotalAudioLength >> 8) & 0xff);
            header[42] = (byte) ((mTotalAudioLength >> 16) & 0xff);
            header[43] = (byte) ((mTotalAudioLength >> 24) & 0xff);
            fileAccessor.write(header);
        } catch (FileNotFoundException e) {
            Logger.e(this.toString(), "FileNotFound overwriting header", e);
            e.printStackTrace();
        } catch (IOException e) {
            Logger.e(this.toString(), "IOException overwriting header", e);
            e.printStackTrace();
        } finally {
            try {
                fileAccessor.close();
            } catch (IOException e) {
                Logger.e(this.toString(), "IOException while closing streams", e);
                e.printStackTrace();
            }
        }
    }

    private boolean parseHeader() throws IOException {
        if (mFile != null && mFile.length() >= 44) {
            byte[] word = new byte[4];
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(mFile, "r");
                raf.read(word);
                String riff = new String(word, StandardCharsets.US_ASCII);
                if (riff.compareTo("RIFF") == 0) {
                    //raf.seek(4);
                    raf.read(word);
                    mTotalDataLength = littleEndianToDecimal(word);
                    raf.read(word);
                    String wave = new String(word, StandardCharsets.US_ASCII);
                    if (wave.compareTo("WAVE") == 0) {
                        raf.seek(40);
                        raf.read(word);
                        mTotalAudioLength = littleEndianToDecimal(word);
                        return true;
                    }
                }
            } finally {
                try {
                    raf.close();
                } catch (IOException e) {
                    Logger.e(this.toString(), "IOException while closing stream", e);
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private byte[] parseInfo() throws IOException {
        if (mFile != null && mFile.length() > 44) {
            byte[] size = new byte[4];
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(mFile, "r");
                raf.seek(4);
                raf.read(size);
                int fileSize = littleEndianToDecimal(size);
                raf.seek(40);
                raf.read(size);
                int audioSize = littleEndianToDecimal(size);
                //check if this is okay
                raf.seek(44 + audioSize);
                raf.read(size);
                String tag = new String(size, StandardCharsets.US_ASCII);
                if (tag.compareTo("LIST") == 0) {
                    raf.seek(44 + audioSize + 16);
                    raf.read(size);
                    mMetadataLength = littleEndianToDecimal(size);
                    byte[] metadata = new byte[mMetadataLength];
                    raf.read(metadata);
                    return metadata;
                } else {
                    Logger.e(this.toString(), "tag was: " + tag);
                }
            } finally {
                try {
                    raf.close();
                } catch (IOException e) {
                    Logger.e(this.toString(), "IOException while closing stream", e);
                    e.printStackTrace();
                }
            }
        } else {
            Logger.e(this.toString(), "parse info failed! File not null is..." + (mFile != null) + " file length is..." + mFile.length());
        }
        return null;
    }

    public String getMetadata() throws JSONException {
        if (mMetadata == null) {
            return "";
        }
        return mMetadata.toJSON().toString();
    }

    public static JSONObject readTrackInfo(byte[] data) throws JSONException {
        String decoded = new String(data, StandardCharsets.US_ASCII);
        Logger.e("WavFile", decoded);
        JSONObject json = new JSONObject(decoded);
        return json;
    }

    int littleEndianToDecimal(byte[] header) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int value = byteBuffer.getInt();
        return value;
    }



    public static WavFile compileChapter(Project project, int chapter, List<WavFile> toCompile) {
        File root = FileNameExtractor.getDirectoryFromProject(project, chapter);
        File chap = new File(root, "chapter.wav");
        chap.delete();
        WavFile chapterWav = new WavFile(chap);
        FileOutputStream os = null;
        BufferedOutputStream bos = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            os = new FileOutputStream(chap, true);
            bos = new BufferedOutputStream(os);
            System.out.println(chap.length());
            for (WavFile wav : toCompile) {
                try {
                    byte[] buffer = new byte[5096];
                    long sizeRemaining = wav.getTotalAudioLength();
                    fis = new FileInputStream(wav.getFile());
                    bis = new BufferedInputStream(fis);
                    bis.skip(44);
                    int len;
                    while (sizeRemaining > 0) {
                        if (buffer.length < sizeRemaining) {
                            buffer = new byte[(int) sizeRemaining];
                        }
                        len = bis.read(buffer);
                        bos.write(buffer);
                        if (len == -1) {
                            break;
                        }
                        sizeRemaining -= len;
                    }
                } finally {
                    try {
                        bis.close();
                        fis.close();
                    } catch (IOException e) {
                        Logger.e("WavFile Compile Chapter", "IOException while closing input streams", e);
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Logger.e("WavFile Compiler Chapter", "FileNotFound Exception", e);
            e.printStackTrace();
        } catch (IOException e) {
            Logger.e("WavFile Compiler Chapter", "IOException", e);
            e.printStackTrace();
        } finally {
            try {
                bos.flush();
                bos.close();
                os.close();
                chapterWav.overwriteHeaderData();
            } catch (IOException e) {
                Logger.e("WavFile Compile Chapter", "IOException while closing output streams", e);
                e.printStackTrace();
            }
        }
        return chapterWav;
    }

    public static WavFile insertWavFile(WavFile base, WavFile insert, int insertIndex) throws IOException, JSONException {
        File result = null;
        FileInputStream fisBase = null;
        BufferedInputStream bisBase = null;
        FileInputStream fisInsert = null;
        BufferedInputStream bisInsert = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            result = new File(base.getFile().getAbsolutePath() + "temp.wav");

            fisBase = new FileInputStream(base.getFile());
            bisBase = new BufferedInputStream(fisBase);

            fisInsert = new FileInputStream(insert.getFile());
            bisInsert = new BufferedInputStream(fisInsert);

            fos = new FileOutputStream(result);
            bos = new BufferedOutputStream(fos);

            int oldAudioLength = base.getTotalAudioLength();
            int newAudioLength = insert.getTotalAudioLength();

            int newWritten = 0;
            int oldWritten = 0;

            for (int i = 0; i < AudioInfo.HEADER_SIZE; i++) {
                bos.write(bisBase.read());
            }
            Logger.e("WavFile", "wrote header");
            for (int i = 0; i < insertIndex; i++) {
                bos.write(bisBase.read());
                oldWritten++;
            }
            Logger.e("WavFile", "wrote before insert");
            fisInsert.skip(AudioInfo.HEADER_SIZE);
            for (int i = 0; i < newAudioLength; i++) {
                bos.write(bisInsert.read());
                newWritten++;
            }
            Logger.e("WavFile", "wrote insert");
            for (int i = insertIndex; i < oldAudioLength; i++) {
                bos.write(bisBase.read());
                oldWritten++;
            }

            //No metadata yet
            WavFileWriter.overwriteHeaderData(result, oldAudioLength + newAudioLength, 0);
            Logger.e("WavFile", "overwrote header");
            if (result.length() != AudioInfo.HEADER_SIZE + oldAudioLength + newAudioLength) {
                Logger.e("WavFile", "ERROR: resulting filesize not right. length is " + result.length() + " should be " + (AudioInfo.HEADER_SIZE + oldAudioLength + newAudioLength));
                Logger.e("WavFile", "new audio written was " + newWritten + " newAudioLength is " + newAudioLength + " old audio written was " + oldWritten + " oldAudioLength is " + oldAudioLength);
            }
        } finally {
            try {
                bos.close();
                fos.close();
                bisInsert.close();
                fisInsert.close();
                bisBase.close();
                fisBase.close();
                fisInsert.close();
            } catch (IOException e) {
                Logger.e("Insert", "IOException while closing streams", e);
                e.printStackTrace();
            }
        }

        WavFile resultWavFile = new WavFile(result, insert.getMetadata());

        return resultWavFile;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFile.getAbsolutePath());
        String metadataString = "{}";
        try {
            metadataString = mMetadata.toJSON().toString();
        } catch (JSONException e) {
            metadataString = "{}";
        }
        dest.writeString(metadataString);
    }

    public static final Parcelable.Creator<WavFile> CREATOR = new Parcelable.Creator<WavFile>() {
        public WavFile createFromParcel(Parcel in) {
            return new WavFile(in);
        }

        public WavFile[] newArray(int size) {
            return new WavFile[size];
        }
    };
}
