package wycliffeassociates.recordingapp.Playback;

import android.os.Handler;
import android.os.Looper;

import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.Playback.interfaces.AudioStateCallback;
import wycliffeassociates.recordingapp.Playback.interfaces.MediaControlReceiver;
import wycliffeassociates.recordingapp.Playback.player.WavPlayer;
import wycliffeassociates.recordingapp.WavFileLoader;
import wycliffeassociates.recordingapp.wav.WavCue;
import wycliffeassociates.recordingapp.wav.WavFile;

/**
 * Created by sarabiaj on 10/27/2016.
 */

public class AudioVisualController implements MediaControlReceiver {

    WavPlayer mPlayer;
    MappedByteBuffer mAudio;
    CutOp mCutOp = new CutOp();

    AudioStateCallback mCallback;

    Handler mHandler;
    private List<WavCue> mCues;
    private int durationInFrames;

    public AudioVisualController(final AudioStateCallback callback, final WavFile wav) {

        mCallback = callback;

        initPlayer(wav);

        mHandler = new Handler(Looper.getMainLooper());

        mPlayer.setOnCompleteListener(new WavPlayer.OnCompleteListener() {
            @Override
            public void onComplete() {
                mCallback.onPlayerPaused();
            }
        });

//        mDropStartMarkerBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPlayer.setLoopStart(mPlayer.getLocationInFrames());
//                swapViews(new View[]{mDropEndMarkerBtn}, new View[]{mDropStartMarkerBtn});
//            }
//        });
//
//        mDropEndMarkerBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPlayer.setLoopEnd(mPlayer.getLocationInFrames());
//                swapViews(new View[]{mClearBtn, mCutBtn}, new View[]{mDropEndMarkerBtn});
//            }
//        });
//
//        mClearBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPlayer.clearLoopPoints();
//                swapViews(new View[]{mDropStartMarkerBtn}, new View[]{mClearBtn, mCutBtn});
//            }
//        });
//
//        mCutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCutOp.cut(mPlayer.getLoopStart(), mPlayer.getLoopEnd());
//                mPlayer.clearLoopPoints();
//                swapViews(new View[]{mUndoBtn}, new View[]{mCutBtn});
//            }
//        });
    }

    private void initPlayer(WavFile wav) {
        WavFileLoader loader = new WavFileLoader(wav);

        mCues = wav.getMetadata().getCuePoints();
        if (mCues != null) {
            sortCues(mCues);
        }
        mAudio = loader.getMappedAudioFile();
        ShortBuffer mAudioShort = mAudio.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        mPlayer = new WavPlayer(mAudioShort, mCutOp, mCues);
    }

    private void sortCues(List<WavCue> cues) {
        Collections.sort(cues, new Comparator<WavCue>() {
            @Override
            public int compare(WavCue lhs, WavCue rhs) {
                return Integer.compare(lhs.getLocation(), rhs.getLocation());
            }
        });
    }

    public void play() {
        mPlayer.play();
    }

    public void pause() {
        mPlayer.pause();
    }

    public void seekNext(){
        mPlayer.seekNext();
    }

    public void seekPrevious(){
        mPlayer.seekPrevious();
    }

    public void seekTo(int frame){
        mPlayer.seekTo(frame);
    }

    @Override
    public int getLocation() {
        return mPlayer.getLocationMs();
    }

    public int getLocationInFrames(){
        return mPlayer.getLocationInFrames();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public void cut(){
        mCutOp.cut(mPlayer.getLoopStart(), mPlayer.getLoopEnd());
        mPlayer.clearLoopPoints();
    }

    public void dropStartMarker(){
        mPlayer.setLoopStart(mPlayer.getLocationInFrames());
    }

    public void dropEndMarker(){
        mPlayer.setLoopEnd(mPlayer.getLocationInFrames());
    }

    public void dropVerseMarker(String label, int location){
        mCues.add(new WavCue(label, location));
    }

    public void clearLoopPoints(){
        System.out.println("cues " + mCues.size());
        mPlayer.clearLoopPoints();
        System.out.println("cues " + mCues.size());
    }

    public void undo(){
        if(mCutOp.hasCut()) {
            mCutOp.undo();
        }
    }

    public int getLoopStart(){
        return mPlayer.getLoopStart();
    }

    public int getLoopEnd(){
        return mPlayer.getLoopEnd();
    }

    public void scrollAudio(float distX){
        int seekTo = Math.max(Math.min((int)((distX * 230) + mPlayer.getLocationInFrames()), mPlayer.getDurationInFrames()), 0);
        mPlayer.seekTo(seekTo);
        mCallback.onLocationUpdated(getLocation());
    }

    public void setStartMarker(int location) {
        mPlayer.setLoopStart(Math.max(location, 0));
    }

    public void setEndMarker(int location) {
        mPlayer.setLoopEnd(Math.min(location, mPlayer.getDurationInFrames()));
    }

    public int getDurationInFrames() {
        return mPlayer.getDurationInFrames();
    }
}
