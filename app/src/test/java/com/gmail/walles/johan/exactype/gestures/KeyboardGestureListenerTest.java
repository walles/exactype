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

import com.gmail.walles.johan.exactype.ExactypeService;
import com.gmail.walles.johan.exactype.KeyCoordinator;
import com.gmail.walles.johan.exactype.KeyboardGestureListener;

import org.junit.Test;
import org.mockito.Mockito;

public class KeyboardGestureListenerTest {
    private void setKey(KeyboardGestureListener gestureListener, char key) {
        KeyCoordinator keyCoordinator = Mockito.mock(KeyCoordinator.class);
        Mockito.stub(
            keyCoordinator.getClosestKey(Mockito.anyFloat(), Mockito.anyFloat())).
            toReturn(key);
        gestureListener.setKeyCoordinator(keyCoordinator);
    }

    @Test
    public void testOnHoldReportsDelete() {
        ExactypeService exactypeService = Mockito.mock(ExactypeService.class);
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeService);
        setKey(testMe, '⌫');

        testMe.onHold(10, 10);
        Mockito.verify(exactypeService).onDeleteHeld();
    }

    @Test
    public void testOnHoldDoesntReportNonDelete() {
        ExactypeService exactypeService = Mockito.mock(ExactypeService.class);
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeService);
        setKey(testMe, 'x');

        testMe.onHold(10, 10);
        Mockito.verifyNoMoreInteractions(exactypeService);
    }

    @Test
    public void testOnLongPressDelete() {
        ExactypeService exactypeService = Mockito.mock(ExactypeService.class);
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeService);
        setKey(testMe, '⌫');

        testMe.onLongPress(5, 6);
        Mockito.verifyNoMoreInteractions(exactypeService);
    }

    @Test
    public void testOnLongPressNonDelete() {
        ExactypeService exactypeService = Mockito.mock(ExactypeService.class);
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeService);
        setKey(testMe, 'x');

        testMe.onLongPress(5, 6);
        Mockito.verify(exactypeService).onLongPress(5, 6);
    }

    @Test
    public void testOnLongLongPressDelete() {
        ExactypeService exactypeService = Mockito.mock(ExactypeService.class);
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeService);
        setKey(testMe, '⌫');

        testMe.onLongPress(5f, 6f);
        testMe.onLongLongPress(5, 6);
        Mockito.verifyNoMoreInteractions(exactypeService);
    }

    @Test
    public void testOnLongLongPressNonDelete() {
        ExactypeService exactypeService = Mockito.mock(ExactypeService.class);
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeService);
        setKey(testMe, 'x');

        testMe.onLongPress(5f, 6f);
        testMe.onLongLongPress(5f, 6f);
        Mockito.verify(exactypeService).onRequestPopupKeyboard('x', 5f, 6f);
    }

    @Test
    public void testOnLongPressUpDelete() {
        ExactypeService exactypeService = Mockito.mock(ExactypeService.class);
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeService);
        setKey(testMe, '⌫');

        testMe.onLongPressUp(5f, 6f);

        // We don't care if isPopupKeyboardShowing() is called...
        Mockito.verify(exactypeService, Mockito.atMost(1)).isPopupKeyboardShowing();

        // ... we just want to ensure nothing gets deleted.
        Mockito.verifyNoMoreInteractions(exactypeService);
    }

    @Test
    public void testOnLongPressUpNonDelete() {
        ExactypeService exactypeService = Mockito.mock(ExactypeService.class);
        KeyboardGestureListener testMe = new KeyboardGestureListener(exactypeService);
        setKey(testMe, 'x');

        testMe.onLongPressUp(5f, 6f);
        Mockito.verify(exactypeService).onKeyTapped('x');
    }
}
