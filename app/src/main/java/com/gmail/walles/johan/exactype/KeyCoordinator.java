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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        @Override
        public String toString() {
            return String.format("KeyInfo{x=%f, y=%f, c='%c'}", x, y, character);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeyInfo keyInfo = (KeyInfo) o;

            if (character != keyInfo.character) return false;
            if (Float.compare(keyInfo.x, x) != 0) return false;
            return Float.compare(keyInfo.y, y) == 0;
        }

        @Override
        public int hashCode() {
            int result = (int) character;
            result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
            result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
            return result;
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
        List<KeyInfo> coordinates = new ArrayList<>();

        for (int row_number = 0; row_number < rows.length; row_number++) {
            String row = rows[row_number];

            for (int char_number = 0; char_number < row.length(); char_number++) {
                char current_char = row.charAt(char_number);

                float x =
                    ((char_number + 1f) * width) / row.length() - width / (2f * row.length());
                float y =
                    ((row_number + 1f) * height) / rows.length - height / (2f * rows.length);

                coordinates.add(new KeyInfo(x, y, current_char));
            }
        }

        return coordinates.iterator();
    }
}
