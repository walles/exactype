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
 * Keeps track of which mode the keyboard should be in.
 */
public class ExactypeMode {
    private final String[] lowercase;
    private final String[] caps;
    private final String[] numeric;
    private final String[] numlocked;
    private String[] currentKeyboard;

    public enum Event {
        INSERT_CHAR,
        SHIFT,
        LONG_PRESS,
        NUM_LOCK,

        /**
         * Somebody pressed the let's-do-characters-again button on the numlocked keyboard.
         */
        ALPHABETIC
    }

    public ExactypeMode(String[] lowercase, String[] caps, String[] numeric, String[] numlocked) {
        this.lowercase = lowercase;
        this.caps = caps;
        this.numeric = numeric;
        this.numlocked = numlocked;

        // This is how we start out
        currentKeyboard = this.caps;
    }

    private void registerCaps(Event event) {
        switch (event) {
            case INSERT_CHAR:
                currentKeyboard = lowercase;
                break;

            case SHIFT:
                currentKeyboard = lowercase;
                break;

            case LONG_PRESS:
                currentKeyboard = numeric;
                break;

            case NUM_LOCK:
                currentKeyboard = numlocked;
                break;

            case ALPHABETIC:
                // This branch intentionally left blank
                break;

            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    private void registerLowercase(Event event) {
        switch (event) {
            case INSERT_CHAR:
                currentKeyboard = lowercase;
                break;

            case SHIFT:
                currentKeyboard = caps;
                break;

            case LONG_PRESS:
                currentKeyboard = numeric;
                break;

            case NUM_LOCK:
                currentKeyboard = numlocked;
                break;

            case ALPHABETIC:
                // This branch intentionally left blank
                break;

            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    private void registerNumeric(Event event) {
        switch (event) {
            case INSERT_CHAR:
                currentKeyboard = lowercase;
                break;

            case SHIFT:
                currentKeyboard = caps;
                break;

            case LONG_PRESS:
                // This branch intentionally left blank
                break;

            case NUM_LOCK:
                currentKeyboard = numlocked;
                break;

            case ALPHABETIC:
                currentKeyboard = lowercase;
                break;

            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    private void registerNumLocked(Event event) {
        switch (event) {
            case INSERT_CHAR:
                // This branch intentionally left blank
                break;

            case SHIFT:
                // This branch intentionally left blank
                break;

            case LONG_PRESS:
                // This branch intentionally left blank
                break;

            case NUM_LOCK:
                currentKeyboard = lowercase;
                break;

            case ALPHABETIC:
                currentKeyboard = lowercase;
                break;

            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    public void register(Event event) {
        if (currentKeyboard == caps) {
            registerCaps(event);
        } else if (currentKeyboard == lowercase) {
            registerLowercase(event);
        } else if (currentKeyboard == numeric) {
            registerNumeric(event);
        } else if (currentKeyboard == numlocked) {
            registerNumLocked(event);
        } else {
            throw new UnsupportedOperationException(
                "No event handler for keyboard: " + Arrays.toString(currentKeyboard));
        }
    }

    public String[] getKeyboard() {
        return currentKeyboard;
    }
}
