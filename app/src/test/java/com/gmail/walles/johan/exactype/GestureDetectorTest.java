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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class GestureDetectorTest {
    @Test
    public void testSingleTap() {
        GestureListener listener = Mockito.mock(GestureListener.class);
        Context context = Mockito.mock(Context.class);
        GestureDetector testMe = new GestureDetector(context, listener);

        MotionEvent down = Mockito.mock(MotionEvent.class);
        testMe.onTouchEvent(down);

        MotionEvent up = Mockito.mock(MotionEvent.class);
        testMe.onTouchEvent(up);

        Mockito.verify(listener).onSingleTap(down);
    }

    @Test
    public void testSloppySingleTap() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testTooLongSingleTap() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testTooFarSingleTap() {
        Assert.fail("Test not implemented");
    }

    @Test
    public void testOnFling() {
        Assert.fail("Test not implemented");
    }

    // FIXME: Add more negative fling tests
}
