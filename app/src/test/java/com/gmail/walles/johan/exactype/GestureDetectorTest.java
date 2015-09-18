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
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ViewConfiguration.class })
public class GestureDetectorTest {
    private static final int LONG_PRESS_TIMEOUT = 29;
    private static final int TOUCH_SLOP = 7;
    private static final MotionEvent INITIAL_DOWN =
        createMotionEvent(10, 10, MotionEvent.ACTION_DOWN, 30, 40);

    private static MotionEvent createMotionEvent(long downTime, long eventTime, int action, float x, float y) {
        MotionEvent returnMe = Mockito.mock(MotionEvent.class);
        Mockito.when(returnMe.getDownTime()).thenReturn(downTime);
        Mockito.when(returnMe.getEventTime()).thenReturn(eventTime);
        Mockito.when(returnMe.getAction()).thenReturn(action);
        Mockito.when(returnMe.getX()).thenReturn(x);
        Mockito.when(returnMe.getY()).thenReturn(y);
        return returnMe;
    }

    /**
     * Set up the ViewConfiguration static methods.
     */
    private void mockViewConfiguration() {
        ViewConfiguration viewConfiguration = Mockito.mock(ViewConfiguration.class);

        PowerMockito.mockStatic(ViewConfiguration.class);
        Mockito.when(ViewConfiguration.getLongPressTimeout()).thenReturn(LONG_PRESS_TIMEOUT);
        Mockito.when(ViewConfiguration.get((Context) Mockito.any())).thenReturn(viewConfiguration);

        Mockito.when(viewConfiguration.getScaledTouchSlop()).thenReturn(TOUCH_SLOP);
    }

    private GestureListener doSingleTap(int sloppiness, int dt) {
        mockViewConfiguration();

        GestureListener listener = Mockito.mock(GestureListener.class);
        Context context = Mockito.mock(Context.class);

        GestureDetector testMe = new GestureDetector(context, listener);

        testMe.onTouchEvent(INITIAL_DOWN);

        if (sloppiness > 0) {
            // Sloppy move down
            MotionEvent move =
                createMotionEvent(10, 10 + dt / 2, MotionEvent.ACTION_MOVE, 30, 40 + sloppiness);
            testMe.onTouchEvent(move);
        }

        // Release after sloppy move right
        MotionEvent up =
            createMotionEvent(10, 10 + dt, MotionEvent.ACTION_UP, 30 + sloppiness, 40);
        testMe.onTouchEvent(up);

        return listener;
    }

    @Test
    public void testPerfectSingleTap() {
        GestureListener listener = doSingleTap(0, 0);

        Mockito.verify(listener).onSingleTap(INITIAL_DOWN);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testSloppySingleTap() {
        GestureListener listener = doSingleTap(TOUCH_SLOP - 1, LONG_PRESS_TIMEOUT - 1);

        Mockito.verify(listener).onSingleTap(INITIAL_DOWN);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testTooLongSingleTap() {
        GestureListener listener = doSingleTap(0, LONG_PRESS_TIMEOUT + 1);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testTooFarSingleTap() {
        GestureListener listener = doSingleTap(TOUCH_SLOP + 1, 0);
        Mockito.verifyNoMoreInteractions(listener);
    }

    private GestureListener doSwipe(int dx, int dy, int dt) {
        mockViewConfiguration();

        GestureListener listener = Mockito.mock(GestureListener.class);
        Context context = Mockito.mock(Context.class);

        GestureDetector testMe = new GestureDetector(context, listener);

        testMe.onTouchEvent(INITIAL_DOWN);

        // Move half way
        MotionEvent move =
            createMotionEvent(10, 10 + dt / 2, MotionEvent.ACTION_MOVE, 30 + dx / 2, 40 + dy / 2);
        testMe.onTouchEvent(move);

        // Release after moving the rest of the way
        MotionEvent up =
            createMotionEvent(10, 10 + dt, MotionEvent.ACTION_UP, 30 + dx, 40 + dy);
        testMe.onTouchEvent(up);

        return listener;
    }

    @Test
    public void testPerfectSwipe() {
        GestureListener listener = doSwipe(97, 23, 42);
        Mockito.verify(listener.onSwipe(97f, 23f));
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testSlowSwipe() {
        GestureListener listener = doSwipe(97, 23, 4200);
        Mockito.verify(listener.onSwipe(97f, 23f));
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testSlowAndShortSwipe() {
        GestureListener listener = doSwipe(TOUCH_SLOP - 1, 0, 4200);

        // We moved too short, that shouldn't count as a swipe
        Mockito.verifyNoMoreInteractions(listener);
    }
}
