package wycliffeassociates.recordingapp.Recording;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Created by sarabiaj on 6/2/2016.
 */
public class WavFile {

    File mFile;

    public WavFile(File file){
        mFile = file;
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
            infoTag[i] = (metadata.getBytes(StandardCharsets.US_ASCII))[i];
        }
        for(int i = metadata.length(); i < metadataSize; i++){
            infoTag[i] = '\0';
        }
        return infoTag;
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
