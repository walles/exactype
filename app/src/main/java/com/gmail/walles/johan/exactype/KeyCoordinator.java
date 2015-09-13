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

import java.util.Iterator;

/**
 * Keeps track of coordinates for keys.
 */
public class KeyCoordinator implements Iterable<KeyCoordinator.KeyInfo> {
    public static class KeyInfo {
        public final char character;
        public final float x;
        public final float y;

        public KeyInfo(float x, float y, char character) {
            this.x = x;
            this.y = y;
            this.character = character;
        }
    }

    private final String[] rows;
    private int width;
    private int height;

    public KeyCoordinator(String rows[], int width, int height) {
        this.rows = rows;
        this.width = width;
        this.height = height;
    }

    @Override
    public Iterator<KeyInfo> iterator() {
        return null;
    }
}
