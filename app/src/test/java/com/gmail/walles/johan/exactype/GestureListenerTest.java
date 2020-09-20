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

import org.junit.Test;
import org.mockito.Mockito;

public class GestureListenerTest {
    private void setKey(GestureListener gestureListener, char key) {
        KeyCoordinator keyCoordinator = Mockito.mock(KeyCoordinator.class);
        Mockito.when(
            keyCoordinator.getClosestKey(Mockito.anyFloat(), Mockito.anyFloat())).
            thenReturn(key);
        gestureListener.setKeyCoordinator(keyCoordinator);
    }

    @Test
    public void testOnHoldReportsDelete() {
        Exactype exactype = Mockito.mock(Exactype.class);
        GestureListener testMe = new GestureListener(exactype);
        setKey(testMe, '⌫');

        testMe.onHold(10, 10);
        Mockito.verify(exactype).onDeleteHeld();
    }

    @Test
    public void testOnHoldDoesntReportNonDelete() {
        Exactype exactype = Mockito.mock(Exactype.class);
        GestureListener testMe = new GestureListener(exactype);
        setKey(testMe, 'x');

        testMe.onHold(10, 10);
        Mockito.verifyNoMoreInteractions(exactype);
    }

    @Test
    public void testOnLongPressDelete() {
        Exactype exactype = Mockito.mock(Exactype.class);
        GestureListener testMe = new GestureListener(exactype);
        setKey(testMe, '⌫');

        testMe.onLongPress(5, 6);
        Mockito.verifyNoMoreInteractions(exactype);
    }

    @Test
    public void testOnLongPressNonDelete() {
        Exactype exactype = Mockito.mock(Exactype.class);
        GestureListener testMe = new GestureListener(exactype);
        setKey(testMe, 'x');

        testMe.onLongPress(5, 6);
        Mockito.verify(exactype).onLongPress(5, 6);
    }

    @Test
    public void testOnLongLongPressDelete() {
        Exactype exactype = Mockito.mock(Exactype.class);
        GestureListener testMe = new GestureListener(exactype);
        setKey(testMe, '⌫');

        testMe.onLongPress(5f, 6f);
        testMe.onLongLongPress(5, 6);
        Mockito.verifyNoMoreInteractions(exactype);
    }

    @Test
    public void testOnLongLongPressNonDelete() {
        Exactype exactype = Mockito.mock(Exactype.class);
        GestureListener testMe = new GestureListener(exactype);
        setKey(testMe, 'x');

        testMe.onLongPress(5f, 6f);
        testMe.onLongLongPress(5f, 6f);
        Mockito.verify(exactype).onRequestPopupKeyboard('x', 5f, 6f);
    }

    @Test
    public void testOnLongPressUpDelete() {
        Exactype exactype = Mockito.mock(Exactype.class);
        GestureListener testMe = new GestureListener(exactype);
        setKey(testMe, '⌫');

        testMe.onLongPressUp(5f, 6f);

        // We don't care if isPopupKeyboardShowing() is called...
        Mockito.verify(exactype, Mockito.atMost(1)).isPopupKeyboardShowing();

        // ... we just want to ensure nothing gets deleted.
        Mockito.verifyNoMoreInteractions(exactype);
    }

    @Test
    public void testOnLongPressUpNonDelete() {
        Exactype exactype = Mockito.mock(Exactype.class);
        GestureListener testMe = new GestureListener(exactype);
        setKey(testMe, 'x');

        testMe.onLongPressUp(5f, 6f);
        Mockito.verify(exactype).onKeyTapped('x');
    }
}
