package wycliffeassociates.recordingapp.wav;

import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import static wycliffeassociates.recordingapp.wav.WavUtils.*;

/**
 * Created by sarabiaj on 10/4/2016.
 */
public class WavOutputStream extends OutputStream implements Closeable, AutoCloseable {

    WavFile mFile;
    OutputStream mOutputStream;
    long mAudioDataLength;

    public WavOutputStream(WavFile target) throws FileNotFoundException{
        this(target, false);
    }

    public WavOutputStream(WavFile target, boolean append) throws FileNotFoundException {
        mFile = target;
        if(mFile.getFile().length() == 0){
            mFile.initializeWavFile();
        }
        mAudioDataLength = target.getTotalAudioLength();
        //Truncate the metadata for writing
        try (FileChannel fc = new FileOutputStream(target.getFile(), append).getChannel().truncate(mAudioDataLength + HEADER_SIZE)) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOutputStream = new FileOutputStream(target.getFile(), append);
    }

//    public WavOutputStream(WavFile file, boolean buffered) throws FileNotFoundException {
//        mFile = file;
//        FileOutputStream fos = new FileOutputStream(file.getFile(), true);
//        if (buffered) {
//            mOutputStream = new BufferedOutputStream(fos);
//        }
//    }

    @Override
    public void write(int oneByte) throws IOException {
        mOutputStream.write(oneByte);
        mAudioDataLength++;

    }

    public void write(byte[] bytes) throws IOException {
        mOutputStream.write(bytes);
        mAudioDataLength += bytes.length;
    }

    public void close() throws IOException {
        mOutputStream.flush();
        mOutputStream.close();
        mFile.finishWrite(mAudioDataLength);
        updateHeader();
    }

    void updateHeader() throws IOException {
        long totalDataSize = mFile.getFile().length() - 36;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int)totalDataSize);
        RandomAccessFile raf = new RandomAccessFile(mFile.getFile(), "rw");
        raf.seek(4);
        raf.write(bb.array());
        bb.clear();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int)mAudioDataLength);
        raf.seek(40);
        raf.write(bb.array());
        raf.close();
    }
}
