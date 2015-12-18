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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;

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

    private UIDataManager mManager;
    private int mVelocity;


    public void setManager(UIDataManager m){
        this.mManager = m;
    }

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Make sure we get keys
        setFocusable(true);

        mVelocity = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            //requestFocus();
            //markerTouchStart(this, event.getRawX());
            break;
        case MotionEvent.ACTION_MOVE:
            // We use raw x because this window itself is going to
            // move, which will screw up the "local" coordinates
            //markerTouchMove(this, event.getRawX());
            break;
        case MotionEvent.ACTION_UP:
           // markerTouchEnd(this);
            break;
        }
        return true;
    }





}
