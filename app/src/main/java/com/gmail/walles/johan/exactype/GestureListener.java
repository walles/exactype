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

import android.view.MotionEvent;

public class GestureListener {
    private final Exactype exactype;
    private final KeyCoordinator keyCoordinator;

    public GestureListener(Exactype exactype, KeyCoordinator keyCoordinator) {
        this.exactype = exactype;
        this.keyCoordinator = keyCoordinator;
    }

    public boolean onFling(float velocityX, float velocityY) {
        if (velocityX <= 0) {
            // More left than right
            return false;
        }
        if (Math.abs(velocityY) > velocityX) {
            // More up / down than right
            return false;
        }

        // Right fling, enter space!
        exactype.onKeyTapped(' ');

        return true;
    }

    public boolean onSingleTap(MotionEvent e) {
        char tappedKey = keyCoordinator.getClosestKey(e.getX(), e.getY());
        if (tappedKey == '⌫') {
            exactype.onDeleteTapped();
        } else if (tappedKey == '⇧') {
            exactype.shiftTapped();
        } else {
            exactype.onKeyTapped(tappedKey);
        }
        return true;
    }
}
