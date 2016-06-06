package wycliffeassociates.recordingapp.Recording;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Created by sarabiaj on 6/2/2016.
 */
public class WavFile {

    File mFile;

    public WavFile(File file){
        mFile = file;
    }

    public WavFile(){}

    public void writeMetadata(String metadata) throws IOException{
        byte[] data = convertToMetadata(metadata);
        FileOutputStream out = new FileOutputStream(mFile, true);
        BufferedOutputStream bof = new BufferedOutputStream(out);
        bof.write(data);
        bof.close();
        out.close();
        WavFileWriter.overwriteHeaderData(mFile, mFile.length());
    }

    public static byte[] convertToMetadata(String metadata){
        int padding = metadata.length() % 4;
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
        infoTag[15] = 'T' ;
        infoTag[24] = (byte) (metadataSize & 0xff);
        infoTag[25] = (byte) ((metadataSize >> 8) & 0xff);
        infoTag[26] = (byte) ((metadataSize >> 16) & 0xff);
        infoTag[27] = (byte) ((metadataSize >> 24) & 0xff);

        for(int i = 0; i < metadata.length(); i++){
            infoTag[i] = (metadata.getBytes(StandardCharsets.UTF_8))[i];
        }
        for(int i = metadata.length(); i < metadataSize; i++){
            infoTag[i] = '\0';
        }
        return infoTag;
    }

    private byte[] readInfoTag() throws IOException{
        if(mFile != null && mFile.length() > 44){
            byte[] size = new byte[4];
            RandomAccessFile raf = new RandomAccessFile(mFile, "r");
            raf.seek(4);
            raf.read(size);
            int fileSize = littleEndianToDecimal(size, 0, 4);
            raf.seek(40);
            raf.read(size);
            int audioSize = littleEndianToDecimal(size, 0, 4);
            //check if this is okay
            raf.seek(44 + audioSize);
            raf.read(size);
            String tag = new String(size, "UTF-8");
            if(tag.compareTo("LIST") == 0){
                raf.seek(44 + audioSize + 24);
                raf.read(size);
                int metadataSize = littleEndianToDecimal(size, 0, 4);
                byte[] metadata = new byte[metadataSize];
                raf.read(metadata);
                return metadata;
            }
        }
        return null;
    }

    public static JSONObject readTrackInfo(byte[] data){
        try {
            String decoded = new String(data, "UTF-8");
            JSONObject json = new JSONObject(decoded);
            return json;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    int littleEndianToDecimal(byte[] header, int loc, int n){
        int sum = 0;
        for(int i = 0; i < n; i++){
            //can just shift in without masking because header is unsigned
            sum |= (header[loc+i] << (Byte.SIZE*i)) & 0xff;
        }
        return sum;
    }
}
