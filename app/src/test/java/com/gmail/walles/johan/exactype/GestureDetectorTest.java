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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ViewConfiguration.class })
public class GestureDetectorTest {
    @Test
    public void testSingleTap() {
        ViewConfiguration viewConfiguration = Mockito.mock(ViewConfiguration.class);

        PowerMockito.mockStatic(ViewConfiguration.class);
        Mockito.when(ViewConfiguration.get((Context)Mockito.any())).thenReturn(viewConfiguration);

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
