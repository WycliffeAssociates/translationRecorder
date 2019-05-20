package org.wycliffeassociates.translationrecorder.Playback.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.door43.tools.reporting.Logger;

import org.wycliffeassociates.translationrecorder.AudioInfo;

/**
 * Plays .Wav audio files
 */
class BufferPlayer {

    private final BufferProvider mBufferProvider;
    private final AudioTrack player;
    private Thread mPlaybackThread;
    private int minBufferSize = 0;
    private int mSessionLength;
    private BufferPlayer.OnCompleteListener mOnCompleteListener;
    private short[] mAudioShorts;


    interface OnCompleteListener {
        void onComplete();
    }

    interface BufferProvider {
        int onBufferRequested(short[] shorts);

        void onPauseAfterPlayingXSamples(int pausedHeadPosition);
    }

    BufferPlayer(AudioTrack audioTrack, int trackBufferSize, BufferProvider bp, BufferPlayer.OnCompleteListener onCompleteListener) {
        player = audioTrack;
        minBufferSize = trackBufferSize;
        mBufferProvider = bp;
        mOnCompleteListener = onCompleteListener;
        init();
    }

    BufferPlayer setOnCompleteListener(BufferPlayer.OnCompleteListener onCompleteListener) {
        mOnCompleteListener = onCompleteListener;
        init();
        return this;
    }

    synchronized void play(final int durationToPlay) throws IllegalStateException {
        if (isPlaying()) {
            return;
        }
        System.out.println("duration to play " + durationToPlay);
        mSessionLength = durationToPlay;
        player.setPlaybackHeadPosition(0);
        player.flush();
        player.setNotificationMarkerPosition(durationToPlay);
        player.play();
        mPlaybackThread = new Thread() {
            public void run() {
                //the starting position needs to beginning of the 16bit PCM data, not in the middle
                //position in the buffer keeps track of where we are for playback
                int shortsRetrieved = 1;
                int shortsWritten = 0;
                while (!mPlaybackThread.isInterrupted() && isPlaying() && shortsRetrieved > 0) {
                    shortsRetrieved = mBufferProvider.onBufferRequested(mAudioShorts);
                    shortsWritten = player.write(mAudioShorts, 0, minBufferSize);
                    switch (shortsWritten) {
                        case AudioTrack.ERROR_INVALID_OPERATION: {
                            Logger.e(this.toString(), "ERROR INVALID OPERATION");
                            break;
                        }
                        case AudioTrack.ERROR_BAD_VALUE: {
                            Logger.e(this.toString(), "ERROR BAD VALUE");
                            break;
                        }
                        case AudioTrack.ERROR: {
                            Logger.e(this.toString(), "ERROR");
                            break;
                        }
                    }
                }
                System.out.println("shorts written " + shortsWritten);
            }
        };
        mPlaybackThread.start();
    }

    void init() {
        mAudioShorts = new short[minBufferSize];
        if (mOnCompleteListener != null) {
            player.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {
                    finish();
                }

                @Override
                public void onPeriodicNotification(AudioTrack track) {
                }
            });
        }
    }

    private synchronized void finish() {
        System.out.println("marker reached");
        player.stop();
        mPlaybackThread.interrupt();
        mOnCompleteListener.onComplete();
    }

    //Simply pausing the audiotrack does not seem to allow the player to resume.
    synchronized void pause() {
        player.pause();
        int location = player.getPlaybackHeadPosition();
        System.out.println("paused at " + location);
        mBufferProvider.onPauseAfterPlayingXSamples(location);
        player.setPlaybackHeadPosition(0);
        player.flush();
    }

    boolean exists() {
        if (player != null) {
            return true;
        } else
            return false;
    }

    synchronized void stop() {
        if (isPlaying() || isPaused()) {
            player.pause();
            player.stop();
            player.flush();
            if (mPlaybackThread != null) {
                mPlaybackThread.interrupt();
            }
        }
    }

    synchronized void release() {
        stop();
        if (player != null) {
            player.release();
        }
    }

    boolean isPlaying() {
        if (player != null)
            return player.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
        else
            return false;
    }

    boolean isPaused() {
        if (player != null)
            return player.getPlayState() == AudioTrack.PLAYSTATE_PAUSED;
        else
            return false;
    }

    int getPlaybackHeadPosition() throws IllegalStateException {
        return player.getPlaybackHeadPosition();
    }

    int getDuration() {
        return 0;
    }

    int getAdjustedDuration() {
        return 0;
    }

    int getAdjustedLocation() {
        return 0;
    }

    void startSectionAt(int i) {
    }

    void seekTo(int i) {
    }

    void seekToEnd() {
    }

    void seekToStart() {
    }

    boolean checkIfShouldStop() {
        return true;
    }

    void setOnlyPlayingSection(boolean b) {
    }

    void stopSectionAt(int i) {
    }
}
