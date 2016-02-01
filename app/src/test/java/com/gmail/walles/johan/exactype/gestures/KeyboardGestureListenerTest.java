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

package com.gmail.walles.johan.exactype.gestures;

import com.gmail.walles.johan.exactype.ExactypeView;
import com.gmail.walles.johan.exactype.KeyboardGestureListener;

import org.junit.Test;
import org.mockito.Mockito;

public class KeyboardGestureListenerTest {
    private ExactypeView getViewWithKey(char key) {
        ExactypeView exactypeView = Mockito.mock(ExactypeView.class);
        Mockito.when(exactypeView.getClosestKey(Mockito.anyFloat(), Mockito.anyFloat())).
            thenReturn(key);
        return exactypeView;
    }

    @Test
    public void testOnHoldReportsDelete() {
        ExactypeView exactypeView = getViewWithKey('⌫');
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeView);

        testMe.onHold(10, 10);
        Mockito.verify(exactypeView).onDeleteHeld();
    }

    @Test
    public void testOnHoldDoesntReportNonDelete() {
        ExactypeView exactypeView = getViewWithKey('x');
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeView);

        testMe.onHold(10, 10);
        Mockito.verifyNoMoreInteractions(exactypeView);
    }

    @Test
    public void testOnLongPressDelete() {
        ExactypeView exactypeView = getViewWithKey('⌫');
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeView);

        testMe.onLongPress(5, 6);
        Mockito.verifyNoMoreInteractions(exactypeView);
    }

    @Test
    public void testOnLongPressNonDelete() {
        ExactypeView exactypeView = getViewWithKey('x');
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeView);

        testMe.onLongPress(5, 6);
        Mockito.verify(exactypeView).onLongPress(5, 6);
    }

    @Test
    public void testOnLongLongPressDelete() {
        ExactypeView exactypeView = getViewWithKey('⌫');
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeView);

        testMe.onLongPress(5f, 6f);
        testMe.onLongLongPress(5, 6);
        Mockito.verifyNoMoreInteractions(exactypeView);
    }

    @Test
    public void testOnLongLongPressNonDelete() {
        ExactypeView exactypeView = getViewWithKey('x');
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeView);

        testMe.onLongPress(5f, 6f);
        testMe.onLongLongPress(5f, 6f);
        Mockito.verify(exactypeView).onRequestPopupKeyboard('x', 5f, 6f);
    }

    @Test
    public void testOnLongPressUpDelete() {
        ExactypeView exactypeView = getViewWithKey('⌫');
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeView);

        testMe.onLongPressUp(5f, 6f);

        // We don't care if isPopupKeyboardShowing() is called...
        Mockito.verify(exactypeView, Mockito.atMost(1)).isPopupKeyboardShowing();

        // ... we just want to ensure nothing gets deleted.
        Mockito.verifyNoMoreInteractions(exactypeView);
    }

    @Test
    public void testOnLongPressUpNonDelete() {
        ExactypeView exactypeView = getViewWithKey('x');
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeView);

        testMe.onLongPressUp(5f, 6f);
        Mockito.verify(exactypeView).onKeyTapped('x');
    }
}
