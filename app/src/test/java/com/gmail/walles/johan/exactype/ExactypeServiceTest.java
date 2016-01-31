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

import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Log.class })
public class ExactypeServiceTest {
    @Before
    public void mockAndroidMethods() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void testNumericLayout() {
        Assert.assertEquals(3, ExactypeService.NUMERIC.length);

        Assert.assertEquals("1234567890", ExactypeService.NUMERIC[0]);

        int line2_length = ExactypeService.NUMERIC[1].length();
        int line3_length = ExactypeService.NUMERIC[2].length() + 2; // Add mode switch and backspace
        int diff = line2_length - line3_length;

        Assert.assertTrue("Move some keys from line 2 to line 3", diff <= 1);
        Assert.assertTrue("Move some keys from line 3 to line 2", diff >= -1);
    }

    @Test
    public void testOnActionTapped() {
        final InputConnection inputConnection = Mockito.mock(InputConnection.class);
        final EditorInfo editorInfo = new EditorInfo();
        editorInfo.imeOptions = EditorInfo.IME_ACTION_SEARCH + EditorInfo.IME_FLAG_FORCE_ASCII;
        ExactypeService exactypeService = new ExactypeService() {
            @Override
            public InputConnection getCurrentInputConnection() {
                return inputConnection;
            }

            @Override
            public EditorInfo getCurrentInputEditorInfo() {
                return editorInfo;
            }

            @Override
            public void enqueue(Runnable runnable) {
                runnable.run();
            }
        };

        exactypeService.onActionTapped();

        Mockito.verify(inputConnection).performEditorAction(EditorInfo.IME_ACTION_SEARCH);
    }
}
