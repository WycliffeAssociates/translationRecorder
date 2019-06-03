package org.wycliffeassociates.translationrecorder.Playback;

import android.content.Context;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

import org.wycliffeassociates.translationrecorder.Playback.Editing.CutOp;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.AudioStateCallback;
import org.wycliffeassociates.translationrecorder.Playback.interfaces.MediaControlReceiver;
import org.wycliffeassociates.translationrecorder.Playback.player.WavPlayer;
import org.wycliffeassociates.translationrecorder.WavFileLoader;
import org.wycliffeassociates.translationrecorder.wav.WavCue;
import org.wycliffeassociates.translationrecorder.wav.WavFile;

import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sarabiaj on 10/27/2016.
 */

public class AudioVisualController implements MediaControlReceiver {

    WavPlayer mPlayer;
    CutOp mCutOp = new CutOp();

    AudioStateCallback mCallback;

    Handler mHandler;
    private List<WavCue> mCues;
    private int durationInFrames;
    WavFileLoader mWavLoader;

    public AudioVisualController(final AudioTrack audioTrack, final int trackBufferSize, final AudioStateCallback callback, final WavFile wav, Context ctx) throws IOException {
        mCallback = callback;
        initPlayer(audioTrack, trackBufferSize, wav, ctx);
        mHandler = new Handler(Looper.getMainLooper());
        mPlayer.setOnCompleteListener(new WavPlayer.OnCompleteListener() {
            @Override
            public void onComplete() {
                mCallback.onPlayerPaused();
            }
        });
    }

    public void setCueList(List<WavCue> cueList) {
        mCues = cueList;
        mPlayer.setCueList(cueList);
    }

    private void initPlayer(final AudioTrack audioTrack, final int trackBufferSize, WavFile wav, Context ctx) throws IOException {
        mWavLoader = new WavFileLoader(wav, ctx);
        mWavLoader.setOnVisualizationFileCreatedListener(new WavFileLoader.OnVisualizationFileCreatedListener() {
            @Override
            public void onVisualizationCreated(ShortBuffer mappedVisualizationFile) {
                mCallback.onVisualizationLoaded(mappedVisualizationFile);
            }
        });
        mCues = wav.getMetadata().getCuePoints();
        if (mCues != null) {
            sortCues(mCues);
        }
        mPlayer = new WavPlayer(audioTrack, trackBufferSize, mWavLoader.mapAndGetAudioBuffer(), mCutOp, mCues);
    }

    private void sortCues(List<WavCue> cues) {
        Collections.sort(cues, new Comparator<WavCue>() {
            @Override
            public int compare(WavCue lhs, WavCue rhs) {
                return Integer.compare(lhs.getLocation(), rhs.getLocation());
            }
        });
    }

    public void play() throws IllegalStateException {
        mPlayer.play();
    }

    public void pause() {
        mPlayer.pause();
    }

    public void seekNext() throws IllegalStateException {
        mPlayer.seekNext();
    }

    public void seekPrevious() throws IllegalStateException {
        mPlayer.seekPrevious();
    }

    public void seekTo(int frame) {
        mPlayer.seekToAbsolute(frame);
    }

    @Override
    public int getAbsoluteLocationMs() throws IllegalStateException {
        return mPlayer.getAbsoluteLocationMs();
    }

    public int getAbsoluteLocationInFrames() throws IllegalStateException {
        return mPlayer.getAbsoluteLocationInFrames();
    }

    public int getRelativeLocationMs() throws IllegalStateException {
        return mPlayer.getRelativeLocationMs();
    }

    public int getRelativeLocationInFrames() throws IllegalStateException {
        return mPlayer.getRelativeLocationInFrames();
    }

    @Override
    public int getRelativeDurationMs() throws IllegalStateException {
        return mPlayer.getRelativeDurationMs();
    }

    public int getAbsoluteDurationInFrames() throws IllegalStateException {
        return mPlayer.getAbsoluteDurationInFrames();
    }

    public int getRelativeDurationInFrames() throws IllegalStateException {
        return mPlayer.getRelativeDurationInFrames();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void cut() {
        mCutOp.cut(mPlayer.getLoopStart(), mPlayer.getLoopEnd());
        mPlayer.clearLoopPoints();
    }

    public CutOp getCutOp() {
        return mCutOp;
    }

    public void dropStartMarker() throws IllegalStateException {
        mPlayer.setLoopStart(mPlayer.getAbsoluteLocationInFrames());
    }

    public void dropEndMarker() throws IllegalStateException {
        mPlayer.setLoopEnd(mPlayer.getAbsoluteLocationInFrames());
    }

    public void dropVerseMarker(String label, int location) {
        mCues.add(new WavCue(label, location));
    }

    public void clearLoopPoints() {
        System.out.println("cues " + mCues.size());
        mPlayer.clearLoopPoints();
        System.out.println("cues " + mCues.size());
    }

    public void undo() {
        if (mCutOp.hasCut()) {
            mCutOp.undo();
        }
    }

    public int getLoopStart() {
        return mPlayer.getLoopStart();
    }

    public int getLoopEnd() {
        return mPlayer.getLoopEnd();
    }

    public void scrollAudio(float distX) throws IllegalStateException {
        int seekTo = Math.max(Math.min((int) ((distX * 230) + mPlayer.getAbsoluteLocationInFrames()), mPlayer.getAbsoluteDurationInFrames()), 0);
        if (distX > 0) {
            int skip = mCutOp.skip(seekTo);
            if (skip != -1) {
                seekTo = skip + 1;
            }
        } else {
            int skip = mCutOp.skipReverse(seekTo);
            if (skip != Integer.MAX_VALUE) {
                seekTo = skip - 1;
            }
        }
        mPlayer.seekToAbsolute(seekTo);
        mCallback.onLocationUpdated();
    }

    public void setStartMarker(int relativeLocation) throws IllegalStateException {
        mPlayer.setLoopStart(Math.max(mCutOp.relativeLocToAbsolute(relativeLocation, false), 0));
    }

    public void setEndMarker(int relativeLocation) throws IllegalStateException {
        mPlayer.setLoopEnd(Math.min(mCutOp.relativeLocToAbsolute(relativeLocation, false), mPlayer.getAbsoluteDurationInFrames()));
    }

    public WavFileLoader getWavLoader() {
        return mWavLoader;
    }
}