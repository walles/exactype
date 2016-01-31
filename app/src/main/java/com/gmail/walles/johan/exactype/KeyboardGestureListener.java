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

import com.gmail.walles.johan.exactype.gestures.GestureListener;

public class KeyboardGestureListener implements GestureListener {
    private final ExactypeService exactypeService;
    private KeyCoordinator keyCoordinator;
    private char longPressKey;

    public KeyboardGestureListener(ExactypeService exactypeService) {
        this.exactypeService = exactypeService;
    }

    public void setKeyCoordinator(KeyCoordinator keyCoordinator) {
        this.keyCoordinator = keyCoordinator;
    }

    private boolean handleRightSwipe(float dx, float dy) {
        if (dx <= 0) {
            // More left than right
            return false;
        }
        if (Math.abs(dy) > dx) {
            // More up / down than right
            return false;
        }

        // Right swipe, enter space!
        exactypeService.onKeyTapped(' ');
        return true;
    }

    private void handleDownSwipe(float dx, float dy) {
        if (dy <= 0) {
            // More up than down
            return;
        }
        if (Math.abs(dx) > dy) {
            // More left / right than down
            return;
        }

        // Down swipe, action!
        exactypeService.onActionTapped();
    }

    @Override
    public void onSwipe(float dx, float dy) {
        if (handleRightSwipe(dx, dy)) {
            return;
        }
        handleDownSwipe(dx, dy);
    }

    @Override
    public void onStartSwipe(float dx, float dy) {
        if (dx > 0) {
            // This is a right swipe, we want left swipes
            return;
        }

        if (Math.abs(dy) > Math.abs(dx)) {
            // This is more of an up / down swipe than a left swipe, never mind
            return;
        }

        exactypeService.onStartLeftSwipe();
    }

    @Override
    public void onSingleTap(float x, float y) {
        char tappedKey = keyCoordinator.getClosestKey(x, y);
        if (tappedKey == '⌫') {
            exactypeService.onDeleteTapped();
        } else if (tappedKey == ExactypeMode.SwitchKey.MARKER) {
            exactypeService.onKeyboardModeSwitchRequested();
        } else {
            exactypeService.onKeyTapped(tappedKey);
        }
    }

    @Override
    public void onLongPress(float x, float y) {
        longPressKey = keyCoordinator.getClosestKey(x, y);
        if (longPressKey == '⌫') {
            // We report repeats for delete, not long presses
            return;
        }

        exactypeService.onLongPress(x, y);
    }

    @Override
    public void onLongLongPress(float x, float y) {
        if (longPressKey == '⌫') {
            // We want repeats for delete, not popup keyboards
            return;
        }

        exactypeService.onRequestPopupKeyboard(longPressKey, x, y);
    }

    @Override
    public void onLongPressUp(float x, float y) {
        if (exactypeService.isPopupKeyboardShowing()) {
            // Is this really the way to deal with popup keyboard events? I have a feeling we're
            // breaking some kind of abstraction here...
            exactypeService.onPopupKeyboardTapped(x, y);
        } else {
            char tappedKey = keyCoordinator.getClosestKey(x, y);
            if (tappedKey == '⌫') {
                // We get a long press up on backspace when the user holds the backspace key, but
                // when the user releases the backspace key in this case she's already done. Edge
                // case is when the user long presses something else and slides onto backspace on
                // the alternate keyboard, but I'm guessing that's uncommon enough that we won't
                // worry about it for now.
                return;
            }

            onSingleTap(x, y);
        }
    }

    @Override
    public void onHold(float x, float y) {
        char tappedKey = keyCoordinator.getClosestKey(x, y);
        if (tappedKey != '⌫') {
            return;
        }

        exactypeService.onDeleteHeld();
    }

    @Override
    public void onDown() {
        exactypeService.onTouchStart();
    }

    @Override
    public void onMove(float x, float y) {
        exactypeService.onTouchMove(x, y);
    }

    @Override
    public void onUp() {
        exactypeService.onTouchEnd();
    }
}
