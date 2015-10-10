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
        TO_UPPER('⇧'),
        TO_LOWER('⇧'),
        NUMLOCK('⓵');

        public final char symbol;

        SwitchKey(char symbol) {
            this.symbol = symbol;
        }
    }

    public interface ModeChangeListener {
        void onModeChange(String[] rows);
    }

    private final String[] lowercase;
    private final String[] caps;
    private final String[] numeric;
    private final String[] numlocked;
    private String[] currentKeyboard;
    private SwitchKey switchKey;
    private final List<ModeChangeListener> listeners;

    public void addModeChangeListener(ModeChangeListener listener) {
        listener.onModeChange(currentKeyboard);
        listeners.add(listener);
    }

    public enum Event {
        INSERT_CHAR,
        NEXT_MODE,
        LONG_PRESS,
    }

    private String[] decorate(String[] base, SwitchKey switchKey) {
        String[] decorated = Arrays.copyOf(base, base.length);

        String lastLine = decorated[decorated.length - 1];
        lastLine = switchKey.symbol + lastLine + '⌫';
        decorated[decorated.length - 1] = lastLine;

        return decorated;
    }

    public ExactypeMode(String[] lowercase, String[] caps, String[] numeric) {
        this.lowercase = decorate(lowercase, SwitchKey.TO_UPPER);
        this.caps = decorate(caps, SwitchKey.TO_LOWER);
        this.numeric = decorate(numeric, SwitchKey.NUMLOCK);
        this.numlocked = decorate(numeric, SwitchKey.TO_LOWER);

        // This is how we start out
        currentKeyboard = this.caps;
        switchKey = SwitchKey.TO_LOWER;

        listeners = new ArrayList<>();
    }

    private void registerCaps(Event event) {
        switch (event) {
            case INSERT_CHAR:
                currentKeyboard = lowercase;
                switchKey = SwitchKey.TO_UPPER;
                break;

            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    private void registerLowercase(Event event) {
        switch (event) {
            default:
                throw new UnsupportedOperationException(event.toString());
        }
    }

    private void registerNumeric(Event event) {
        switch (event) {
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
        } else {
            throw new UnsupportedOperationException(
                "No event handler for keyboard: " + Arrays.toString(currentKeyboard));
        }

        for (ModeChangeListener listener : listeners) {
            // FIXME: Only do this if something actually changed
            listener.onModeChange(currentKeyboard);
        }
    }

    public String[] getKeyboard() {
        return currentKeyboard;
    }

    public void setShifted(boolean shifted) {
        currentKeyboard = shifted ? caps : lowercase;
    }

    public SwitchKey getModeSwitchKey() {
        return switchKey;
    }
}
