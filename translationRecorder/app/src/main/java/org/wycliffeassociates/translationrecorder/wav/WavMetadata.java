package org.wycliffeassociates.translationrecorder.wav;

import android.os.Parcel;
import android.os.Parcelable;

import com.door43.tools.reporting.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.wycliffeassociates.translationrecorder.project.Project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Created by sarabiaj on 10/4/2016.
 */
public class WavMetadata implements Parcelable {

    private final int SIZE_OF_LABEL = 4;

    String mLanguage = "";
    String mResource = "";
    String mAnthology = "";
    String mVersion = "";
    String mBookNumber = "";
    String mBook = "";
    String mMode = "";
    String mChapter = "";
    String mStartVerse = "";
    String mEndVerse = "";
    String mSourceLanguage = "";
    String mContributor = "";

    HashMap<Integer, WavCue> mCuePoints = new HashMap<>();

    public WavMetadata(Project p, String contributor, String chapter, String startVerse, String endVerse) {
        mAnthology = p.getAnthologySlug();
        mLanguage = p.getTargetLanguageSlug();
        mVersion = p.getVersionSlug();
        mBook = p.getBookSlug();
        mBookNumber = p.getBookNumber();
        mMode = p.getModeSlug().toLowerCase();
        mChapter = chapter;
        mStartVerse = startVerse;
        mEndVerse = endVerse;
        mContributor = contributor;
    }

    public WavMetadata(File file) {
        parseMetadata(file);
    }

    public String getAnthology(){return mAnthology;}
    public String getLanguage(){return mLanguage;}
    public String getVersion(){return mVersion;}
    public String getSlug(){return mBook;}
    public String getBookNumber(){return mBookNumber;}
    public String getModeSlug(){return mMode;}
    public String getChapter(){return mChapter;}
    public String getStartVerse(){return mStartVerse;}
    public String getEndVerse(){return mEndVerse;}
    public String getContributor() {
        return mContributor;
    }

    private String readLabel(ByteBuffer buffer) {
        if (buffer.remaining() >= SIZE_OF_LABEL) {
            byte[] label = new byte[4];
            buffer.get(label);
            return new String(label, StandardCharsets.US_ASCII);
        } else {
            return "";
        }
    }

    public List<WavCue> getCuePoints(){
        List<WavCue> cues = new ArrayList<>();
        for(WavCue cue : mCuePoints.values()){
            cues.add(cue);
        }
        return cues;
    }

    private void parseMetadata(File file) {
        if (file.length() > 44) {
            byte[] word = new byte[4];
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.read(word);
                if (!WavUtils.labelsMatch("RIFF", word)) {
                    throw new RuntimeException("Attempting to load a non-Wav file.");
                }
                //read the length of the audio data
                raf.seek(WavUtils.AUDIO_LENGTH_LOCATION);
                raf.read(word);
                int audioLength = WavUtils.littleEndianToDecimal(word);
                //audio length field incorrect
                //TODO: #664
                if (audioLength < 0 || audioLength > file.length() - WavUtils.HEADER_SIZE) {
                    throw new IllegalArgumentException("Audio data field reported to be " + audioLength + " while the file length is " + file.length());
                }
                //seek to the end of the header + audio data to parse metadata
                raf.seek(audioLength + WavUtils.HEADER_SIZE);
                byte[] metadata = new byte[(int) (file.length() - audioLength - WavUtils.HEADER_SIZE)];
                raf.read(metadata);
                ByteBuffer bb = ByteBuffer.wrap(metadata).order(ByteOrder.LITTLE_ENDIAN);
                //loop through all remaining chunks, if any
                while (bb.remaining() > 8) {
                    String label = readLabel(bb);
                    int chunkSize = bb.getInt();
                    ByteBuffer chunk;
                    if (chunkSize <= bb.remaining()) {
                        chunk = (ByteBuffer) bb.slice().order(ByteOrder.LITTLE_ENDIAN).limit(chunkSize);
                        WavUtils.seek(bb, chunkSize);
                    } else {
                        //TODO: #664
                        throw new IllegalArgumentException("ChunkPlugin size larger than remaining file length; " +
                                "attempting to allocate " + chunkSize + " with remaining file size of " +
                                (file.length() - bb.remaining()));
                    }
                    if (new String("LIST").equals(label)) {
                        parseList(chunk);
                    } else if (new String("cue ").equals(label)) {
                        parseCue(chunk);
                    } //unrecognized chunks will just be skipped
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Parses a list chunk. A label of "adtl" signifies a label chunk, and "IART" is where TR stores
     * its metadata. Other labels should be ignored in TR.
     *
     * @param chunk
     */
    private void parseList(ByteBuffer chunk) {
        while (chunk.position() < chunk.limit()) {
            //read the subchunk name
            String label = readLabel(chunk);
            if (new String("adtl").equals(label)) {
                //read the size of the subchunk
                parseLabels(chunk.slice().order(ByteOrder.LITTLE_ENDIAN));
                return;
            } else if (new String("INFO").equals(label)) {
                label = readLabel(chunk);
                //read the size of the subchunk
                int chunkSize = chunk.getInt();
                if(chunkSize > chunk.remaining()){
                    //TODO: #664
                    throw new RuntimeException("Subchunk size of list is invalid, size is " + chunkSize + "but only " + chunk.remaining() + " remains");
                }
                if (new String("IART").equals(label)) {
                    byte[] trMetadata = new byte[chunkSize];
                    chunk.get(trMetadata);
                    parseTrMetadata(trMetadata);
                } else {
                    //else ignore and move to the next subchunk
                    WavUtils.seek(chunk, chunkSize);
                }
            }
        }
    }

    /**
     * Parses a cue chunk, which adhere to the following format:
     * "cue " (0x6375 6520)
     * size of cue chunk (4B LE)
     * number of cues (4B LE)
     * For each cue:
     * cue ID (4B LE)
     * location (index in PCM array considering 44100 indicies per second [so don't consider block size])
     * "data" (0x6461 7461)
     * 0000 0000
     * 0000 0000
     * location (again, same as above)
     * <p/>
     * NOTE This method assumes that the first 8 bytes have already been removed and parsed.
     *
     * @param chunk
     */
    private void parseCue(ByteBuffer chunk) {
        if(!chunk.hasRemaining()){
            return;
        }
        //get number of cues
        int numCues = chunk.getInt();

        //each cue subchunk should be 24 bytes, plus 4 for the number of cues field
        if (chunk.remaining() != (24 * numCues)) {
            //TODO: #664
            return;
        }

        //For each cue, extract the cue Id and the cue location
        for (int i = 0; i < numCues; i++) {
            int cueId = chunk.getInt();
            int cueLoc = chunk.getInt();

            //If the label has already been parsed, append this data to the existing object
            if (mCuePoints.containsKey(cueId)) {
                mCuePoints.get(cueId).setLocation(cueLoc);
            } else { //else create a new cue and wait for a label later
                WavCue cue = new WavCue(cueLoc);
                mCuePoints.put(cueId, cue);
            }
            //skip the rest of the cue chunk to move to the next cue
            WavUtils.seek(chunk, 16);
        }
    }


    private void parseLabels(ByteBuffer chunk) {
        while (chunk.hasRemaining()) {
            if (new String("ltxt").equals(readLabel(chunk))) {
                int size = chunk.getInt();
                //move to skip ltxt subchunk
                WavUtils.seek(chunk, size);
                if (new String("labl").equals(readLabel(chunk))) {
                    size = chunk.getInt();
                    Integer id = chunk.getInt();
                    byte[] labelBytes = new byte[size-4];
                    chunk.get(labelBytes);
                    //trim necessary to strip trailing 0's used to pad to word align
                    String label = new String(labelBytes, StandardCharsets.US_ASCII).trim();
                    if (mCuePoints.containsKey(id)) {
                        mCuePoints.get(id).setLabel(label);
                    } else {
                        mCuePoints.put(id, new WavCue(label));
                    }
                } else {
                    //else skip over this subchunk
                    size = chunk.getInt();
                    WavUtils.seek(chunk, size);
                }
            }
        }
    }

    private void parseTrMetadata(byte[] trChunk) {
        String metadata = new String(trChunk, StandardCharsets.US_ASCII).trim();
        try {
            JSONObject json = new JSONObject(metadata);
            parseMetadataFromJson(json);
        } catch (JSONException e) {
            Logger.e(this.toString(), "Tried to parse TR metadata and threw JSONException.", e);
            return;
        }
    }

    /**
     * Loads the user profile from json
     *
     * @param json
     * @return
     * @throws Exception
     */
    public void parseMetadataFromJson(JSONObject json) throws JSONException {
        if (json != null) {
            mAnthology = "";
            if (json.has("anthology")) {
                mAnthology = json.getString("anthology");
            }
            mLanguage = "";
            if (json.has("language")) {
                mLanguage = json.getString("language");
            }
            mVersion = "";
            if (json.has("version")) {
                mVersion = json.getString("version");
            }
            mBook = "";
            if (json.has("book")) {
                mBook = json.getString("book");
            }
            mBookNumber = "";
            if (json.has("book_number")) {
                mBookNumber = json.getString("book_number");
            }
            mMode = "";
            if (json.has("mode")) {
                mMode = json.getString("mode").toLowerCase();
            }
            mChapter = "";
            if (json.has("chapter")) {
                mChapter = json.getString("chapter");
            }
            mStartVerse = "";
            if (json.has("startv")) {
                mStartVerse = json.getString("startv");
            }
            mEndVerse = "";
            if (json.has("endv")) {
                mEndVerse = json.getString("endv");
            }
            mContributor = "";
            if (json.has("contributor")) {
                mContributor = json.getString("contributor");
            }
            if (json.has("markers")) {
                JSONObject markers = json.getJSONObject("markers");
                parseMarkers(markers);
            }
        }
    }

    private void parseMarkers(JSONObject markers) {
        try {
            mCuePoints = new HashMap<>();
            Iterator<String> keys = markers.keys();
            while (keys.hasNext()) {
                String s = keys.next();
                int position = markers.getInt(s);
                WavCue cue = new WavCue(s, position);
                addCue(cue);
            }
        } catch (JSONException e) {
        }
    }

    /**
     * Returns the profile represented as a json object
     *
     * @return
     */
    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("anthology", mAnthology);
            json.put("language", mLanguage);
            json.put("version", mVersion);
            json.put("book", mBook);
            json.put("book_number", mBookNumber);
            json.put("mode", mMode);
            json.put("chapter", mChapter);
            json.put("startv", mStartVerse);
            json.put("endv", mEndVerse);
            json.put("contributor", mContributor);
            json.put("markers", getMarkerJsonObject());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject getMarkerJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        for (WavCue cue : mCuePoints.values()) {
            json.put(cue.getLabel(), cue.getLocation());
        }
        return json;
    }

    public byte[] createCueChunk() {
        int numCues = mCuePoints.size();
        ByteBuffer bb = ByteBuffer.allocate(12 + numCues * 24);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(new String("cue ").getBytes(StandardCharsets.US_ASCII));
        //cue data size: 4 byte numCues field, 24 bytes per cue
        bb.putInt(4 + 24 * numCues);
        bb.putInt(mCuePoints.size());
        for (Integer id : mCuePoints.keySet()) {
            //Cue id
            bb.putInt(id);
            //Play order position- ignore, no playlists
            bb.putInt((int) mCuePoints.get(id).getLocation());
            //Data chunk label
            bb.put(new String("data").getBytes(StandardCharsets.US_ASCII));
            //chunk start- ignore, using standard data chunk
            bb.putInt(0);
            //block start- ignore since data is uncompressed
            bb.putInt(0);
            //cue position
            bb.putInt((int) mCuePoints.get(id).getLocation());
        }
        return bb.array();
    }

    public byte[] createLabelChunk() {
        int numCues = mCuePoints.size();
        int size = (numCues * 40) + 4 + computeTextSize();
        ByteBuffer bb = ByteBuffer.allocate(size + 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(new String("LIST").getBytes(StandardCharsets.US_ASCII));
        bb.putInt(size);
        bb.put(new String("adtl").getBytes(StandardCharsets.US_ASCII));
        for (Integer i : mCuePoints.keySet()) {
            bb.put(new String("ltxt").getBytes(StandardCharsets.US_ASCII));
            bb.putInt(20);
            bb.putInt(i);
            bb.putInt(0);
            bb.put(new String("rvn ").getBytes(StandardCharsets.US_ASCII));
            bb.putInt(0);
            bb.putInt(0);
            bb.put(new String("labl").getBytes(StandardCharsets.US_ASCII));
            byte[] label = wordAlignedLabel(i);
            bb.putInt(4 + label.length);
            bb.putInt(i);
            bb.put(label);
        }
        return bb.array();
    }

    private int computeTextSize(){
        int total = 0;
        for(Integer i : mCuePoints.keySet()){
            int length = mCuePoints.get(i).getLabel().length();
            total += getWordAlignedLength(length);
        }
        return total;
    }

    private int getWordAlignedLength(int length){
        if (length % 4 != 0) {
            length += 4 - (length % 4);
        }
        return length;
    }

    private byte[] wordAlignedLabel(int index) {
        String label = mCuePoints.get(index).getLabel();
        int alignedLength = label.length();
        if (alignedLength % 4 != 0) {
            alignedLength += 4 - (alignedLength % 4);
        }
        byte[] alignedLabel = Arrays.copyOf(label.getBytes(), alignedLength);
        return alignedLabel;
    }

    public byte[] createTrMetadataChunk() {
        String metadata = this.toJSON().toString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String metadataString = this.toJSON().toString();
        dest.writeString(metadataString);
    }

    public static final Parcelable.Creator<WavMetadata> CREATOR = new Parcelable.Creator<WavMetadata>() {
        public WavMetadata createFromParcel(Parcel in) {
            return new WavMetadata(in);
        }

        public WavMetadata[] newArray(int size) {
            return new WavMetadata[size];
        }
    };

    public WavMetadata(Parcel in) {
        try {
            parseMetadataFromJson(new JSONObject(in.readString()));
        } catch (JSONException e) {
            Logger.e(this.toString(), "Unable to parse parceled metadata");
            e.printStackTrace();
        }
    }

    public void addCue(WavCue cue) {
        for(WavCue q : mCuePoints.values()) {
            if(q.getLabel().equals(cue.getLabel())){
                q.setLocation(cue.getLocation());
                return;
            }
        }
        Set<Integer> keys = mCuePoints.keySet();
        int newKey = 0;
        for(Integer i : keys){
            newKey = Math.max(newKey, i);
        }
        newKey++;
        mCuePoints.put(Integer.parseInt(cue.getLabel()), cue);
    }
}
