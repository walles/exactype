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

import java.util.Arrays;

/**
 * Keeps track of coordinates for keys.
 */
public class KeyCoordinator {
    public boolean hasRows(String[] rows) {
        return Arrays.equals(this.rows, rows);
    }

    public static class KeyInfo {
        public final char character;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        private int x;
        private int y;

        public KeyInfo(int x, int y, char character) {
            this.x = x;
            this.y = y;
            this.character = character;
        }

        @Override
        public String toString() {
            return String.format("KeyInfo{x=%d, y=%d, c='%c'}", x, y, character);
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
    private final KeyInfo[] keys;

    public KeyCoordinator(String rows[]) {
        this.rows = rows;

        int keyCount = 0;
        for (String row : rows) {
            keyCount += row.length();
        }
        keys = new KeyInfo[keyCount];

        int index = 0;
        for (String row : rows) {
            for (int column = 0; column < row.length(); column++) {
                keys[index++] = new KeyInfo(0, 0, row.charAt(column));
            }
        }
    }

    public void setSize(int width, int height) {
        int index = 0;
        for (int row_number = 0; row_number < rows.length; row_number++) {
            String row = rows[row_number];

            for (int char_number = 0; char_number < row.length(); char_number++) {
                int x =
                    ((char_number + 1) * width) / row.length() - width / (2 * row.length());
                int y =
                    ((row_number + 1) * height) / rows.length - height / (2 * rows.length);

                keys[index].x = x;
                keys[index].y = y;
                index++;
            }
        }
    }

    public KeyInfo[] getKeys() {
        return keys;
    }

    /**
     * Find the key closest to a coordinate.
     */
    public char getClosestKey(float x, float y) {
        KeyInfo closestKey = null;
        float closestDistance2 = Float.MAX_VALUE;
        for (KeyInfo keyInfo : keys) {
            float dx = keyInfo.x - x;
            float dy = keyInfo.y - y;
            float distance2 = dx * dx + dy * dy;

            if (distance2 < closestDistance2) {
                closestKey = keyInfo;
                closestDistance2 = distance2;
            }
        }

        return closestKey != null ? closestKey.character : '\0';
    }
}
