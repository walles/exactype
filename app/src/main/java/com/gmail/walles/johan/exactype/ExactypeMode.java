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
import java.util.Arrays;
import java.util.List;

/**
 * Keeps track of which mode the keyboard should be in.
 */
public class ExactypeMode {
    /**
     * What kind of mode switch key should be showing.
     */
    public enum SwitchKey {
        TO_UPPER("Ab"),
        TO_LOWER("ab"),
        NUMLOCK("12");

        public static final char MARKER = '♻';
        final String decoration;

        SwitchKey(String decoration) {
            this.decoration = decoration;
        }
    }

    public interface ModeChangeListener {
        void onModeChange(String[] rows, SwitchKey switchKey);
    }

    private final String[] lowercase;
    private final String[] caps;
    private final String[] numeric;
    private String[] currentKeyboard;

    /**
     * This is the keyboard most recently set by the system.
     */
    private String[] implicitKeyboard;

    private SwitchKey switchKey;
    private final List<ModeChangeListener> listeners;

    public void addModeChangeListener(ModeChangeListener listener) {
        listener.onModeChange(currentKeyboard, switchKey);
        listeners.add(listener);
    }

    public enum Event {
        INSERT_CHAR,
        NEXT_MODE,
        LONG_PRESS,
    }

    private String[] decorate(String[] base) {
        String[] decorated = Arrays.copyOf(base, base.length);

        String lastLine = decorated[decorated.length - 1];
        lastLine = SwitchKey.MARKER + lastLine + '⌫';
        decorated[decorated.length - 1] = lastLine;

        return decorated;
    }

    public ExactypeMode(String[] lowercase, String[] caps, String[] numeric) {
        this.lowercase = decorate(lowercase);
        this.caps = decorate(caps);
        this.numeric = decorate(numeric);

        // This is how we start out
        currentKeyboard = this.caps;
        switchKey = SwitchKey.TO_LOWER;

        implicitKeyboard = currentKeyboard;

        listeners = new ArrayList<>();
    }

    private void registerCaps(Event event) {
        switch (event) {
            case INSERT_CHAR:
                currentKeyboard = lowercase;
                switchKey = SwitchKey.TO_UPPER;
                break;

            case LONG_PRESS:
                currentKeyboard = numeric;
                switchKey = SwitchKey.NUMLOCK;
                break;

            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    private void switchMode() {
        switch (switchKey) {
            case TO_UPPER:
                currentKeyboard = caps;

                if (implicitKeyboard == lowercase) {
                    // lowercase -> uppercase -> numlock
                    switchKey = SwitchKey.NUMLOCK;
                } else {
                    // Numlock -> uppercase -> lowercase
                    switchKey = SwitchKey.TO_LOWER;
                }
                break;

            case TO_LOWER:
                currentKeyboard = lowercase;

                if (implicitKeyboard == lowercase) {
                    // lowercase -> uppercase -> numlock
                    switchKey = SwitchKey.TO_UPPER;
                } else {
                    // numlock -> uppercase -> lowercase
                    switchKey = SwitchKey.NUMLOCK;
                }
                break;

            case NUMLOCK:
                currentKeyboard = numeric;

                if (implicitKeyboard == lowercase) {
                    // lowercase -> uppercase -> numlock
                    switchKey = SwitchKey.TO_LOWER;
                } else {
                    // numlock -> uppercase -> lowercase
                    switchKey = SwitchKey.TO_UPPER;
                }
                break;

            default:
                throw new UnsupportedOperationException(switchKey.toString());
        }
    }

    private void registerLowercase(Event event) {
        switch (event) {
            case INSERT_CHAR:
                // This block intentionally left blank
                break;

            case LONG_PRESS:
                currentKeyboard = numeric;
                switchKey = SwitchKey.NUMLOCK;
                break;

            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    private void registerNumeric(Event event) {
        switch (event) {
            case INSERT_CHAR:
                if (switchKey == SwitchKey.NUMLOCK) {
                    // Numlock not in effect because we have a key for switching it on
                    currentKeyboard = lowercase;
                    switchKey = SwitchKey.TO_UPPER;
                } else {
                    // We're numlocked, do nothing!
                }
                break;

            case LONG_PRESS:
                // This block intentionally left blank
                break;

            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    public void register(Event event) {
        String[] preKeyboard = currentKeyboard;
        SwitchKey preSwitchKey = switchKey;

        if (event == Event.NEXT_MODE) {
            switchMode();
        } else if (currentKeyboard == caps) {
            registerCaps(event);
        } else if (currentKeyboard == lowercase) {
            registerLowercase(event);
        } else if (currentKeyboard == numeric) {
            registerNumeric(event);
        } else {
            throw new UnsupportedOperationException(
                "No event handler for keyboard: " + Arrays.toString(currentKeyboard));
        }

        if (event == Event.INSERT_CHAR) {
            implicitKeyboard = currentKeyboard;
        }

        if (currentKeyboard != preKeyboard || switchKey != preSwitchKey) {
            for (ModeChangeListener listener : listeners) {
                listener.onModeChange(currentKeyboard, switchKey);
            }
        }
    }

    public String[] getKeyboard() {
        return currentKeyboard;
    }

    public void setShifted(boolean shifted) {
        String[] preKeyboard = currentKeyboard;
        SwitchKey preSwitchKey = switchKey;

        currentKeyboard = shifted ? caps : lowercase;
        implicitKeyboard = currentKeyboard;
        switchKey = shifted ? SwitchKey.TO_LOWER : SwitchKey.TO_UPPER;

        if (currentKeyboard != preKeyboard || switchKey != preSwitchKey) {
            for (ModeChangeListener listener : listeners) {
                listener.onModeChange(currentKeyboard, switchKey);
            }
        }
    }

    public SwitchKey getModeSwitchKey() {
        return switchKey;
    }
}
