package org.wycliffeassociates.translationrecorder.Playback.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import org.wycliffeassociates.translationrecorder.AudioInfo;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;

/**
 * Plays .Wav audio files
 */
class BufferPlayer {

    private final BufferProvider mBufferProvider;
    private AudioTrack player = null;
    private Thread mPlaybackThread;
    private int minBufferSize = 0;
    private int mSessionLength;
    private BufferPlayer.OnCompleteListener mOnCompleteListener;
    private short[] mAudioShorts;


    interface OnCompleteListener{
        void onComplete();
    }

    interface BufferProvider {
        int onBufferRequested(short[] shorts);
        void onPauseAfterPlayingXSamples(int pausedHeadPosition);
    }

    BufferPlayer(BufferProvider bp) {
        mBufferProvider = bp;
        init();
    }

    BufferPlayer(BufferProvider bp, BufferPlayer.OnCompleteListener onCompleteListener) {
        mBufferProvider = bp;
        mOnCompleteListener = onCompleteListener;
        init();
    }

    BufferPlayer setOnCompleteListener(BufferPlayer.OnCompleteListener onCompleteListener){
        mOnCompleteListener = onCompleteListener;
        init();
        return this;
    }

    synchronized void play(final int durationToPlay){
        if(isPlaying()){
            return;
        }
        System.out.println("duration to play " + durationToPlay);
        mSessionLength = durationToPlay;
        player.setPlaybackHeadPosition(0);
        player.flush();
        player.setNotificationMarkerPosition(durationToPlay);
        player.play();
        mPlaybackThread = new Thread(){
            public void run(){
                //the starting position needs to beginning of the 16bit PCM data, not in the middle
                //position in the buffer keeps track of where we are for playback
                int shortsRetrieved = 1;
                int shortsWritten = 0;
                while(!mPlaybackThread.isInterrupted() && isPlaying() && shortsRetrieved > 0){
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

    void init(){
        //some arbitrarily larger buffer
        minBufferSize = 10 * AudioTrack.getMinBufferSize(AudioInfo.SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        player = new AudioTrack(AudioManager.STREAM_MUSIC, AudioInfo.SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM);

        mAudioShorts = new short[minBufferSize];
        if(mOnCompleteListener != null) {
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

    private synchronized void finish(){
        System.out.println("marker reached");
        player.stop();
        mPlaybackThread.interrupt();
        mOnCompleteListener.onComplete();
    }

    //Simply pausing the audiotrack does not seem to allow the player to resume.
    synchronized void pause(){
        player.pause();
        int location = player.getPlaybackHeadPosition();
        System.out.println("paused at " + location);
        mBufferProvider.onPauseAfterPlayingXSamples(location);
        player.setPlaybackHeadPosition(0);
        player.flush();
    }

    boolean exists(){
        if(player != null){
            return true;
        } else
            return false;
    }

    synchronized void stop(){
        if(isPlaying() || isPaused()){
            player.pause();
            player.stop();
            player.flush();
            if(mPlaybackThread != null){
                mPlaybackThread.interrupt();
            }
        }
    }

    synchronized void release(){
        stop();
        if(player != null) {
            player.release();
        }
    }

    boolean isPlaying(){
        if(player != null)
            return player.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
        else
            return false;
    }

    boolean isPaused(){
        if(player != null)
            return player.getPlayState() == AudioTrack.PLAYSTATE_PAUSED;
        else
            return false;
    }

    int getPlaybackHeadPosition(){
        return player.getPlaybackHeadPosition();
    }
    int getDuration(){
        return 0;
    }
    int getAdjustedDuration(){
        return 0;
    }
    int getAdjustedLocation(){
        return 0;
    }
    void startSectionAt(int i){
    }
    void seekTo(int i){
    }
    void seekToEnd(){
    }
    void seekToStart(){
    }
    boolean checkIfShouldStop(){
        return true;
    }
    void setOnlyPlayingSection(boolean b){
    }
    void stopSectionAt(int i){
    }

//
//    int getLocationMs(){
//        if(player != null) {
//            int loc = Math.min((int)Math.round(((playbackStart / 2 + player.getPlaybackHeadPosition()) *
//                    (1000.0 / (float)AudioInfo.SAMPLERATE))), getDuration());
////            if(mMovedBackwards){
////                loc = mCutOp.reverseTimeAdjusted(loc, (int) (playbackStart / 88.2));
////            } else {
//            //Ignore cuts prior to playback start: assume they're already accounted for
//            loc = mCutOp.timeAdjusted(loc, (int) Math.round(playbackStart / 88.2));
//           // }
//            return loc;
//        }
//        else {
//            forceBreakOut = true;
//            return 0;
//        }
//    }
//
//    int getAdjustedLocation(){
//        if(player != null) {
//            int loc = mCutOp.reverseTimeAdjusted(getLocationMs());
//            return loc;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    int getDuration(){
//        if(player != null && audioData != null){
//            int duration = (int)(audioData.capacity()/((AudioInfo.SAMPLERATE/1000.0) * AudioInfo.BLOCKSIZE));
//            return duration;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    int getAdjustedDuration(){
//        return getDuration() - mCutOp.getSizeCut();
//    }


//    void setOnlyPlayingSection(Boolean onlyPlayingSection){
//        mOnlyPlayingSection = onlyPlayingSection;
//    }
//
//    void resetState(){
//        mOnlyPlayingSection = false;
//        endPlaybackPosition = 0;
//        startPlaybackPosition = 0;
//        minBufferSize = 0;
//        keepPlaying = false;
//        playbackStart = 0;
//        forceBreakOut = false;
//        releaseAtEnd = false;
//    }
//
//    void onPlay(){
//        releaseAtEnd = false;
//        forceBreakOut = false;
//        if(isPlaying()){
//            return;
//        }
//        keepPlaying = true;
//        player.flush();
//        player.onPlay();
//        if(!mOnlyPlayingSection && !sPressedSeek && !sPressedPause){
//            playbackStart = 0;
//        }
//        sPressedPause = false;
//        sPressedSeek = false;
//        mPlaybackThread = new Thread(){
//
//            void run(){
//                //the starting position needs to beginning of the 16bit PCM data, not in the middle
//                int position = (playbackStart % 2 == 0)? playbackStart : playbackStart+1;
//                Thread thisThread = Thread.currentThread();
//                //position in the buffer keeps track of where we are for playback
//                audioData.position(position);
//                int limit = audioData.capacity();
//                short[] shorts = new short[minBufferSize/2];
//                byte[] bytes = new byte[minBufferSize];
//                while(audioData != null && audioData.position() < limit && keepPlaying && thisThread == mPlaybackThread){
//
//                    //checks to see if we're in a selected section and should end
//                    if(checkIfShouldStop()){
//                        break;
//                    }
//
//                    get(bytes);
//                    //copy the bytes from the audio file into a short buffer, need to flip byte order
//                    //as wav files are little endian
//                    ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
//                    bytesBuffer.asShortBuffer().get(shorts);
//                    //write the buffer to the audiotrack; the write is blocking
//                    player.write(shorts, 0, shorts.length);
//                }
//                //location doesn't usually end up going to the end before audio playback stops.
//                //continue to loop until the end is reached.
////                while(audioData != null && (getLocationMs() <= (getDuration())) && !forceBreakOut && thisThread == mPlaybackThread){
////                    Thread.yield();
////                }
//                if(releaseAtEnd){
//                    audioData = null;
//                    player = null;
//                }
//            }
//        };
//        mPlaybackThread.start();
//    }
//
//    private void get(byte[] bytes){
//        if(mCutOp.cutExistsInRange(audioData.position(), minBufferSize)){
//            getWithSkips(bytes);
//        } else {
//            getWithoutSkips(bytes);
//        }
//    }
//
//    private void getWithoutSkips(byte[] bytes){
//        int size = bytes.length;
//        int end = 0;
//        boolean brokeEarly = false;
//        for(int i = 0; i < size; i++){
//            if(!audioData.hasRemaining()){
//                brokeEarly = true;
//                end = i;
//                break;
//            }
//            bytes[i] = audioData.get();
//        }
//        if(brokeEarly){
//            for(int i = end; i < size; i++){
//                bytes[i] = 0;
//            }
//        }
//    }
//
//    private void getWithSkips(byte[] bytes){
//        int size = bytes.length;
//        int skip = 0;
//        int end = 0;
//        boolean brokeEarly = false;
//        for(int i = 0; i < size; i++){
//            if(!audioData.hasRemaining()){
//                brokeEarly = true;
//                end = i;
//                break;
//            }
//            skip = mCutOp.skip((int)(audioData.position()/88.2));
//            if(skip != -1 && i % 2 == 0){
//                Logger.i(this.toString(), "Location is " + getLocationMs() + "position is " + audioData.position());
//                int start = (int) (skip * (AudioInfo.SAMPLERATE / 500.0));
//                //make sure the playback start is within the bounds of the file's capacity
//                start = Math.max(Math.min(audioData.capacity(), start), 0);
//                int position = (start % 2 == 0) ? start : start + 1;
//                audioData.position(position);
//                Logger.i(this.toString(), "Location is now " + getLocationMs() + "position is " + audioData.position());
//            }
//            bytes[i] = audioData.get();
//        }
//        if(brokeEarly){
//            for(int i = end; i < size; i++){
//                bytes[i] = 0;
//            }
//        }
//    }
//
//    /**
//     * Sets the audio data to onPlay back; this expects a mapped buffer of PCM data
//     * Header of .wav files should not be included in this mapped buffer
//     * Initializes the audio track to onPlay this file
//     * @param file
//     */
//    void loadFile(MappedByteBuffer file){
//        resetState();
//        audioData = file;
//        minBufferSize = AudioTrack.getMinBufferSize(AudioInfo.SAMPLERATE,
//                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
//
//        System.out.println("buffer size for playback is " + minBufferSize);
//        System.out.println("length of audio data buffer is " +audioData.capacity());
//
//        player = new AudioTrack(AudioManager.STREAM_MUSIC, AudioInfo.SAMPLERATE,
//                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
//                minBufferSize, AudioTrack.MODE_STREAM);
//    }
//
//    //Pause calls flush so as to eliminate data that may have been written right after the onPause
//    void onPause(boolean fromButtonPress){
//        if(player != null){
//            playbackStart = (int)(getLocationMs() * 88.2);
//            sPressedPause = true;
//            onPause();
//        }
//    }
//
//    void onPause(){
//        if(player != null){
//            player.onPause();
//            player.flush();
//            forceBreakOut = true;
//            keepPlaying = false;
//        }
//    }
//
//    boolean exists(){
//        if(player != null){
//            return true;
//        } else
//            return false;
//    }
//
//    void seekToStart(){
//        if(player != null ) {
//            if(mOnlyPlayingSection){
//                seekTo(startPlaybackPosition);
//            }
//            else {
//                seekTo(0);
//            }
//        }
//    }
//
//    void seekToEnd(){
//        if(player != null ) {
//            if(mOnlyPlayingSection){
//                seekTo(endPlaybackPosition);
//            }
//            else {
//                seekTo(mCutOp.timeAdjusted(getDuration() - mCutOp.getSizeCut()));
//            }
//        }
//    }
//
//    void seekTo(int x){
//        boolean wasPlaying = isPlaying();
//        sPressedSeek = true;
//        stop();
//
//        int seconds = x/1000;
//        int ms = (x-(seconds*1000));
//        int tens = ms/10;
//        int idx = (AudioInfo.SAMPLERATE * seconds) + (ms * 44) + (tens);
//        idx*=2;
//
//        playbackStart = idx;
//        //make sure the playback start is within the bounds of the file's capacity
//        if(audioData != null) {
//            playbackStart = Math.max(Math.min(audioData.capacity(), playbackStart), 0);
//        } else {
//            playbackStart = 0;
//        }
//        if(wasPlaying){
//            onPlay();
//        }
//        //Logger.w(this.toString(), "Seeking to " + x + "ms which is location " + playbackStart);
//    }
//
//    void stop(){
//        if(isPlaying() || isPaused()){
//            keepPlaying = false;
//            player.onPause();
//            player.stop();
//            if(mPlaybackThread != null){
//                forceBreakOut = true;
//                mPlaybackThread = null;
//            }
//            player.flush();
//        }
//    }
//
//    void stopSectionAt(int end){
//        endPlaybackPosition = end;
//        mOnlyPlayingSection = true;
//    }
//
//    void startSectionAt(int startMS){
//        startPlaybackPosition = startMS;
//    }
//
//    boolean checkIfShouldStop(){
//        if((getDuration()) <= getLocationMs()) {
//            onPause();
//            if(mOnCompleteListener != null){
//                mOnCompleteListener.onComplete();
//            }
//            return true;
//        }
//        if(mOnlyPlayingSection && (getLocationMs() >= endPlaybackPosition)){
//            onPause();
//            seekTo(startPlaybackPosition);
//            stop();
//            if(mOnCompleteListener != null){
//                mOnCompleteListener.onComplete();
//            }
//            return true;
//        }
//        return false;
//    }
//
//    void release(){
//        stop();
//        if(player != null) {
//            player.release();
//        }
//        if(mPlaybackThread!= null){
//            keepPlaying = false;
//            forceBreakOut = true;
//        } else {
//            releaseAtEnd = true;
//            audioData = null;
//            player = null;
//        }
//    }
//
//    boolean isPlaying(){
//        if(player != null)
//            return player.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
//        else
//            return false;
//    }
//
//    boolean isPaused(){
//        if(player != null)
//            return player.getPlayState() == AudioTrack.PLAYSTATE_PAUSED;
//        else
//            return false;
//    }
//
//    int getLocationMs(){
//        if(player != null) {
//            int loc = Math.min((int)Math.round(((playbackStart / 2 + player.getPlaybackHeadPosition()) *
//                    (1000.0 / (float)AudioInfo.SAMPLERATE))), getDuration());
////            if(mMovedBackwards){
////                loc = mCutOp.reverseTimeAdjusted(loc, (int) (playbackStart / 88.2));
////            } else {
//            //Ignore cuts prior to playback start: assume they're already accounted for
//            loc = mCutOp.timeAdjusted(loc, (int) Math.round(playbackStart / 88.2));
//            // }
//            return loc;
//        }
//        else {
//            forceBreakOut = true;
//            return 0;
//        }
//    }
//
//    int getAdjustedLocation(){
//        if(player != null) {
//            int loc = mCutOp.reverseTimeAdjusted(getLocationMs());
//            return loc;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    int getDuration(){
//        if(player != null && audioData != null){
//            int duration = (int)(audioData.capacity()/((AudioInfo.SAMPLERATE/1000.0) * AudioInfo.BLOCKSIZE));
//            return duration;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    int getAdjustedDuration(){
//        return getDuration() - mCutOp.getSizeCut();
//    }
//
//    int getSelectionEnd(){
//        return endPlaybackPosition;
//    }
}
