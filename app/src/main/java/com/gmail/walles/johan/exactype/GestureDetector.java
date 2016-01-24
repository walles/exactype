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
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class GestureDetector {
    private final GestureListener listener;
    private final int touchSlop;
    private final int longPressTimeout;
    private final Handler handler;

    private int currentPointerIndex;

    // Motion events are re-used, so we can't just save the motion event. Instead we save all
    // relevant field values.
    private float startX;
    private float startY;
    private float mostRecentX;
    private float mostRecentY;
    private long startTime;
    private boolean isLongPressing;
    private int repetitions;

    public GestureDetector(Context context, Handler handler, GestureListener listener) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        touchSlop = viewConfiguration.getScaledTouchSlop();
        longPressTimeout = ViewConfiguration.getLongPressTimeout();

        this.handler = handler;
        this.listener = listener;
    }

    private void resetStart() {
        startTime = 0;
        isLongPressing = false;
        handler.removeCallbacksAndMessages(this);
    }

    private void setStart(float x, float y, long timestamp) {
        startX = x;
        startY = y;
        startTime = timestamp;

        mostRecentX = startX;
        mostRecentY = startY;

        repetitions = 0;

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

                                   if (isLongPressing) {
                                       listener.onLongLongPress(mostRecentX, mostRecentY);
                                   } else {
                                       isLongPressing = true;
                                       listener.onLongPress(mostRecentX, mostRecentY);
                                       handler.postAtTime(
                                           this,
                                           GestureDetector.this,
                                           startTime + 2 * longPressTimeout);
                                   }
                               }
                           },
            GestureDetector.this,
            startTime + longPressTimeout);

        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                if (Math.abs(mostRecentX - startX) > touchSlop) {
                    // We moved too much for a hold, never mind
                    return;
                }
                if (Math.abs(mostRecentY - startY) > touchSlop) {
                    // We moved too much for a hold, never mind
                    return;
                }

                listener.onHold(mostRecentX, mostRecentY);
                repetitions++;

                handler.postAtTime(
                    this,
                    GestureDetector.this,
                    startTime + (repetitions + 1) * longPressTimeout);
            }

            @Override
            public String toString() {
                return "Repeat";
            }},
            GestureDetector.this,
            startTime + longPressTimeout);
    }

    private boolean isStarted() {
        return startTime != 0;
    }

    private boolean handleTapEnd(float x, float y, long timestamp) {
        if (!isStarted()) {
            // We don't know how this started, can't work with this
            return false;
        }

        long dt = timestamp - startTime;
        if (dt >= longPressTimeout) {
            // End of event but not a tap, never mind
            return false;
        }

        float dx = x - startX;
        if (Math.abs(dx) > touchSlop) {
            // End of event but not a tap, never mind
            return false;
        }

        float dy = y - startY;
        if (Math.abs(dy) > touchSlop) {
            // End of event but not a tap, never mind
            return false;
        }

        // Close enough, quick enough
        listener.onSingleTap(startX, startY);
        resetStart();

        return true;
    }

    private boolean handleSwipeEnd(float x, float y) {
        if (!isStarted()) {
            // We don't know how this started, can't work with this
            return false;
        }

        if (isLongPressing) {
            return false;
        }

        float dx = x - startX;
        float dy = y - startY;

        if (Math.abs(dx) < touchSlop && Math.abs(dy) < touchSlop) {
            // Too short for a swipe, never mind
            return false;
        }

        // Far enough
        listener.onSwipe(dx, dy);
        resetStart();

        return true;
    }

    private boolean handleLongPressEnd(float x, float y) {
        if (!isStarted()) {
            // We don't know how this started, can't work with this
            return false;
        }

        if (!isLongPressing) {
            return false;
        }

        listener.onLongPressUp(x, y);
        resetStart();

        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return onTouchEvent(event.getAction(), event.getX(0), event.getY(0), event.getEventTime());
    }

    private boolean onTouchEvent(int action, float x, float y, long timestamp) {
        if (action == MotionEvent.ACTION_DOWN) {
            listener.onDown();
            setStart(x, y, timestamp);
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            listener.onMove(x, y);
            mostRecentX = x;
            mostRecentY = y;
            return true;
        }

        if (action != MotionEvent.ACTION_UP) {
            // We ignore non-up events
            return false;
        }

        listener.onUp();

        if (handleTapEnd(x, y, timestamp)) {
            return true;
        }

        if (handleSwipeEnd(x, y)) {
            return true;
        }

        if (handleLongPressEnd(x, y)) {
            return true;
        }

        // Gesture ended but we don't know how
        resetStart();
        return false;
    }
}
