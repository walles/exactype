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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.Nullable;

public class ExactypeModeTest {
    private static final String[] LOWERCASE_LAYOUT = new String[] { "Lowercase", "", "" };
    private static final String[] CAPS_LAYOUT = new String[] { "Caps", "", "" };
    private static final String[] NUMERIC_LAYOUT = new String[] { "Numeric", "", "" };

    private static final String[] LOWERCASE = new String[] { "Lowercase", "", "♻⌫" };
    private static final String[] CAPS = new String[] { "Caps", "", "♻⌫" };
    private static final String[] NUMERIC = new String[] { "Numeric", "", "♻⌫" };

    private Set<ExactypeMode.Event> needsTesting;

    private static ExactypeMode createMode() {
        return new ExactypeMode(LOWERCASE_LAYOUT, CAPS_LAYOUT, NUMERIC_LAYOUT);
    }

    private abstract static class Generator {
        public ExactypeMode getMode() {
            ExactypeMode mode = createMode();
            setUp(mode);
            return mode;
        }

        /**
         * Set up an initial mode for us to test.
         *
         * @param mode The mode instance to modify.
         */
        public abstract void setUp(ExactypeMode mode);
    }

    @Before
    public void setUp() {
        //noinspection ConstantConditions
        needsTesting = null;
    }

    @After
    public void postVerification() {
        if (needsTesting != null) {
            // This assertion triggers either when you've forgotten to test for one event, or as a
            // side effect of another event test failing. The second variant isn't optimal of
            // course, but as long as everything else is fine this will be as well.
            Assert.assertEquals("These weren't tested: " + needsTesting.toString(),
                0, needsTesting.size());
        }
    }

    private void assertMode(
        String[] keyboard, ExactypeMode.SwitchKey switchKey, ExactypeMode mode)
    {
        String wantedDescription = "[" + keyboard[0] + ", " + switchKey + "]";
        String actualDescription = "[" + mode.getKeyboard()[0] + ", " + mode.getModeSwitchKey() + "]";
        Assert.assertEquals("Wrong mode + switch key", wantedDescription, actualDescription);
    }

    /**
     * @param generator Generates a before-state
     * @param event The event type to test
     * @param expected_outcome The expected state after that event
     * @param mode_switch_key_after The expected mode key value after the event
     */
    private void assertModeTransition(
        Generator generator, ExactypeMode.Event event,
        String[] expected_outcome, ExactypeMode.SwitchKey mode_switch_key_after)
    {
        if (needsTesting == null) {
            needsTesting = new HashSet<>();

            Collections.addAll(needsTesting, ExactypeMode.Event.values());
        }

        Assert.assertTrue("Each event should be tested only once", needsTesting.remove(event));

        ExactypeMode mode = generator.getMode();
        mode.register(event);
        assertMode(expected_outcome, mode_switch_key_after, mode);
    }

    @Test
    public void testCaps() {
        Generator caps = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                assertMode(CAPS, ExactypeMode.SwitchKey.NUMLOCK, mode);
            }
        };

        assertModeTransition(caps,
            ExactypeMode.Event.INSERT_CHAR, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(caps,
            ExactypeMode.Event.NEXT_MODE, NUMERIC, ExactypeMode.SwitchKey.TO_LOWER);
        assertModeTransition(caps,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.NUMLOCK);
    }

    @Test
    public void testLowercase() {
        Generator lowercase = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.INSERT_CHAR);
                assertMode(LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER, mode);
            }
        };

        assertModeTransition(lowercase,
            ExactypeMode.Event.INSERT_CHAR, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(lowercase,
            ExactypeMode.Event.NEXT_MODE, CAPS, ExactypeMode.SwitchKey.NUMLOCK);
        assertModeTransition(lowercase,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.NUMLOCK);
    }

    @Test
    public void testNumeric() {
        Generator longPressNumeric = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.LONG_PRESS);
                assertMode(NUMERIC, ExactypeMode.SwitchKey.NUMLOCK, mode);
            }
        };

        assertModeTransition(longPressNumeric,
            ExactypeMode.Event.INSERT_CHAR, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(longPressNumeric,
            ExactypeMode.Event.NEXT_MODE, NUMERIC, ExactypeMode.SwitchKey.TO_LOWER);
        assertModeTransition(longPressNumeric,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.NUMLOCK);
    }

    @Test
    public void testNumlock() {
        Generator numlocked = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.LONG_PRESS);
                mode.register(ExactypeMode.Event.NEXT_MODE);
                assertMode(NUMERIC, ExactypeMode.SwitchKey.TO_LOWER, mode);
            }
        };

        assertModeTransition(numlocked,
            ExactypeMode.Event.INSERT_CHAR, NUMERIC, ExactypeMode.SwitchKey.TO_LOWER);
        assertModeTransition(numlocked,
            ExactypeMode.Event.NEXT_MODE, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(numlocked,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.TO_LOWER);
    }

    @Test
    public void testSetShifted() {
        ExactypeMode testMe = createMode();

        testMe.setShifted(true);
        assertMode(CAPS, ExactypeMode.SwitchKey.NUMLOCK, testMe);

        testMe.setShifted(false);
        assertMode(LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER, testMe);
    }

    @Test
    public void testSetNumeric() {
        ExactypeMode testMe = createMode();

        testMe.setNumeric();
        assertMode(NUMERIC, ExactypeMode.SwitchKey.TO_LOWER, testMe);
    }

    private static class LoggingListener implements ExactypeMode.ModeChangeListener {
        @Nullable private String[] rows;
        @Nullable private ExactypeMode.SwitchKey switchKey;

        @Override
        public void onModeChange(String[] rows, ExactypeMode.SwitchKey switchKey) {
            Assert.assertNull(this.rows);

            this.rows = rows;
            this.switchKey = switchKey;
        }

        public String[] getRows() {
            String[] returnMe = rows;
            rows = null;
            return returnMe;
        }

        public ExactypeMode.SwitchKey getSwitchKey() {
            ExactypeMode.SwitchKey returnMe = switchKey;
            switchKey = null;
            return returnMe;
        }
    }

    @Test
    public void testRegisterCallback() {
        ExactypeMode testMe = createMode();

        LoggingListener listener = new LoggingListener();
        testMe.addModeChangeListener(listener);

        // Assert that the listener was called with the expected initial keyboard (caps)
        Assert.assertArrayEquals(CAPS, listener.getRows());
        Assert.assertEquals(ExactypeMode.SwitchKey.NUMLOCK, listener.getSwitchKey());

        testMe.register(ExactypeMode.Event.INSERT_CHAR);

        // Assert that the listener was called with the lowercase keyboard
        Assert.assertArrayEquals(LOWERCASE, listener.getRows());
        Assert.assertEquals(ExactypeMode.SwitchKey.TO_UPPER, listener.getSwitchKey());

        testMe.register(ExactypeMode.Event.INSERT_CHAR);

        // Assert that the listener was not called, since we should still be at the lowercase
        // keyboard
        Assert.assertNull(listener.getRows());
        Assert.assertNull(listener.getSwitchKey());
    }

    @Test
    public void testSetShiftedCallback() {
        ExactypeMode testMe = createMode();

        LoggingListener listener = new LoggingListener();
        testMe.addModeChangeListener(listener);

        // Assert that the listener was called with the expected initial keyboard (caps)
        Assert.assertArrayEquals(CAPS, listener.getRows());
        Assert.assertEquals(ExactypeMode.SwitchKey.NUMLOCK, listener.getSwitchKey());

        testMe.setShifted(false);

        // Assert that the listener was called with the lowercase keyboard
        Assert.assertArrayEquals(LOWERCASE, listener.getRows());
        Assert.assertEquals(ExactypeMode.SwitchKey.TO_UPPER, listener.getSwitchKey());

        testMe.setShifted(false);

        // Assert that the listener was not called, since we're still at the lowercase keyboard
        Assert.assertNull(listener.getRows());
        Assert.assertNull(listener.getSwitchKey());
    }

    @Test
    public void testSetNumericCallback() {
        ExactypeMode testMe = createMode();

        LoggingListener listener = new LoggingListener();
        testMe.addModeChangeListener(listener);

        // Assert that the listener was called with the expected initial keyboard (caps)
        Assert.assertArrayEquals(CAPS, listener.getRows());
        Assert.assertEquals(ExactypeMode.SwitchKey.NUMLOCK, listener.getSwitchKey());

        testMe.setNumeric();

        // Assert that the listener was called with the numeric keyboard
        Assert.assertArrayEquals(NUMERIC, listener.getRows());
        Assert.assertEquals(ExactypeMode.SwitchKey.TO_LOWER, listener.getSwitchKey());

        testMe.setNumeric();

        // Assert that the listener was not called, since we're still at the numeric keyboard
        Assert.assertNull(listener.getRows());
        Assert.assertNull(listener.getSwitchKey());
    }

    @Test
    public void testSwitchModeFromCaps() {
        ExactypeMode testMe = createMode();
        assertMode(CAPS, ExactypeMode.SwitchKey.NUMLOCK, testMe);

        testMe.register(ExactypeMode.Event.NEXT_MODE);
        assertMode(NUMERIC, ExactypeMode.SwitchKey.TO_LOWER, testMe);

        testMe.register(ExactypeMode.Event.NEXT_MODE);
        assertMode(LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER, testMe);
    }

    @Test
    public void testSwitchModeFromLowercase() {
        ExactypeMode testMe = createMode();
        testMe.register(ExactypeMode.Event.INSERT_CHAR);
        assertMode(LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER, testMe);

        testMe.register(ExactypeMode.Event.NEXT_MODE);
        assertMode(CAPS, ExactypeMode.SwitchKey.NUMLOCK, testMe);

        testMe.register(ExactypeMode.Event.NEXT_MODE);
        assertMode(NUMERIC, ExactypeMode.SwitchKey.TO_LOWER, testMe);
    }
}
