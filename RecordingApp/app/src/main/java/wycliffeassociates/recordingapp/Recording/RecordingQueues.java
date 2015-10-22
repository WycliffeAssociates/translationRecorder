package wycliffeassociates.recordingapp.Recording;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jsarabia on 7/24/15.
 */
public class RecordingQueues {
    public static volatile BlockingQueue<RecordingMessage> UIQueue = new ArrayBlockingQueue<>(32768);
    public static volatile BlockingQueue<RecordingMessage> writingQueue = new ArrayBlockingQueue<>(32768);
    public static volatile BlockingQueue<RecordingMessage> compressionQueue = new ArrayBlockingQueue<>(32768);
    public static volatile BlockingQueue<Boolean> doneWriting = new ArrayBlockingQueue<>(1);
    public static volatile BlockingQueue<Boolean> doneWritingCompressed = new ArrayBlockingQueue<>(1);


    private RecordingQueues(){}


}
