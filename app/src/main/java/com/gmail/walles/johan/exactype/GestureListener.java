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

public class GestureListener {
    private final Exactype exactype;
    private KeyCoordinator keyCoordinator;
    private char longPressKey;

    public GestureListener(Exactype exactype) {
        this.exactype = exactype;
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
        exactype.onKeyTapped(' ');
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
        exactype.onActionTapped();
    }

    public void onSwipe(float dx, float dy) {
        if (handleRightSwipe(dx, dy)) {
            return;
        }
        handleDownSwipe(dx, dy);
    }

    public void onSingleTap(float x, float y) {
        char tappedKey = keyCoordinator.getClosestKey(x, y);
        if (tappedKey == '⌫') {
            exactype.onDeleteTapped();
        } else if (tappedKey == ExactypeMode.SwitchKey.MARKER) {
            exactype.onKeyboardModeSwitchRequested();
        } else {
            exactype.onKeyTapped(tappedKey);
        }
    }

    public void onLongPress(float x, float y) {
        longPressKey = keyCoordinator.getClosestKey(x, y);
        if (longPressKey == '⌫') {
            // We report repeats for delete, not long presses
            return;
        }

        exactype.onLongPress();
    }

    public void onLongLongPress(float x, float y) {
        if (longPressKey == '⌫') {
            // We want repeats for delete, not popup keyboards
            return;
        }

        exactype.onRequestPopupKeyboard(longPressKey, x, y);
    }

    public void onLongPressUp(float x, float y) {
        if (exactype.isPopupKeyboardShowing()) {
            // Is this really the way to deal with popup keyboard events? I have a feeling we're
            // breaking some kind of abstraction here...
            exactype.onPopupKeyboardTapped(x, y);
        } else {
            onSingleTap(x, y);
        }
    }

    public void onHold(float x, float y) {
        char tappedKey = keyCoordinator.getClosestKey(x, y);
        if (tappedKey != '⌫') {
            return;
        }

        exactype.onDeleteHeld();
    }

    public void onDown(float x, float y) {
        exactype.onTouchStart(x, y);
    }

    public void onMove(float x, float y) {
        exactype.onTouchMove(x, y);
    }

    public void onUp() {
        exactype.onTouchEnd();
    }
}
