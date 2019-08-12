package org.wycliffeassociates.translationrecorder.wav;

import android.media.AudioFormat;
import android.os.Parcel;
import android.os.Parcelable;

import com.door43.tools.reporting.Logger;

import org.json.JSONException;
import org.wycliffeassociates.translationrecorder.AudioInfo;
import org.wycliffeassociates.translationrecorder.project.Project;
import org.wycliffeassociates.translationrecorder.project.ProjectFileUtils;

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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;

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
    WavMetadata mMetadata;
    private int mTotalAudioLength = 0;
    private int mTotalDataLength = 0;
    private int mMetadataLength = 0;

    /**
     * Loads an existing wav file and parses metadata it may have
     * @param file an existing wav file to load
     */
    public WavFile(File file) {
        mFile = file;
        mMetadata = new WavMetadata(file);
        parseHeader();
    }

    /**
     * Creates a new Wav file and initializes the header
     * @param file the path to use for creating the wav file
     * @param metadata metadata to attach to the wav file
     */
    public WavFile(File file, WavMetadata metadata) {
        mFile = file;
        initializeWavFile();
        mMetadata = metadata;
    }

    void finishWrite(int totalAudioLength) throws IOException {
        mTotalAudioLength = totalAudioLength;
        if(mMetadata != null) {
            writeMetadata(totalAudioLength);
        }
    }

    public WavFile(Parcel in) {
        mFile = new File(in.readString());
        mMetadata = in.readParcelable(WavMetadata.class.getClassLoader());
        mTotalAudioLength = in.readInt();
        mTotalDataLength = in.readInt();
        mMetadataLength = in.readInt();
    }
//
//    public WavFile(File file, String jsonMetadata) {
//        mFile = file;
//        mMetadataLength = 0;
//        mTotalAudioLength = (int) file.length() - HEADER_SIZE;
//        mTotalDataLength = (int) file.length() - 8;
//        try {
//            mMetadata = new Metadata(new JSONObject(jsonMetadata));
//            writeMetadata();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void parseChunkSizes() {
//        RandomAccessFile raf = null;
//        try {
//            raf = new RandomAccessFile(mFile, "r");
//            byte[] size = new byte[4];
//            raf.seek(4);
//            raf.read(size);
//            mTotalDataLength = littleEndianToDecimal(size);
//            raf.seek(40);
//            raf.read(size);
//            mTotalAudioLength = littleEndianToDecimal(size);
//            //check if this is okay
//            raf.seek(44 + mTotalAudioLength);
//            raf.read(size);
//            String tag = new String(size, StandardCharsets.US_ASCII);
//            if (tag.compareTo("LIST") == 0) {
//                raf.seek(44 + mTotalAudioLength + 16);
//                raf.read(size);
//                mMetadataLength = littleEndianToDecimal(size);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                raf.close();
//            } catch (IOException e) {
//                Logger.e(this.toString(), "IOException while closing stream", e);
//                e.printStackTrace();
//            }
//        }
//    }

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
        mFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(mFile, false)) {

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

    private void writeMetadata(int totalAudioLength) throws IOException {
        mTotalAudioLength = totalAudioLength;
        byte[] cueChunk = mMetadata.createCueChunk();
        byte[] labelChunk = mMetadata.createLabelChunk();
        byte[] trMetadata = mMetadata.createTrMetadataChunk();
        try (
            FileOutputStream out = new FileOutputStream(mFile, true);
            BufferedOutputStream bof = new BufferedOutputStream(out);
        ){
            //truncates existing metadata- new metadata may not be as long
            out.getChannel().truncate(HEADER_SIZE + mTotalAudioLength);
            bof.write(cueChunk);
            bof.write(labelChunk);
            bof.write(trMetadata);
        }
        mMetadataLength = cueChunk.length + labelChunk.length + trMetadata.length;
        mTotalDataLength = mTotalAudioLength + mMetadataLength + HEADER_SIZE - 8;
        overwriteHeaderData();
        return;
    }

//    private int writeMetadata(String metadata) throws IOException {
//        byte[] data = convertToMetadata(metadata);
//        BufferedOutputStream bof = null;
//        FileOutputStream out = null;
//        try {
//            out = new FileOutputStream(mFile, true);
//            //truncates existing metadata- new metadata may not be as long
//            out.getChannel().truncate(HEADER_SIZE + mTotalAudioLength);
//            bof = new BufferedOutputStream(out);
//            bof.write(data);
//        } finally {
//            try {
//                bof.close();
//                out.close();
//            } catch (IOException e) {
//                Logger.e(this.toString(), "IOException while closing streams", e);
//                e.printStackTrace();
//            }
//        }
//        mMetadataLength = data.length;
//        mTotalDataLength = mTotalAudioLength + mMetadataLength + HEADER_SIZE - 8;
//        overwriteHeaderData();
//        return data.length;
//    }

//    public static byte[] convertToMetadata(String metadata) {
//        //word align
//        int padding = metadata.length() % 4;
//        if (padding != 0) {
//            padding = 4 - padding;
//        }
//        byte[] infoTag = new byte[metadata.length() + padding + 20];
//
//        int metadataSize = metadata.length() + padding;
//        int chunkSize = 12 + metadataSize;
//
//        infoTag[0] = 'L';
//        infoTag[1] = 'I';
//        infoTag[2] = 'S';
//        infoTag[3] = 'T';
//        infoTag[4] = (byte) (chunkSize & 0xff);
//        infoTag[5] = (byte) ((chunkSize >> 8) & 0xff);
//        infoTag[6] = (byte) ((chunkSize >> 16) & 0xff);
//        infoTag[7] = (byte) ((chunkSize >> 24) & 0xff);
//        infoTag[8] = 'I';
//        infoTag[9] = 'N';
//        infoTag[10] = 'F';
//        infoTag[11] = 'O';
//        infoTag[12] = 'I'; // fmt  chunk
//        infoTag[13] = 'A';
//        infoTag[14] = 'R';
//        infoTag[15] = 'T';
//        infoTag[16] = (byte) (metadataSize & 0xff);
//        infoTag[17] = (byte) ((metadataSize >> 8) & 0xff);
//        infoTag[18] = (byte) ((metadataSize >> 16) & 0xff);
//        infoTag[19] = (byte) ((metadataSize >> 24) & 0xff);
//
//        for (int i = 20; i < metadata.length() + 20; i++) {
//            infoTag[i] = (metadata.getBytes(StandardCharsets.US_ASCII))[i - 20];
//        }
//        for (int i = metadata.length() + 20; i < infoTag.length; i++) {
//            infoTag[i] = '\0';
//        }
//        return infoTag;
//    }

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

    public void parseHeader() {
        if (mFile != null && mFile.length() >= HEADER_SIZE) {
            byte[] header = new byte[HEADER_SIZE];
            try (RandomAccessFile raf = new RandomAccessFile(mFile, "r")) {
                raf.read(header);
                ByteBuffer bb = ByteBuffer.wrap(header);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                //Skip over "RIFF"
                bb.getInt();
                mTotalDataLength = bb.getInt();
                //Seek to the audio length field
                bb.position(WavUtils.AUDIO_LENGTH_LOCATION);
                mTotalAudioLength = bb.getInt();
            } catch (FileNotFoundException e){
                Logger.e(this.toString(), "File not found", e);
                e.printStackTrace();
            } catch (IOException e) {
                Logger.e(this.toString(), "IOException while parsing header", e);
                e.printStackTrace();
            }
        }
    }

    public String getMetadataString() throws JSONException {
        if (mMetadata == null) {
            return "";
        }
        return mMetadata.toJSON().toString();
    }

    /**
     * Adds a marker to the wav file at the given position
     * Does not write to the file until commit is called.
     * @param label string for the label of the marker
     * @param position block index of the PCM array ex 44100 for 1 second
     * @return a reference to this to allow chaining with commit
     */
    public WavFile addMarker(String label, int position){
        WavCue cue = new WavCue(label, position);
        mMetadata.addCue(cue);
        return this;
    }

    public WavFile clearMarkers() {
        mMetadata.mCuePoints = new HashMap<>();
        return this;
    }

    public void commit(){
        try {
            writeMetadata(mTotalAudioLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WavMetadata getMetadata(){
        return mMetadata;
    }

    public static WavFile compileChapter(Project project, int chapter, List<WavFile> toCompile) {
        File root = ProjectFileUtils.getParentDirectory(project, chapter);
        File chap = new File(root, project.getChapterFileName(chapter));
        chap.delete();
        String chapterString = ProjectFileUtils.chapterIntToString(project, chapter);
        String startVerse = toCompile.get(0).getMetadata().getStartVerse();
        String endVerse = toCompile.get(toCompile.size()-1).getMetadata().getEndVerse();
        //chapter wav isn't particularly useful in terms of keeping it for a final product,
        // so don't worry about contributors
        WavMetadata metadata = new WavMetadata(project, "", chapterString, startVerse, endVerse);
        WavFile chapterWav = new WavFile(chap, metadata);
        int locationOffset = 0;
        for (WavFile wav : toCompile) {
            try (WavOutputStream wos = new WavOutputStream(chapterWav, true, WavOutputStream.BUFFERED)){
                List<WavCue> cues = wav.getMetadata().getCuePoints();
                for(WavCue cue : cues){
                    chapterWav.addMarker(cue.getLabel(), locationOffset + cue.getLocation());
                }
                locationOffset += wav.getTotalAudioLength()/2;
                try (FileInputStream fis = new FileInputStream(wav.getFile());
                    BufferedInputStream bis = new BufferedInputStream(fis)){
                    byte[] buffer = new byte[5096];
                    long sizeRemaining = wav.getTotalAudioLength();
                    bis.skip(WavFile.HEADER_SIZE);
                    int len;
                    while (sizeRemaining > 0) {
                        if (buffer.length < sizeRemaining) {
                            buffer = new byte[(int) sizeRemaining];
                        }
                        len = bis.read(buffer);
                        wos.write(buffer);
                        if (len == -1) {
                            break;
                        }
                        sizeRemaining -= len;
                    }
                }
            } catch (FileNotFoundException e) {
                Logger.e("WavFile Compiler Chapter", "FileNotFound Exception", e);
                e.printStackTrace();
            } catch (IOException e) {
                Logger.e("WavFile Compiler Chapter", "IOException", e);
                e.printStackTrace();
            }
        }

        return chapterWav;
    }

    public static WavFile insertWavFile(
            WavFile base,
            WavFile insert,
            int insertFrame
    ) throws IOException, JSONException {

        // Prepare new metadata
        WavMetadata newMetadata = base.getMetadata();
        for (WavCue cue: newMetadata.getCuePoints()) {
            if(cue.getLocation() >= insertFrame) {
                cue.setLocation(cue.getLocation() + (insert.getTotalAudioLength()/2));
            }
        }

        //convert to two byte PCM
        insertFrame *= 2;

        File result = new File(base.getFile().getParentFile(),"temp.wav");
        WavFile resultWav = new WavFile(result, newMetadata);

        long start = System.currentTimeMillis();

        try (
            FileInputStream fisBase = new FileInputStream(base.getFile());
            FileInputStream fisInsert = new FileInputStream(insert.getFile());

            WavOutputStream wos = new WavOutputStream(resultWav, 0);
        ) {
            MappedByteBuffer baseBuffer = fisBase.getChannel().map(
                    FileChannel.MapMode.READ_ONLY,
                    HEADER_SIZE,
                    base.getTotalAudioLength()
            );
            MappedByteBuffer insertBuffer = fisInsert.getChannel().map(
                    FileChannel.MapMode.READ_ONLY,
                    HEADER_SIZE,
                    insert.getTotalAudioLength()
            );
            int oldAudioLength = base.getTotalAudioLength();
            int newAudioLength = insert.getTotalAudioLength();

            int newWritten = 0;
            int oldWritten = 0;

            int increment = 1024 * 4;
            byte[] bytes = new byte[increment];
            //Logger.e("WavFile", "wrote header");
            for (int i = 0; i < insertFrame; i+=increment) {
                if(insertFrame - i <= increment){
                    increment = insertFrame-i;
                    bytes = new byte[increment];
                }
                baseBuffer.get(bytes);
                wos.write(bytes);
                oldWritten+=increment;
            }
            wos.flush();
            //Logger.e("WavFile", "wrote before insert");
            increment = 1024 * 4;
            bytes = new byte[increment];
            for (int i = 0; i < newAudioLength; i+=increment) {
                if(newAudioLength-i <= increment){
                    increment = newAudioLength-i;
                    bytes = new byte[increment];
                }
                insertBuffer.get(bytes);
                wos.write(bytes);
                newWritten+=increment;
            }
            wos.flush();
            //Logger.e("WavFile", "wrote insert");
            increment = 1024 * 4;
            bytes = new byte[increment];
            for (int i = insertFrame; i < oldAudioLength; i+=increment) {
                if(oldAudioLength - i <= increment){
                    increment = oldAudioLength-i;
                    bytes = new byte[increment];
                }
                baseBuffer.get(bytes);
                wos.write(bytes);
                oldWritten+=increment;
            }
            wos.flush();
            if (result.length() != AudioInfo.HEADER_SIZE + oldAudioLength + newAudioLength) {
                String errorMessage = "ERROR: resulting filesize not right. length is " + result.length()
                        + " should be " + (AudioInfo.HEADER_SIZE + oldAudioLength + newAudioLength);
                Logger.e("WavFile", errorMessage);
                String infoMessage = "new audio written was " + newWritten
                        + " newAudioLength is " + newAudioLength
                        + " old audio written was " + oldWritten
                        + " oldAudioLength is " + oldAudioLength;
                Logger.e("WavFile", infoMessage);
            }
            baseBuffer = null;
            insertBuffer = null;
            Runtime.getRuntime().gc();
        }

        return resultWav;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFile.getAbsolutePath());
        dest.writeParcelable(mMetadata, 0);
        dest.writeInt(mTotalAudioLength);
        dest.writeInt(mTotalDataLength);
        dest.writeInt(mMetadataLength);
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
