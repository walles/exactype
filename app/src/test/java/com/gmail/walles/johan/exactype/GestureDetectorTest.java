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

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    static {
        Mockito.when(MOTION_EVENT.getX())
            .thenThrow(new AssertionFailedError("Call getX(int) instead"));
        Mockito.when(MOTION_EVENT.getY())
            .thenThrow(new AssertionFailedError("Call getY(int) instead"));
    }

    private GestureDetector testMe;
    private GestureListener listener;

    private List<PostedEvent> postedEvents;

    private Map<String, List<Long>> triggeredEventTimes;

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
        triggeredEventTimes = new HashMap<>();

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
        long eventTime, int action, int pointerIndex, float x, float y)
    {
        Mockito.when(MOTION_EVENT.getDownTime()).thenReturn(T0);
        Mockito.when(MOTION_EVENT.getEventTime()).thenReturn(eventTime);
        Mockito.when(MOTION_EVENT.getAction()).thenReturn(action);
        Mockito.when(MOTION_EVENT.getX(pointerIndex)).thenReturn(x);
        Mockito.when(MOTION_EVENT.getY(pointerIndex)).thenReturn(y);

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
    private void doMotion(long eventTime, int action, int pointerIndex, float x, float y) {
        doWaitUntil(eventTime);

        testMe.onTouchEvent(motionEvent(eventTime, action, pointerIndex, x, y));
    }

    /**
     * Passes a motion to the GestureDetector.
     * <p>
     * Triggers timeout handler before that if the motion is after the timeout.
     * </p>
     */
    private void doMotion(long eventTime, int action, float x, float y) {
        doMotion(eventTime, action, 0, x, y);
    }

    /*
     * Wait until a specific time, trigger timeout handlers on the way.
     */
    private void doWaitUntil(long untilTime) {
        List<Runnable> runUs = new ArrayList<>();
        Iterator<PostedEvent> iterator = postedEvents.iterator();
        while (iterator.hasNext()) {
            PostedEvent event = iterator.next();

            if (untilTime <= event.timeout) {
                continue;
            }

            runUs.add(event.runnable);
            iterator.remove();

            String eventName = event.runnable.toString();
            List<Long> times = triggeredEventTimes.get(eventName);
            if (times == null) {
                times = new LinkedList<>();
                triggeredEventTimes.put(eventName, times);
            }
            times.add(event.timeout);
        }
        for (Runnable runnable : runUs) {
            runnable.run();
        }
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
        Mockito.verify(listener).onDown();

        doMotion(T0 + 1, MotionEvent.ACTION_MOVE, X0 + 1, Y0 + 2);
        Mockito.verify(listener).onMove(X0 + 1, Y0 + 2);

        doMotion(T0 + 2, MotionEvent.ACTION_UP, X0 + 3, Y0 + 5);
        Mockito.verify(listener).onUp();
    }

    @Test
    public void testHold() {
        // Press and hold
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);
        doWaitUntil(T0 + LONG_PRESS_TIMEOUT + 2);
        Mockito.verify(listener).onHold(X0, Y0);

        doWaitUntil(T0 + (LONG_PRESS_TIMEOUT * 3) + 2);
        Mockito.verify(listener, Mockito.times(2)).onHold(X0, Y0);

        doWaitUntil(T0 + (LONG_PRESS_TIMEOUT * 4) + 2);
        Mockito.verify(listener, Mockito.times(3)).onHold(X0, Y0);

        doMotion(T0 + (LONG_PRESS_TIMEOUT * 4) - 2, MotionEvent.ACTION_UP, X0 + 3, Y0 + 5);
        Mockito.verify(listener).onUp();

        Mockito.verify(listener, Mockito.never()).
            onSingleTap(Mockito.anyFloat(), Mockito.anyFloat());

        Assert.assertEquals(Arrays.toString(new Long[] {
            T0 + LONG_PRESS_TIMEOUT,
            T0 + LONG_PRESS_TIMEOUT * 2,
            T0 + LONG_PRESS_TIMEOUT * 3,
        }), Arrays.toString(triggeredEventTimes.get("Repeat").toArray(new Long[0])));
    }

    @Test
    public void testMoveCancelsHold() {
        // Press and hold
        doMotion(T0, MotionEvent.ACTION_DOWN, X0, Y0);
        doWaitUntil(T0 + LONG_PRESS_TIMEOUT + 2);
        Mockito.verify(listener).onHold(X0, Y0);

        doWaitUntil(T0 + (LONG_PRESS_TIMEOUT * 3) + 2);
        Mockito.verify(listener, Mockito.times(2)).onHold(X0, Y0);

        // Moving a little shouldn't cancel anything
        float x = X0 + TOUCH_SLOP - 1;
        float y = Y0 + TOUCH_SLOP - 1;
        doMotion((T0 + (LONG_PRESS_TIMEOUT * 3) + 3), MotionEvent.ACTION_MOVE, x, y);
        doWaitUntil(T0 + (LONG_PRESS_TIMEOUT * 4) + 2);
        Mockito.verify(listener).onHold(x, y);

        // Moving more...
        x = X0 + TOUCH_SLOP + 1;
        y = Y0 + TOUCH_SLOP + 1;
        doMotion((T0 + (LONG_PRESS_TIMEOUT * 4) + 3), MotionEvent.ACTION_MOVE, x, y);

        // ... should cancel the onHold() calls
        doWaitUntil(T0 + LONG_PRESS_TIMEOUT * 10);
        Mockito.verify(listener, Mockito.never()).onHold(x, y);

        // We should still get onUp().
        doMotion(T0 + LONG_PRESS_TIMEOUT * 11, MotionEvent.ACTION_UP,
            X0 + TOUCH_SLOP + 1, Y0 + TOUCH_SLOP + 1);
        Mockito.verify(listener).onUp();

        // This wasn't a single tap
        Mockito.verify(listener, Mockito.never()).
            onSingleTap(Mockito.anyFloat(), Mockito.anyFloat());
    }

    @Test
    public void testMultitouchTouchTouch() {
        final float X1 = X0 + 42;
        final float Y1 = Y0 + 47;

        // First finger down
        doMotion(T0, MotionEvent.ACTION_DOWN, 0, X0, Y0);

        // Second finger down
        doMotion(T0 + 1, MotionEvent.ACTION_DOWN, 1, X1, Y1);

        // When the second finger hits, we should treat the first finger as done
        Mockito.verify(listener).onSingleTap(X0, Y0);
        Mockito.verifyNoMoreInteractions(listener);

        // First finger up
        doMotion(T0 + 2, MotionEvent.ACTION_UP, 0, X0, Y0);

        // Second finger up
        doMotion(T0 + 3, MotionEvent.ACTION_UP, 1, X1, Y1);

        // Second finger done, tap should be detected
        Mockito.verify(listener).onSingleTap(X1, Y1);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testMultitouchTouchLongLongPess() {
        final float X1 = X0 + 42;
        final float Y1 = Y0 + 47;

        // First finger down
        doMotion(T0, MotionEvent.ACTION_DOWN, 0, X0, Y0);

        // Second finger down
        doMotion(T0 + 1, MotionEvent.ACTION_DOWN, 1, X1, Y1);

        // When the second finger hits, we should treat the first finger as done
        Mockito.verify(listener).onSingleTap(X0, Y0);
        Mockito.verifyNoMoreInteractions(listener);

        // First finger up
        doMotion(T0 + 2, MotionEvent.ACTION_UP, 0, X0, Y0);

        // Second finger up after 2x long press timeout
        doMotion(T0 + 1 + LONG_PRESS_TIMEOUT * 2, MotionEvent.ACTION_UP, 1, X1, Y1);

        // Second finger done, long press events should be reported
        InOrder inOrder = Mockito.inOrder(listener);
        inOrder.verify(listener).onLongPress(X1, Y1);
        inOrder.verify(listener).onLongLongPress(X1, Y1);
        inOrder.verify(listener).onLongPressUp(X1, Y1);
        Mockito.verifyNoMoreInteractions(listener);
    }

    /**
     * Test two taps while holding down another key.
     *
     * <p>Timeline:
     * <pre>
     * 00000
     *  1 1
     * </pre>
     */
    @Test
    public void testMultitouchAndSingleTouches1() {
        final float X1 = X0 + 43;
        final float Y1 = Y0 + 48;

        final float X2 = X0 + 270;
        final float Y2 = Y0 + 136;

        // First finger down
        doMotion(T0, MotionEvent.ACTION_DOWN, 0, X0, Y0);

        // Second finger down
        doMotion(T0 + 1, MotionEvent.ACTION_DOWN, 1, X1, Y1);
        // When the second finger hits, we should treat the first finger as done
        Mockito.verify(listener).onSingleTap(X0, Y0);
        Mockito.verifyNoMoreInteractions(listener);

        // Second finger up
        doMotion(T0 + 2, MotionEvent.ACTION_UP, 1, X1, Y1);
        // When the second finger is lifted, we should we should report its tap
        Mockito.verify(listener).onSingleTap(X1, Y1);
        Mockito.verifyNoMoreInteractions(listener);

        // Second finger down (again)
        doMotion(T0 + 3, MotionEvent.ACTION_DOWN, 1, X2, Y2);
        Mockito.verifyNoMoreInteractions(listener);

        // Second finger up (again)
        doMotion(T0 + 4, MotionEvent.ACTION_UP, 1, X2, Y2);
        // When the second finger is lifted, we should we should report its tap
        Mockito.verify(listener).onSingleTap(X2, Y2);
        Mockito.verifyNoMoreInteractions(listener);
    }

    /**
     * Test down 0, down 1, up 0, up 1, followed by an ordinary single tap.
     *
     * <p>Timeline:
     * <pre>
     * 00 0
     *  11
     * </pre>
     */
    @Test
    public void testMultitouchAndSingleTouches2() {
        final float X1 = X0 + 43;
        final float Y1 = Y0 + 48;

        final float X2 = X0 + 270;
        final float Y2 = Y0 + 136;

        // First finger down
        doMotion(T0, MotionEvent.ACTION_DOWN, 0, X0, Y0);

        // Second finger down
        doMotion(T0 + 1, MotionEvent.ACTION_DOWN, 1, X1, Y1);
        // When the second finger hits, we should treat the first finger as done
        Mockito.verify(listener).onSingleTap(X0, Y0);
        Mockito.verifyNoMoreInteractions(listener);

        // First finger up
        doMotion(T0 + 2, MotionEvent.ACTION_UP, 0, X0, Y0);
        Mockito.verifyNoMoreInteractions(listener);

        // Second finger up
        doMotion(T0 + 3, MotionEvent.ACTION_UP, 1, X1, Y1);
        // When the second finger is lifted, we should we should report its tap
        Mockito.verify(listener).onSingleTap(X1, Y1);
        Mockito.verifyNoMoreInteractions(listener);

        // First finger tap
        doMotion(T0 + 4, MotionEvent.ACTION_DOWN, 0, X2, Y2);
        doMotion(T0 + 5, MotionEvent.ACTION_UP, 0, X2, Y2);
        // We should be able to detect a plain tap after the multi touch events are done
        Mockito.verify(listener).onSingleTap(X2, Y2);
        Mockito.verifyNoMoreInteractions(listener);
    }
}
