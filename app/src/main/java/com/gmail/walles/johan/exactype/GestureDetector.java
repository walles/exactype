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
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class GestureDetector {
    private GestureListener listener;
    private final int touchSlop;
    private final int longPressTimeout;
    private final Handler handler;

    // Motion events are re-used, so we can't just save the motion event. Instead we save all
    // relevant field values.
    private float startX;
    private float startY;
    private float mostRecentX;
    private float mostRecentY;
    private long startTime;
    private boolean isLongPressing;

    public GestureDetector(Context context, Handler handler) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        touchSlop = viewConfiguration.getScaledTouchSlop();
        longPressTimeout = ViewConfiguration.getLongPressTimeout();

        this.handler = handler;
    }

    public void setListener(GestureListener listener) {
        this.listener = listener;
    }

    private void setStart(@Nullable MotionEvent e) {
        if (e == null) {
            startTime = 0;
            isLongPressing = false;
            handler.removeCallbacksAndMessages(this);
            return;
        }

        startX = e.getX();
        startY = e.getY();
        startTime = e.getEventTime();

        mostRecentX = startX;
        mostRecentY = startY;

        handler.postAtTime(new Runnable() {
                               @Override
                               public void run() {
                                   if (Math.abs(mostRecentX - startX) > touchSlop) {
                                       // We moved too much for a long press, never mind
                                       return;
                                   }
                                   if (Math.abs(mostRecentY - startY) > touchSlop) {
                                       // We moved too much for a long press, never mind
                                       return;
                                   }

                                   isLongPressing = true;
                                   listener.onLongPress(mostRecentX, mostRecentY);
                               }
                           },
            GestureDetector.this,
            e.getEventTime() + longPressTimeout);
    }

    private boolean isStarted() {
        return startTime != 0;
    }

    private boolean handleTapEnd(MotionEvent event) {
        if (!isStarted()) {
            // We don't know how this started, can't work with this
            return false;
        }

        long dt = event.getEventTime() - startTime;
        if (dt >= longPressTimeout) {
            // End of event but not a tap, never mind
            return false;
        }

        float dx = event.getX() - startX;
        if (Math.abs(dx) > touchSlop) {
            // End of event but not a tap, never mind
            return false;
        }

        float dy = event.getY() - startY;
        if (Math.abs(dy) > touchSlop) {
            // End of event but not a tap, never mind
            return false;
        }

        // Close enough, quick enough
        listener.onSingleTap(startX, startY);
        setStart(null);

        return true;
    }

    private boolean handleSwipeEnd(MotionEvent event) {
        if (!isStarted()) {
            // We don't know how this started, can't work with this
            return false;
        }

        if (isLongPressing) {
            return false;
        }

        float dx = event.getX() - startX;
        float dy = event.getY() - startY;

        if (Math.abs(dx) < touchSlop && Math.abs(dy) < touchSlop) {
            // Too short for a swipe, never mind
            return false;
        }

        // Far enough
        listener.onSwipe(dx, dy);
        setStart(null);

        return true;
    }

    private boolean handleLongPressEnd(MotionEvent event) {
        if (!isStarted()) {
            // We don't know how this started, can't work with this
            return false;
        }

        if (!isLongPressing) {
            return false;
        }

        listener.onLongPressUp(event.getX(), event.getY());
        setStart(null);

        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setStart(event);
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mostRecentX = event.getX();
            mostRecentY = event.getY();
            return true;
        }

        if (event.getAction() != MotionEvent.ACTION_UP) {
            // We ignore non-up events
            return false;
        }

        if (handleTapEnd(event)) {
            return true;
        }

        if (handleSwipeEnd(event)) {
            return true;
        }

        if (handleLongPressEnd(event)) {
            return true;
        }

        // Gesture ended but we don't know how
        setStart(null);
        return false;
    }
}
