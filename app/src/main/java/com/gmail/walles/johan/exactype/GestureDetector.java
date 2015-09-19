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

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class GestureDetector {
    private final GestureListener gestureListener;
    private final int touchSlop;
    private final int longPressTimeout;

    @Nullable
    private MotionEvent startEvent;

    public GestureDetector(Context context, GestureListener gestureListener) {
        this.gestureListener = gestureListener;

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        touchSlop = viewConfiguration.getScaledTouchSlop();
        longPressTimeout = ViewConfiguration.getLongPressTimeout();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startEvent = event;
            return true;
        }

        if (event.getAction() != MotionEvent.ACTION_UP) {
            // We ignore non-up events
            return false;
        }

        if (startEvent == null) {
            // We don't know how this started, can't work with this
            return false;
        }

        long dt = event.getEventTime() - startEvent.getEventTime();
        if (dt >= longPressTimeout) {
            // End of event but not a tap, never mind
            startEvent = null;
            return true;
        }

        float dx = event.getX() - startEvent.getX();
        if (Math.abs(dx) > touchSlop) {
            // End of event but not a tap, never mind
            startEvent = null;
            return true;
        }

        float dy = event.getY() - startEvent.getY();
        if (Math.abs(dy) > touchSlop) {
            // End of event but not a tap, never mind
            startEvent = null;
            return true;
        }

        // Close enough, quick enough
        gestureListener.onSingleTap(startEvent);
        startEvent = null;

        return true;
    }
}
