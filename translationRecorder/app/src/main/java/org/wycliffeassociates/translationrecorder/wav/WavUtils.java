package org.wycliffeassociates.translationrecorder.wav;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Created by sarabiaj on 10/5/2016.
 */
public class WavUtils {

    public static int HEADER_SIZE = 44;
    public static int AUDIO_LENGTH_LOCATION = 40;

    private WavUtils(){}

    public static int littleEndianToDecimal(byte[] header) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(header);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int value = byteBuffer.getInt();
        return value;
    }

    public static boolean labelsMatch(String label, byte[] word){
        String read = new String(word, StandardCharsets.US_ASCII);
        return label.equals(read);
    }

    static void seek(ByteBuffer byteBuffer, int seek){
        byteBuffer.position(byteBuffer.position() + seek);
    }
}
