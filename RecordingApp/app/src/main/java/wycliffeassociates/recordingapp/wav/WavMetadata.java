package wycliffeassociates.recordingapp.wav;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.Reporting.Logger;

import static wycliffeassociates.recordingapp.wav.WavUtils.AUDIO_LENGTH_LOCATION;
import static wycliffeassociates.recordingapp.wav.WavUtils.HEADER_SIZE;
import static wycliffeassociates.recordingapp.wav.WavUtils.labelsMatch;
import static wycliffeassociates.recordingapp.wav.WavUtils.littleEndianToDecimal;
import static wycliffeassociates.recordingapp.wav.WavUtils.seek;


/**
 * Created by sarabiaj on 10/4/2016.
 */
public class WavMetadata implements Parcelable{

    String mProject = "";
    String mLanguage = "";
    String mSource = "";
    String mSlug = "";
    String mBookNumber = "";
    String mMode = "";
    String mChapter = "";
    String mStartVerse = "";
    String mEndVerse = "";
    HashMap<Integer, WavCue> mCuePoints = new HashMap<>();

    public WavMetadata(Project p, String chapter, String startVerse, String endVerse) {
        mProject = p.getAnthology();
        mLanguage = p.getTargetLanguage();
        mSource = p.getVersion();
        mSlug = p.getSlug();
        mBookNumber = p.getBookNumber();
        mMode = p.getMode();
        mChapter = chapter;
        mStartVerse = startVerse;
        mEndVerse = endVerse;
    }

    public WavMetadata(File file){
        parseMetadata(file);
    }

    private void parseMetadata(File file){
        if (file.length() > 44) {
            byte[] word = new byte[4];
            try (RandomAccessFile raf =  new RandomAccessFile(file, "r")) {
                raf.read(word);
                if(!labelsMatch("RIFF", word)){
                    throw new RuntimeException("Attempting to load a non-Wav file.");
                }
                //read the length of the audio data
                raf.seek(AUDIO_LENGTH_LOCATION);
                raf.read(word);
                int audioLength = littleEndianToDecimal(word);
                //audio length field incorrect
                //TODO: #664
                if(audioLength <= 0 || audioLength > file.length() - HEADER_SIZE){
                    throw new RuntimeException("Audio data field reported to be " + audioLength + " while the file length is " + file.length());
                }
                //seek to the end of the header + audio data to parse metadata
                raf.seek(audioLength + HEADER_SIZE);
                boolean endOfFile = (raf.getFilePointer() >= file.length())? true : false;
                byte[] chunkName;
                //loop through all remaining chunks, if any
                while (!endOfFile) {
                    //Get chunk label
                    raf.read(word);
                    chunkName = Arrays.copyOf(word, word.length);
                    //Get chunk size
                    raf.read(word);
                    int chunkSize = littleEndianToDecimal(word);
                    byte[] chunk;
                    if(chunkSize <= file.length() - raf.getFilePointer()) {
                         chunk = new byte[chunkSize];
                    } else {
                        //TODO: #664
                        throw new RuntimeException("Chunk size larger than remaining file length; " +
                                "attempting to allocate " + chunkSize + " with remaining file size of " +
                                (file.length() - raf.getFilePointer()));
                    }
                    raf.read(chunk);
                    if(labelsMatch("LIST", chunkName)){
                        parseList(chunk);
                    } else if (labelsMatch("cue ", chunkName)){
                        parseCue(chunk);
                    } //unrecognized chunks will just be skipped
                    endOfFile = (raf.getFilePointer() >= file.length())? true : false;
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
     * @param listChunk
     */
    private void parseList(byte[] listChunk) {
        ByteBuffer chunk = ByteBuffer.wrap(listChunk);
        chunk.order(ByteOrder.LITTLE_ENDIAN);
        while (chunk.position() < chunk.capacity()) {
            //read the subchunk name
            byte[] chunkName = new byte[4];
            chunk.get(chunkName);
            if (labelsMatch("adtl", chunkName)) {
                //read the size of the subchunk
                byte[] subChunk = new byte[chunk.remaining()];
                //grab the subchunk
                chunk.get(subChunk);
                parseLabels(subChunk);
            } else if (labelsMatch("IART", chunkName)) {
                //read the size of the subchunk
                int chunkSize = chunk.getInt();
                //grab the subchunk
                byte[] subChunk = new byte[chunkSize];
                chunk.get(subChunk);
                parseTrMetadata(subChunk);
            } //else ignore and move to the next subchunk
        }
    }

    /**
     * Parses a cue chunk, which adhere to the following format:
     * "cue " (0x6375 6520)
     * size of cue chunk (4B LE)
     * number of cues (4B LE)
     * For each cue:
     *      cue ID (4B LE)
     *      location (index in PCM array considering 44100 indicies per second [so don't consider block size])
     *      "data" (0x6461 7461)
     *      0000 0000
     *      0000 0000
     *      location (again, same as above)
     *
     * NOTE This method assumes that the first 8 bytes have already been removed and parsed.
     * @param cueChunk
     */
    private void parseCue(byte[] cueChunk){
        if(cueChunk.length == 0){
            return;
        }
        ByteBuffer chunk = ByteBuffer.wrap(cueChunk);
        chunk.order(ByteOrder.LITTLE_ENDIAN);

        //get number of cues
        int numCues = chunk.getInt();

        //each cue subchunk should be 24 bytes, plus 4 for the number of cues field
        if(cueChunk.length != (24*numCues) + 4){
            return;
        }

        //For each cue, extract the cue Id and the cue location
        for(int i = 0; i < numCues; i++){
            int cueId = chunk.getInt();
            int cueLoc = chunk.getInt();

            //If the label has already been parsed, append this data to the existing object
            if(mCuePoints.containsKey(cueId)) {
                mCuePoints.get(cueId).setLocation(cueLoc);
            } else { //else create a new cue and wait for a label later
                WavCue cue = new WavCue(cueLoc);
                mCuePoints.put(cueId, cue);
            }
            //skip the rest of the cue chunk to move to the next cue
            seek(chunk, 16);
        }
    }


    private void parseLabels(byte[] labelChunk) {
        if (labelChunk.length == 0) {
            return;
        }
        byte[] word = new byte[4];
        ByteBuffer chunk = ByteBuffer.wrap(labelChunk);
        chunk.order(ByteOrder.LITTLE_ENDIAN);

        while (chunk.position() < chunk.capacity()) {
            chunk.get(word);
            if (labelsMatch("ltxt", word)) {
                int size = chunk.getInt();
                //move to skip ltxt subchunk
                seek(chunk, size);
                //read next chunk label
                chunk.get(word);
                if (labelsMatch("labl", word)) {
                    size = chunk.getInt();
                    Integer id = chunk.getInt();
                    byte[] labelBytes = new byte[size];
                    String label = new String(labelBytes, StandardCharsets.US_ASCII);
                    if (mCuePoints.containsKey(id)) {
                        mCuePoints.get(id).setLabel(label);
                    } else {
                        mCuePoints.put(id, new WavCue(label));
                    }
                    parseLabels(Arrays.copyOfRange(labelChunk, 12 + (int) size, labelChunk.length));
                }
                else {
                    //else skip over this subchunk
                    size = chunk.getInt();
                    seek(chunk, size);
                }
            }
        }
    }

    private void parseTrMetadata(byte[] trChunk){
        String metadata = new String(trChunk, StandardCharsets.US_ASCII);
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
            mProject = "";
            if (json.has("project")) {
                mProject = json.getString("project");
            }
            mLanguage = "";
            if (json.has("language")) {
                mLanguage = json.getString("language");
            }
            mSource = "";
            if (json.has("source")) {
                mSource = json.getString("source");
            }
            mSlug = "";
            if (json.has("slug")) {
                mSlug = json.getString("slug");
            }
            mBookNumber = "";
            if (json.has("book_number")) {
                mBookNumber = json.getString("book_number");
            }
            mMode = "";
            if (json.has("mode")) {
                mMode = json.getString("mode");
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
            if(json.has("markers")) {
                JSONObject markers = json.getJSONObject("markers");
                parseMarkers(markers);
            }
        }
    }

    private void parseMarkers(JSONObject markers){
        try {
            mCuePoints = new HashMap<>();
            Iterator<String> keys = markers.keys();
            while(keys.hasNext()){
                String s = keys.next();
                long position = markers.getLong(s);
                WavCue cue = new WavCue(s, position);
                mCuePoints.put(Integer.parseInt(s), cue);
            }
        } catch (JSONException e){
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
            json.put("project", mProject);
            json.put("language", mLanguage);
            json.put("source", mSource);
            json.put("slug", mSlug);
            json.put("book_number", mBookNumber);
            json.put("mode", mMode);
            json.put("chapter", mChapter);
            json.put("startv", mStartVerse);
            json.put("endv", mEndVerse);
            json.put("markers", getMarkerJsonObject());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject getMarkerJsonObject() throws JSONException{
        JSONObject json = new JSONObject();
        for (WavCue cue : mCuePoints.values()) {
            json.put(cue.getLabel(), cue.getLocation());
        }
        return json;
    }

    public byte[] createCueChunk(){
        int numCues = mCuePoints.size();
        ByteBuffer bb = ByteBuffer.allocate(12 + numCues*24);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(new String("cue ").getBytes(StandardCharsets.US_ASCII));
        //cue data size: 4 byte numCues field, 24 bytes per cue
        bb.putInt(4 + 24*numCues);
        bb.putInt(mCuePoints.size());
        for(Integer id : mCuePoints.keySet()){
            //Cue id
            bb.putInt(id);
            //Play order position- ignore, no playlists
            bb.putInt((int)mCuePoints.get(id).getLocation());
            //Data chunk label
            bb.put(new String("data").getBytes(StandardCharsets.US_ASCII));
            //chunk start- ignore, using standard data chunk
            bb.putInt(0);
            //block start- ignore since data is uncompressed
            bb.putInt(0);
            //cue position
            bb.putInt((int)mCuePoints.get(id).getLocation());
        }
        return bb.array();
    }

    public byte[] createLabelChunk(){
        int numCues = mCuePoints.size();
        int size = (numCues * 48) + 4;
        ByteBuffer bb = ByteBuffer.allocate(size + 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(new String("LIST").getBytes(StandardCharsets.US_ASCII));
        bb.putInt(size);
        bb.put(new String("adtl").getBytes(StandardCharsets.US_ASCII));
        for(Integer i : mCuePoints.keySet()) {
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

    private byte[] wordAlignedLabel(int index){
        String label = mCuePoints.get(index).getLabel();
        int alignedLength = label.length();
        if(alignedLength % 4 != 0){
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

    public WavMetadata(Parcel in){
        try {
            parseMetadataFromJson(new JSONObject(in.readString()));
        } catch (JSONException e) {
            Logger.e(this.toString(), "Unable to parse parceled metadata");
            e.printStackTrace();
        }
    }

    public void addCue(WavCue cue) {
        mCuePoints.put(Integer.parseInt(cue.getLabel()), cue);
    }

    public void removeCue(int verse){
        mCuePoints.remove(verse);
    }
}
