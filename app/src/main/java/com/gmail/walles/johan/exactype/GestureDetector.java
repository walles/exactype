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

import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.gmail.walles.johan.exactype.util.LoggingUtils;

import androidx.annotation.Nullable;
import timber.log.Timber;

public class GestureDetector {
    private static final String TOUCH_EVENT = "Touch";
    private static final String SWIPE_EVENT_HORIZONTAL = "Horizontal Swipe";
    private static final String SWIPE_EVENT_VERTICAL = "Vertical Swipe";
    private static final float MM_PER_INCH = 25.4f;

    private enum Direction {
        HORIZONTAL,
        VERTICAL
    }

    private final GestureListener listener;
    final int touchSlop;
    private final int longPressTimeout;
    private final Handler handler;
    private final DisplayMetrics displayMetrics;

    @Nullable
    private Integer currentPointerId;

    // Motion events are re-used, so we can't just save the motion event. Instead we save all
    // relevant field values.
    private float startX;
    private float startY;
    private float mostRecentX;
    private float mostRecentY;
    private long startTime;
    private boolean isLongPressing;
    private int repetitions;

    public GestureDetector(DisplayMetrics displayMetrics, Handler handler, GestureListener listener) {
        this.displayMetrics = displayMetrics;

        int buttonWidth = displayMetrics.widthPixels / 10; // Assuming 10 buttons per row
        touchSlop = buttonWidth / 2;

        longPressTimeout = ViewConfiguration.getLongPressTimeout();

        Timber.d("Gesture Detector: Touch slop=%dpx, Long press timeout=%dms", touchSlop, longPressTimeout);

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
                                           startTime + 3 * longPressTimeout);
                                   }
                               }

                               @Override
                               public String toString() {
                                   return "Long presses";
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
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_POINTER_UP) {
            // These really mean the same thing, treat them as such
            action = MotionEvent.ACTION_UP;
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            // These really mean the same thing, treat them as such
            action = MotionEvent.ACTION_DOWN;
        }

        final int eventPointerId = event.getPointerId(event.getActionIndex());
        if (currentPointerId == null) {
            if (action == MotionEvent.ACTION_DOWN) {
                // Take on the newly started event
                currentPointerId = eventPointerId;
            } else /* not DOWN */ {
                // Never mind, we only want new events
                return false;
            }
        } else /* currentPointerId is non-null */ {
            if (eventPointerId != currentPointerId) {
                if (action != MotionEvent.ACTION_DOWN) {
                    // We don't care what happens to other pointers unless they go down
                    return false;
                }

                // Another pointer has gone down, ditch the current one and go with the new one

                // Fake an up for the current pointer...
                int pointerIndex = event.findPointerIndex(currentPointerId);
                onTouchEvent(MotionEvent.ACTION_UP,
                    event.getX(pointerIndex), event.getY(pointerIndex),
                    event.getEventTime());

                // ... and continue with the new one
                currentPointerId = eventPointerId;
            }
        }

        int pointerIndex = event.findPointerIndex(currentPointerId);
        final boolean result =
            onTouchEvent(
                action,
                event.getX(pointerIndex), event.getY(pointerIndex),
                event.getEventTime());

        if (action == MotionEvent.ACTION_UP) {
            currentPointerId = null;
        }

        return result;
    }

    private float pixelsToMm(float pixels, Direction direction) {
        switch (direction) {
            case VERTICAL:
                return (pixels / displayMetrics.ydpi) * MM_PER_INCH;
            case HORIZONTAL:
                return (pixels / displayMetrics.xdpi) * MM_PER_INCH;
            default:
                throw new RuntimeException("Unknown direction value: " + direction);
        }
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
            Timber.i("Ignoring event with action type: %d", action);
            return false;
        }

        listener.onUp();

        float dx = Math.abs(x - startX);
        float dy = Math.abs(y - startY);
        if (handleTapEnd(x, y, timestamp)) {
            LoggingUtils.CustomEvent touchMetadata = new LoggingUtils.CustomEvent(TOUCH_EVENT);
            touchMetadata.putCustomAttribute(
                "Horizontal distance (mm)", pixelsToMm(dx, Direction.HORIZONTAL));
            touchMetadata.putCustomAttribute(
                "Vertical distance (mm)", pixelsToMm(dy, Direction.VERTICAL));
            LoggingUtils.logCustom(touchMetadata);

            return true;
        }

        if (handleSwipeEnd(x, y)) {
            LoggingUtils.CustomEvent touchMetadata;
            if (dx > dy) {
                touchMetadata = new LoggingUtils.CustomEvent(SWIPE_EVENT_HORIZONTAL);
                touchMetadata.putCustomAttribute(
                    "Distance (mm)", pixelsToMm(dx, Direction.HORIZONTAL));
            } else {
                touchMetadata = new LoggingUtils.CustomEvent(SWIPE_EVENT_VERTICAL);
                touchMetadata.putCustomAttribute(
                    "Distance (mm)", pixelsToMm(dy, Direction.VERTICAL));
            }
            LoggingUtils.logCustom(touchMetadata);

            return true;
        }

        if (handleLongPressEnd(x, y)) {
            return true;
        }

        // Gesture ended but we don't know how
        Timber.w(new RuntimeException("Gesture ended but we don't know how"),
            "start=(%f, %f) mostRecent=(%f, %f), age=%dms, reps=%d, isLongPressing=%b",
            startX, startY,
            mostRecentX, mostRecentY,
            timestamp - startTime,
            repetitions,
            isLongPressing);

        resetStart();
        return false;
    }
}
