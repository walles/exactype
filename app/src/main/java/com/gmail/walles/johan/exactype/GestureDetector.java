/*
 * Copyright 2015 Johan Walles <johan.walles@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.walles.johan.exactype;

import android.view.MotionEvent;

public class GestureDetector {
    private final GestureListener gestureListener;

    public GestureDetector(GestureListener gestureListener) {
        this.gestureListener = gestureListener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        // FIXME: Detect single tap and call gestureListener.onSingleTap()

        // FIXME: Detect fling and call gestureListener.onFling()

        // FIXME: Detect long press and call gestureListener.onLongPressDown(). For switching to
        // showing the numeric keyboard

        // FIXME: Detect long press continuing on the same spot and call
        // gestureListener.onLongPressHeld(). For switching back to the letter keyboard and bringing
        // up alternative characters (like @ for A or É for E)

        // FIXME: Detect long press release and call gestureListener.onLongPressUp(). For entering
        // something from the numeric keyboard or from the alternative characters keyboard

        return true;
    }
}
