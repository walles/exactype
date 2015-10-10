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
        Assert.assertArrayEquals(expected_outcome, mode.getKeyboard());
        Assert.assertEquals(mode_switch_key_after, mode.getModeSwitchKey());
    }

    @Test
    public void testInitial() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                Assert.assertArrayEquals(CAPS, mode.getKeyboard());
                Assert.assertEquals(ExactypeMode.SwitchKey.TO_LOWER, mode.getModeSwitchKey());
            }
        };

        assertModeTransition(generator,
            ExactypeMode.Event.INSERT_CHAR, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(generator,
            ExactypeMode.Event.NEXT_MODE, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(generator,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.NUMLOCK);
    }

    @Test
    public void testCaps() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                Assert.assertArrayEquals(CAPS, mode.getKeyboard());
                Assert.assertEquals(ExactypeMode.SwitchKey.TO_LOWER, mode.getModeSwitchKey());
            }
        };

        assertModeTransition(generator,
            ExactypeMode.Event.INSERT_CHAR, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(generator,
            ExactypeMode.Event.NEXT_MODE, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(generator,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.NUMLOCK);
    }

    @Test
    public void testLowercase() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.NEXT_MODE);
                Assert.assertArrayEquals(LOWERCASE, mode.getKeyboard());
                Assert.assertEquals(ExactypeMode.SwitchKey.TO_UPPER, mode.getModeSwitchKey());
            }
        };

        assertModeTransition(generator,
            ExactypeMode.Event.INSERT_CHAR, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(generator,
            ExactypeMode.Event.NEXT_MODE, CAPS, ExactypeMode.SwitchKey.TO_LOWER);
        assertModeTransition(generator,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.NUMLOCK);
    }

    @Test
    public void testNumeric() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.LONG_PRESS);
                Assert.assertArrayEquals(NUMERIC, mode.getKeyboard());
                Assert.assertEquals(ExactypeMode.SwitchKey.NUMLOCK, mode.getModeSwitchKey());
            }
        };

        assertModeTransition(generator,
            ExactypeMode.Event.INSERT_CHAR, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(generator,
            ExactypeMode.Event.NEXT_MODE, NUMERIC, ExactypeMode.SwitchKey.TO_LOWER);
        assertModeTransition(generator,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.NUMLOCK);
    }

    @Test
    public void testNumlock() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.LONG_PRESS);
                mode.register(ExactypeMode.Event.NEXT_MODE);
                Assert.assertArrayEquals(NUMERIC, mode.getKeyboard());
                Assert.assertEquals(ExactypeMode.SwitchKey.TO_LOWER, mode.getModeSwitchKey());
            }
        };

        assertModeTransition(generator,
            ExactypeMode.Event.INSERT_CHAR, NUMERIC, ExactypeMode.SwitchKey.TO_LOWER);
        assertModeTransition(generator,
            ExactypeMode.Event.NEXT_MODE, LOWERCASE, ExactypeMode.SwitchKey.TO_UPPER);
        assertModeTransition(generator,
            ExactypeMode.Event.LONG_PRESS, NUMERIC, ExactypeMode.SwitchKey.TO_LOWER);
    }

    @Test
    public void testSetShifted() {
        ExactypeMode testMe = createMode();

        testMe.setShifted(true);
        Assert.assertArrayEquals(CAPS, testMe.getKeyboard());
        Assert.assertEquals(ExactypeMode.SwitchKey.TO_LOWER, testMe.getModeSwitchKey());

        testMe.setShifted(false);
        Assert.assertArrayEquals(LOWERCASE, testMe.getKeyboard());
        Assert.assertEquals(ExactypeMode.SwitchKey.TO_UPPER, testMe.getModeSwitchKey());
    }

    private static class LoggingListener implements ExactypeMode.ModeChangeListener {
        private String rows[];
        private ExactypeMode.SwitchKey switchKey;

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
            return switchKey;
        }
    }

    @Test
    public void testCallback() {
        ExactypeMode testMe = createMode();

        LoggingListener listener = new LoggingListener();
        testMe.addModeChangeListener(listener);

        // Assert that the listener was called with the expected initial keyboard (caps)
        Assert.assertArrayEquals(CAPS, listener.getRows());
        Assert.assertEquals(ExactypeMode.SwitchKey.TO_LOWER, listener.getSwitchKey());

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
}
