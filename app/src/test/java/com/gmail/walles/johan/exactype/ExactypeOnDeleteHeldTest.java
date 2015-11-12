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

import android.view.inputmethod.InputConnection;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ExactypeOnDeleteHeldTest {
    private static class TestableExactype extends Exactype {
        private final InputConnection inputConnection;

        public TestableExactype(InputConnection inputConnection) {
            this.inputConnection = inputConnection;
        }

        @Override
        public InputConnection getCurrentInputConnection() {
            return inputConnection;
        }
    }

    private String deleteWord(String before) {
        InputConnection inputConnection = Mockito.mock(InputConnection.class);
        Mockito.stub(inputConnection.getSelectedText(0)).toReturn(null);

        Exactype exactype = new TestableExactype(inputConnection);
        exactype.onDeleteHeld();

        // Capture the parameters passed to deleteSurroundingText()
        ArgumentCaptor<Integer> beforeLength = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(inputConnection).deleteSurroundingText(
            beforeLength.capture(), Mockito.eq(0));

        String after = before.substring(0, before.length() - beforeLength.getValue());
        return after;
    }

    @Test
    public void testEmpty() {
        Assert.assertEquals("", deleteWord(""));
    }

    @Test
    public void testWithSelectedText() {
        InputConnection inputConnection = Mockito.mock(InputConnection.class);
        Mockito.stub(inputConnection.getSelectedText(0)).toReturn(null);

        Exactype exactype = new TestableExactype(inputConnection);
        exactype.onDeleteHeld();

        Mockito.verify(inputConnection).commitText("", 1);
    }
}
