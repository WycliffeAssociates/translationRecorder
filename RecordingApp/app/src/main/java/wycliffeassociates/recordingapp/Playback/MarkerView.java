/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wycliffeassociates.recordingapp.Playback;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.AudioVisualization.SectionMarkers;
import wycliffeassociates.recordingapp.AudioVisualization.UIDataManager;


/**
 * Represents a draggable start or end marker.
 *
 * Most events are passed back to the client class using a
 * listener interface.
 *
 * This class directly keeps track of its own velocity, though,
 * accelerating as the user holds down the left or right arrows
 * while this control is focused.
 */
public class MarkerView extends ImageView {

    public static boolean LEFT = false;
    public static boolean RIGHT = true;
    public boolean mOrientation;
    /**
     * Detects gestures on the main canvas
     */
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * Detects if the user is scrolling the main waveform horizontally
         * @param distX refers to how far the user scrolled horizontally
         * @param distY is ignored for this use as we are only allowing horizontal scrolling
         * @param event1 not accessed, contains information about the start of the gesture
         * @param event2 not used, contains information about the end of the gesture
         * @return must be true for gesture detection
         */
        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distX, float distY) {
            if (mManager != null) {
                int newMarkerTime;// = //(mManager.getAdjustedLocation() + (event2.getX() * mManager.millisecondsPerPixel()));
                if(mOrientation == RIGHT){
                    newMarkerTime = mManager.timeAdjusted((int)(mManager.reverseTimeAdjusted(SectionMarkers.getEndLocationMs()) + (event2.getX() - getWidth()/8.f) * mManager.millisecondsPerPixel()));
                } else {
                    newMarkerTime = mManager.timeAdjusted((int)(mManager.reverseTimeAdjusted(SectionMarkers.getStartLocationMs()) + (event2.getX() - getWidth()/8.f) * mManager.millisecondsPerPixel()));
                }
                if(distX < 0) {
                    int skip = mManager.skip(newMarkerTime);
                    if (skip != -1) {
                        newMarkerTime = skip + 2;
                    }
                } else {
                    int skip = mManager.skipReverse(newMarkerTime);
                    if(skip != Integer.MAX_VALUE){
                        newMarkerTime = skip - 2;
                    }
                }
                if(mOrientation == RIGHT){
                    SectionMarkers.setEndTime(
                            Math.max((int) (Math.min(newMarkerTime, (float) mManager.getDuration())), Math.max(SectionMarkers.getStartLocationMs(), 0)),
                            AudioInfo.SCREEN_WIDTH,
                            mManager.getAdjustedDuration(),
                            mManager
                    );
                    mManager.stopSectionAt(SectionMarkers.getEndLocationMs());
                } else {
                    SectionMarkers.setStartTime(
                            Math.min((int) (Math.max(newMarkerTime, 0)), Math.min(SectionMarkers.getEndLocationMs(), mManager.getDuration())),
                            AudioInfo.SCREEN_WIDTH,
                            mManager.getAdjustedDuration(),
                            mManager
                    );
                    mManager.startSectionAt(SectionMarkers.getStartLocationMs());
                }
                mManager.updateUI();
            }
            return true;
        }
    }

    private UIDataManager mManager;
    protected GestureDetectorCompat mDetector;

    public void setManager(UIDataManager m){
        this.mManager = m;
    }

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDetector = new GestureDetectorCompat(getContext(), new MyGestureListener());
    }

    /**
     * Passes a touch event to the scroll and scale gesture detectors, if they exist
     * @param ev the gesture detected
     * @return returns true to signify the event was handled
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mDetector!= null) {
            mDetector.onTouchEvent(ev);
        }
        return true;
    }

    public void setOrientation(boolean o){
        mOrientation = o;
    }


}
