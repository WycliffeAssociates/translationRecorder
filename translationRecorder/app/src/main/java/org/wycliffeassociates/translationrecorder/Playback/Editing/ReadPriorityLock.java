package org.wycliffeassociates.translationrecorder.Playback.Editing;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sarabiaj on 1/6/2017.
 */

public class ReadPriorityLock {

    AtomicBoolean mWriteRequest = new AtomicBoolean(false);
    AtomicInteger mReaders = new AtomicInteger(0);

    public ReadPriorityLock() {
    }

    public void readLock() throws InterruptedException {
        while (true) {
            if(mWriteRequest.compareAndSet(false, false)) {
                mReaders.getAndIncrement();
                //need to try again, incase a writer requested after the check and before the reader increment
                if(mWriteRequest.compareAndSet(false, false)) {
                    return;
                } else {
                    //backoff before trying again
                    mReaders.getAndDecrement();
                }
            }
            Thread.sleep(10);
        }
    }

    public void readUnlock() {
        mReaders.getAndDecrement();
    }

    public void writeLock() throws InterruptedException {
        mWriteRequest.set(true);
        while (!mReaders.compareAndSet(0, 0)) {
            Thread.sleep(10);
        }
    }

    public void writeUnlock() {
        mWriteRequest.set(false);
    }
}