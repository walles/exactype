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
    private final GestureListener listener;
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
    private int repetitions;

    public GestureDetector(Context context, Handler handler, GestureListener listener) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        touchSlop = viewConfiguration.getScaledTouchSlop();
        longPressTimeout = ViewConfiguration.getLongPressTimeout();

        this.handler = handler;
        this.listener = listener;
    }

    private void setStart(@Nullable final MotionEvent e) {
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
                    startTime + repetitions * longPressTimeout);
            }},
            GestureDetector.this,
            startTime + longPressTimeout);
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
            listener.onDown(event.getX(), event.getY());
            setStart(event);
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            listener.onMove(event.getX(), event.getY());
            mostRecentX = event.getX();
            mostRecentY = event.getY();
            return true;
        }

        if (event.getAction() != MotionEvent.ACTION_UP) {
            // We ignore non-up events
            return false;
        }

        listener.onUp();

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
