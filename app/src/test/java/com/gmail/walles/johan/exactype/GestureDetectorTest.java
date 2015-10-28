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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests for the {@link GestureDetector}.
 * <p>
 * Note that {@link MotionEvent}s are re-used in real-life, so tests need to re-use events as well.
 * </p>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ViewConfiguration.class })
public class GestureDetectorTest {
    private static final int LONG_PRESS_TIMEOUT = 29;
    private static final int TOUCH_SLOP = 7;

    private static final long T0 = 10;
    private static final int X0 = 30;
    private static final int Y0 = 40;

    private static final MotionEvent MOTION_EVENT = Mockito.mock(MotionEvent.class);

    private GestureDetector testMe;
    private GestureListener listener;

    private List<PostedEvent> postedEvents;

    private static class PostedEvent {
        public final Runnable runnable;
        public final long timeout;
        public PostedEvent(Runnable runnable, long timeout) {
            this.runnable = runnable;
            this.timeout = timeout;
        }
    }

    @Before
    public void setUp() {
        mockViewConfiguration();

        listener = Mockito.mock(GestureListener.class);

        Context context = Mockito.mock(Context.class);

        postedEvents = new ArrayList<>();

        Handler handler = Mockito.mock(Handler.class);
        Mockito.stub(handler.postAtTime(
            Mockito.any(Runnable.class),
            Mockito.anyObject(),
            Mockito.anyLong())).toAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                postedEvents.add(new PostedEvent(
                    (Runnable)invocation.getArguments()[0],
                    (long)invocation.getArguments()[2]));

                return true;
            }
        });

        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) {
                    postedEvents.clear();

                    return null;
                }
            }).when(handler).removeCallbacksAndMessages(Mockito.anyObject());

        testMe = new GestureDetector(context, handler, listener);
    }

    @After
    public void checkHandlers() {
        Assert.assertEquals("Handler should be triggered or removed but is still here",
            0, postedEvents.size());
    }

    /**
     * Re-purpose a motion event. The Android code re-uses motion events, that's why we want to do
     * that in the unit tests as well.
     */
    private static MotionEvent motionEvent(
        long eventTime, int action, float x, float y)
    {
        Mockito.when(MOTION_EVENT.getDownTime()).thenReturn(T0);
        Mockito.when(MOTION_EVENT.getEventTime()).thenReturn(eventTime);
        Mockito.when(MOTION_EVENT.getAction()).thenReturn(action);
        Mockito.when(MOTION_EVENT.getX()).thenReturn(x);
        Mockito.when(MOTION_EVENT.getY()).thenReturn(y);

        return MOTION_EVENT;
    }

    /**
     * Set up the ViewConfiguration static methods.
     */
    private void mockViewConfiguration() {
        ViewConfiguration viewConfiguration = Mockito.mock(ViewConfiguration.class);

        PowerMockito.mockStatic(ViewConfiguration.class);
        Mockito.when(ViewConfiguration.getLongPressTimeout()).thenReturn(LONG_PRESS_TIMEOUT);
        Mockito.when(ViewConfiguration.get((Context)Mockito.any())).thenReturn(viewConfiguration);

        Mockito.when(viewConfiguration.getScaledTouchSlop()).thenReturn(TOUCH_SLOP);
    }

    /**
     * Passes a motion to the GestureDetector.
     * <p>
     * Triggers timeout handler before that if the motion is after the timeout.
     * </p>
     */
    private void doMotion(long eventTime, int action, float x, float y) {
        List<Runnable> runUs = new ArrayList<>();
        Iterator<PostedEvent> iterator = postedEvents.iterator();
        while (iterator.hasNext()) {
            PostedEvent event = iterator.next();

            if (eventTime > event.timeout) {
                runUs.add(event.runnable);
                iterator.remove();
            }
        }
        for (Runnable runnable : runUs) {
            runnable.run();
        }

        testMe.onTouchEvent(motionEvent(eventTime, action, x, y));
    }

    private void doSingleTap(int sloppiness, int dt) {
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);

        if (sloppiness > 0) {
            // Sloppy move down
            doMotion(T0 + dt / 2, MotionEvent.ACTION_MOVE, X0, Y0 + sloppiness);
        }

        // Release after sloppy move right
        doMotion(T0 + dt, MotionEvent.ACTION_UP, X0 + sloppiness, Y0);
    }

    @Test
    public void testPerfectSingleTap() {
        doSingleTap(0, 0);

        Mockito.verify(listener).onSingleTap(X0, Y0);
    }

    @Test
    public void testSloppySingleTap() {
        doSingleTap(TOUCH_SLOP - 1, LONG_PRESS_TIMEOUT - 1);

        Mockito.verify(listener).onSingleTap(X0, Y0);
    }

    @Test
    public void testTooLongSingleTap() {
        doSingleTap(0, LONG_PRESS_TIMEOUT + 1);

        // A too long single tap is really a long press, but we just verify it wasn't a single tap
        Mockito.verify(listener, Mockito.never())
            .onSingleTap(Mockito.anyFloat(), Mockito.anyFloat());
    }

    @Test
    public void testTooFarSingleTap() {
        doSingleTap(TOUCH_SLOP + 1, 0);

        // Tapping too far == swipe
        Mockito.verify(listener).onSwipe(Mockito.anyFloat(), Mockito.anyFloat());
    }

    private void doSwipe(int dx, int dy, int dt) {
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);

        int halfWayDt = dt / 2;
        if (dt > LONG_PRESS_TIMEOUT) {
            // Move before the half way timeout to indicate this is not a long press
            halfWayDt = LONG_PRESS_TIMEOUT - 2;
        }

        // Move half way
        doMotion(T0 + halfWayDt, MotionEvent.ACTION_MOVE, X0 + dx / 2f, Y0 + dy / 2f);

        // Release after moving the rest of the way
        doMotion(T0 + dt, MotionEvent.ACTION_UP, X0 + dx, Y0 + dy);
    }

    @Test
    public void testFastSwipe() {
        doSwipe(97, 23, LONG_PRESS_TIMEOUT / 2);
        Mockito.verify(listener).onSwipe(97f, 23f);
    }

    @Test
    public void testSlowSwipe() {
        doSwipe(97, 23, LONG_PRESS_TIMEOUT * 2);
        Mockito.verify(listener).onSwipe(97f, 23f);
    }

    @Test
    public void testSlowAndShortSwipe() {
        doSwipe(TOUCH_SLOP - 1, 0, LONG_PRESS_TIMEOUT * 2);

        // We moved too short, that shouldn't count as a swipe
        Mockito.verify(listener, Mockito.never())
            .onSwipe(Mockito.anyFloat(), Mockito.anyFloat());
    }

    @Test
    public void testLongPress() {
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);

        // Simulate waiting LONG_PRESS_TIMEOUT
        doMotion(T0 + LONG_PRESS_TIMEOUT + 1, MotionEvent.ACTION_MOVE, X0 + 1, Y0);

        Mockito.verify(listener).onLongPress(X0, Y0);

        int x1 = X0 + 29;
        int y1 = Y0 + 31;
        doMotion(T0 + LONG_PRESS_TIMEOUT * 2, MotionEvent.ACTION_UP, x1, y1);

        Mockito.verify(listener).onLongPressUp(x1, y1);
    }

    @Test
    public void testMoveCancelsLongPress() {
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);
        doMotion(
            T0 + LONG_PRESS_TIMEOUT / 2,
            MotionEvent.ACTION_MOVE,
            X0 + TOUCH_SLOP + 1, Y0 + TOUCH_SLOP + 1);

        // Making a move after LONG_PRESS_TIMEOUT simulates waiting for it
        doMotion(
            T0 + LONG_PRESS_TIMEOUT * 2,
            MotionEvent.ACTION_MOVE,
            X0 + TOUCH_SLOP + 1, Y0 + TOUCH_SLOP + 1);

        // We should *not* have received any long press notification
        Mockito.verify(listener, Mockito.never()).
            onLongPress(Mockito.anyFloat(), Mockito.anyFloat());

        int x1 = X0 + 29;
        int y1 = Y0 + 31;
        doMotion(T0 + 3 * LONG_PRESS_TIMEOUT, MotionEvent.ACTION_UP, x1, y1);

        Mockito.verify(listener, Mockito.never())
            .onLongPressUp(Mockito.anyFloat(), Mockito.anyFloat());
    }

    @Test
    public void testLongLongPress() {
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);

        // Simulate waiting LONG_PRESS_TIMEOUT
        doMotion(T0 + LONG_PRESS_TIMEOUT + 1, MotionEvent.ACTION_MOVE, X0 + 1, Y0);

        // X0 here because we didn't move to X0 + 1 until after the long press timeout
        Mockito.verify(listener).onLongPress(X0, Y0);

        // Simulate waiting another LONG_PRESS_TIMEOUT
        doMotion(T0 + 2 * LONG_PRESS_TIMEOUT + 1, MotionEvent.ACTION_MOVE, X0 + 2, Y0);

        // X0 + 1 here is because we didn't move to X0 + 2 until after the second long press timeout
        // had expired
        Mockito.verify(listener).onLongLongPress(X0 + 1, Y0);

        int x1 = X0 + 29;
        int y1 = Y0 + 31;
        doMotion(T0 + 3 * LONG_PRESS_TIMEOUT, MotionEvent.ACTION_UP, x1, y1);

        Mockito.verify(listener).onLongPressUp(x1, y1);
    }

    @Test
    public void testMoveCancelsLongLongPress() {
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);

        // Simulate waiting LONG_PRESS_TIMEOUT
        doMotion(T0 + LONG_PRESS_TIMEOUT + 1, MotionEvent.ACTION_MOVE, X0 + 1, Y0);

        Mockito.verify(listener).onLongPress(X0, Y0);

        // Simulate moving after the long press but before the long long press
        int x1 = X0 + 29;
        int y1 = Y0 + 31;
        doMotion(T0 + LONG_PRESS_TIMEOUT + 2, MotionEvent.ACTION_MOVE, x1, y1);

        // Give the long long press notification a chance to trigger even though it should have been
        // canceled, then release.
        doMotion(T0 + 3 * LONG_PRESS_TIMEOUT, MotionEvent.ACTION_UP, x1, y1);

        Mockito.verify(listener).onLongPressUp(x1, y1);
    }

    @Test
    public void testDownMoveUpEvents() {
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);
        Mockito.verify(listener).onDown(X0, Y0);

        doMotion(T0 + 1, MotionEvent.ACTION_MOVE, X0 + 1, Y0 + 2);
        Mockito.verify(listener).onMove(X0 + 1, Y0 + 2);

        doMotion(T0 + 2, MotionEvent.ACTION_UP, X0 + 3, Y0 + 5);
        Mockito.verify(listener).onUp();
    }
}
