package org.wycliffeassociates.translationrecorder.utilities;

/**
 * Created by sarabiaj on 2/21/2017.
 */

public class RingBuffer {

    private int mHead = 0;
    private int mTail = 0;
    private float[] mBuffer;

    public RingBuffer(int capacity) {
        mBuffer = new float[capacity];
    }

    public void add(float i) {
        mBuffer[mTail] = i;
        mTail = (mTail + 1) % mBuffer.length;
        if(mHead == mTail) {
            mHead = (mHead + 1) % mBuffer.length;
        }
    }

    public void clear() {
        mHead = 0;
        mTail = 0;
    }

    public float get(int i) {
        int index = (mHead + i) % mBuffer.length;
        return mBuffer[index];
    }

    public boolean isEmpty() {
        return mHead == mTail;
    }

    public int size() {
        if(mHead == 0 && mTail < mBuffer.length) {
            return mTail;
        } else {
            return mBuffer.length;
        }
    }

    public float[] getArray() {
        float[] buffer = new float[size()*4];
        for (int i = 0; i < size(); i+= 2) {
            buffer[(i*4)] = i;
            buffer[(i*4)+1] = get(i);
            buffer[(i*4)+2] = i;
            buffer[(i*4)+3] = get(i+1);
        }
        return buffer;
    }

    public float[] scaleAndGetArray(float scalingFactor) {
        float[] buffer = new float[size()*4];
        for (int i = 0; i < size(); i+= 2) {
            buffer[(i*4)] = i;
            buffer[(i*4)+1] = scalingFactor * get(i);
            buffer[(i*4)+2] = i;
            buffer[(i*4)+3] = scalingFactor * get(i+1);
        }
        return buffer;
    }
}
