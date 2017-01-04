package org.wycliffeassociates.translationrecorder.wav;

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

/**
 * Created by sarabiaj on 10/4/2016.
 */
public class WavOutputStream extends OutputStream implements Closeable, AutoCloseable {

    WavFile mFile;
    OutputStream mOutputStream;
    BufferedOutputStream mBos;
    int mAudioDataLength;
    boolean mBuffered = false;

    public static final int BUFFERED = 1;

    public WavOutputStream(WavFile target) throws FileNotFoundException {
        this(target, false, 0);
    }

    public WavOutputStream(WavFile target, int flag) throws FileNotFoundException {
        this(target, false, flag);
    }

    public WavOutputStream(WavFile target, boolean append) throws FileNotFoundException {
        this(target, append, 0);
    }

    public WavOutputStream(WavFile target, boolean append, int flag) throws FileNotFoundException {
        mFile = target;
        if (mFile.getFile().length() == 0) {
            mFile.initializeWavFile();
        }
        mAudioDataLength = target.getTotalAudioLength();
        //Truncate the metadata for writing
        //if appending, then truncate metadata following the audio length, otherwise truncate after the header
        int whereToTruncate = (append) ? mAudioDataLength : 0;
        try (FileChannel fc = new FileOutputStream(target.getFile(), true).getChannel().truncate(whereToTruncate + WavUtils.HEADER_SIZE)) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        //always need to use append to continue writing after the header rather than overwriting it
        mOutputStream = new FileOutputStream(target.getFile(), true);
        if (flag == BUFFERED) {
            mBos = new BufferedOutputStream(mOutputStream);
            mBuffered = true;
        }
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
        if (mBuffered) {
            mBos.write(oneByte);
        } else {
            mOutputStream.write(oneByte);
        }
        mAudioDataLength++;
    }
    
    @Override
    public void flush() throws IOException {
        if (mBuffered) {
            mBos.flush();
        }
        mOutputStream.flush();
    }

    public void write(byte[] bytes) throws IOException {
        if (mBuffered) {
            mBos.write(bytes);
        } else {
            mOutputStream.write(bytes);
        }
        mAudioDataLength += bytes.length;
    }

    public void close() throws IOException {
        if (mBuffered) {
            mBos.flush();
        }
        mOutputStream.flush();
        mOutputStream.close();
        mFile.finishWrite(mAudioDataLength);
        updateHeader();
    }

    void updateHeader() throws IOException {
        long totalDataSize = mFile.getFile().length() - 36;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) totalDataSize);
        RandomAccessFile raf = new RandomAccessFile(mFile.getFile(), "rw");
        raf.seek(4);
        raf.write(bb.array());
        bb.clear();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(mAudioDataLength);
        raf.seek(40);
        raf.write(bb.array());
        raf.close();
    }
}
