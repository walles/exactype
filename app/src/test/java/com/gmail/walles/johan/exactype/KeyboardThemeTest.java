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

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({View.MeasureSpec.class, Log.class})
public class KeyboardThemeTest {
    @Test
    public void testSetBoundsPortrait() throws Exception {
        KeyboardTheme testMe = createKeyboardTheme(100, 300);
        testMe.setBounds(100, 300);

        Assert.assertEquals("Width", 100, testMe.getWidth());
        Assert.assertEquals("Height", 93, testMe.getHeight());
    }

    @Test
    public void testSetBoundsLandscape() throws Exception {
        KeyboardTheme testMe = createKeyboardTheme(300, 100);
        testMe.setBounds(300, 100);

        Assert.assertEquals("Width", 300, testMe.getWidth());
        Assert.assertEquals("Height", 40, testMe.getHeight());
    }

    @NonNull
    private KeyboardTheme createKeyboardTheme(int screenWidth, int screenHeight) {
        PowerMockito.mockStatic(Log.class);

        PowerMockito.mockStatic(View.MeasureSpec.class);
        Mockito.when(View.MeasureSpec.getSize(screenWidth)).thenReturn(screenWidth);
        Mockito.when(View.MeasureSpec.getSize(screenHeight)).thenReturn(screenHeight);

        Paint textPaint = Mockito.mock(Paint.class);
        Paint strokePaint = Mockito.mock(Paint.class);

        KeyboardTheme testMe =
            new KeyboardTheme(
                screenWidth, screenHeight,
                textPaint, strokePaint,
                131, 50f, 50f * 11, 25f);
        testMe.setShouldComputeTextSize();
        return testMe;
    }
}
