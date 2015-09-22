package wycliffeassociates.recordingapp;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by jsarabia on 7/24/15.
 */
public class RecordingQueues {
    public static volatile BlockingQueue<RecordingMessage> UIQueue = new ArrayBlockingQueue<>(1024);
    public static volatile BlockingQueue<RecordingMessage> writingQueue = new ArrayBlockingQueue<>(1024);
    public static volatile BlockingQueue<Boolean> doneWriting = new ArrayBlockingQueue<>(1);

    private RecordingQueues(){}


}
