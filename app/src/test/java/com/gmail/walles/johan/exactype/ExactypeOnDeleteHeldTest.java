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
import android.view.inputmethod.InputConnection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Log.class })
public class ExactypeOnDeleteHeldTest {
    @Before
    public void mockAndroidMethods() {
        PowerMockito.mockStatic(Log.class);
    }

    private static class TestableExactype extends Exactype {
        private final InputConnection inputConnection;

        public TestableExactype(InputConnection inputConnection) {
            this.inputConnection = inputConnection;
            this.feedbackWindow = Mockito.mock(FeedbackWindow.class);
        }

        public FeedbackWindow getFeedbackWindow() {
            return feedbackWindow;
        }

        @Override
        public InputConnection getCurrentInputConnection() {
            return inputConnection;
        }

        @Override
        public void enqueue(Runnable runnable) {
            runnable.run();
        }

        @Override
        protected boolean queueIsEmpty() {
            return true;
        }
    }

    private String deleteWord(final String before) {
        InputConnection inputConnection = Mockito.mock(InputConnection.class);
        Mockito.stub(inputConnection.getSelectedText(0)).toReturn(null);
        Mockito.when(
            inputConnection.getTextBeforeCursor(Mockito.anyInt(), Mockito.eq(0)))
            .thenAnswer(new Answer<String>()
            {
            @Override
            public String answer(InvocationOnMock invocation) {
                int n = (Integer)invocation.getArguments()[0];
                if (n > before.length()) {
                    n = before.length();
                }

                return before.substring(before.length() - n);
            }
        });

        Exactype exactype = new TestableExactype(inputConnection);
        exactype.onDeleteHeld();

        // Capture the parameters passed to deleteSurroundingText()
        ArgumentCaptor<Integer> beforeLength = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(inputConnection).deleteSurroundingText(
            beforeLength.capture(), Mockito.eq(0));


        String after = before.substring(0, Math.max(0, before.length() - beforeLength.getValue()));
        return after;
    }

    /**
     * After deleting a word, the cursor should be where the just-deleted word used to start.
     * <p>
     * Basically we should divide the text into blocks of alphanumeric characters and
     * non-alphanumeric characters. Then, if the text ends with an alphanumeric block, delete that
     * one only. If the text ends with a non-alphanumeric block, delete that and the block before
     * it.
     * </p>
     */
    @Test
    public void testDeleteWord() {
        Assert.assertEquals("", deleteWord(""));

        Assert.assertEquals("", deleteWord("a"));
        Assert.assertEquals("", deleteWord("="));

        Assert.assertEquals("", deleteWord("a "));
        Assert.assertEquals("", deleteWord("= "));

        Assert.assertEquals("", deleteWord("aa"));
        Assert.assertEquals("", deleteWord("aa "));

        Assert.assertEquals("a ", deleteWord("a b"));
        Assert.assertEquals("a ", deleteWord("a b "));
        Assert.assertEquals("a ", deleteWord("a b  "));

        Assert.assertEquals("AA ", deleteWord("AA bb"));
        Assert.assertEquals("BB ", deleteWord("BB bb "));
        Assert.assertEquals("CC ", deleteWord("CC bb  "));

        Assert.assertEquals("aa  ", deleteWord("aa  bb"));
        Assert.assertEquals("aa  ", deleteWord("aa  bb "));
        Assert.assertEquals("aa  ", deleteWord("aa  bb  "));

        Assert.assertEquals("54.", deleteWord("54.32"));
        Assert.assertEquals("54,", deleteWord("54,32"));

        // We're currently looking back 22 chars, this test verifies we don't screw up at the
        // boundary of that. If Exactype.DELETE_LOOKBACK changes, this test will have to change too.
        Assert.assertEquals("12345678", deleteWord("123456789012345678901234567890"));
    }

    @Test
    public void testDeleteSelectedText() {
        InputConnection inputConnection = Mockito.mock(InputConnection.class);
        Mockito.stub(inputConnection.getSelectedText(0)).toReturn("something");

        Exactype exactype = new TestableExactype(inputConnection);
        exactype.onDeleteHeld();

        Mockito.verify(inputConnection).commitText("", 1);
    }

    @Test
    public void testCloseFeedbackWindow() {
        InputConnection inputConnection = Mockito.mock(InputConnection.class);
        Mockito.stub(inputConnection.getSelectedText(0)).toReturn("");
        Mockito.stub(inputConnection.getTextBeforeCursor(Mockito.anyInt(), Mockito.eq(0))).toReturn("");
        TestableExactype exactype = new TestableExactype(inputConnection);

        exactype.onDeleteHeld();

        Mockito.verify(exactype.getFeedbackWindow()).close();
    }
}
