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
    private static final String[] LOWERCASE = new String[] { "Lowercase" };
    private static final String[] CAPS = new String[] { "Caps" };
    private static final String[] NUMERIC = new String[] { "Numeric" };
    private static final String[] NUMLOCK = new String[] { "Numlocked" };

    private Set<ExactypeMode.Event> needsTesting;

    private abstract static class Generator {
        public ExactypeMode getMode() {
            ExactypeMode mode = new ExactypeMode(LOWERCASE, CAPS, NUMERIC, NUMLOCK);
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
        needsTesting = new HashSet<>();

        Collections.addAll(needsTesting, ExactypeMode.Event.values());
    }

    @After
    public void postVerification() {
        // This assertion triggers either when you've forgotten to test for one event, or as a side
        // effect of another event test failing. The second variant isn't optimal of course, but as
        // long as everything else is fine this will be as well.
        Assert.assertEquals("These weren't tested: " + needsTesting.toString(),
            0, needsTesting.size());
    }

    private void assertModeTransition(
        Generator generator, ExactypeMode.Event event, String[] expected_outcome)
    {
        Assert.assertTrue("Each event should be tested only once", needsTesting.remove(event));

        ExactypeMode mode = generator.getMode();
        mode.register(event);
        Assert.assertArrayEquals(expected_outcome, mode.getKeyboard());
    }

    @Test
    public void testInitial() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                Assert.assertArrayEquals(CAPS, mode.getKeyboard());
            }
        };

        assertModeTransition(generator, ExactypeMode.Event.INSERT_CHAR, LOWERCASE);
        assertModeTransition(generator, ExactypeMode.Event.SHIFT, LOWERCASE);
        assertModeTransition(generator, ExactypeMode.Event.LONG_PRESS, NUMERIC);
        assertModeTransition(generator, ExactypeMode.Event.NUM_LOCK, NUMLOCK);
        assertModeTransition(generator, ExactypeMode.Event.ALPHABETIC, CAPS);
    }

    @Test
    public void testCaps() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                Assert.assertArrayEquals(CAPS, mode.getKeyboard());
            }
        };

        assertModeTransition(generator, ExactypeMode.Event.INSERT_CHAR, LOWERCASE);
        assertModeTransition(generator, ExactypeMode.Event.SHIFT, LOWERCASE);
        assertModeTransition(generator, ExactypeMode.Event.LONG_PRESS, NUMERIC);
        assertModeTransition(generator, ExactypeMode.Event.NUM_LOCK, NUMLOCK);
        assertModeTransition(generator, ExactypeMode.Event.ALPHABETIC, CAPS);
    }

    @Test
    public void testLowercase() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.SHIFT);
                Assert.assertArrayEquals(LOWERCASE, mode.getKeyboard());
            }
        };

        assertModeTransition(generator, ExactypeMode.Event.INSERT_CHAR, LOWERCASE);
        assertModeTransition(generator, ExactypeMode.Event.SHIFT, CAPS);
        assertModeTransition(generator, ExactypeMode.Event.LONG_PRESS, NUMERIC);
        assertModeTransition(generator, ExactypeMode.Event.NUM_LOCK, NUMLOCK);
        assertModeTransition(generator, ExactypeMode.Event.ALPHABETIC, LOWERCASE);
    }

    @Test
    public void testNumeric() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.LONG_PRESS);
                Assert.assertArrayEquals(NUMERIC, mode.getKeyboard());
            }
        };

        assertModeTransition(generator, ExactypeMode.Event.INSERT_CHAR, LOWERCASE);
        assertModeTransition(generator, ExactypeMode.Event.SHIFT, CAPS);

        // Long pressing the numeric keyboard shouldn't be possible, so anything goes here really
        assertModeTransition(generator, ExactypeMode.Event.LONG_PRESS, NUMERIC);

        assertModeTransition(generator, ExactypeMode.Event.NUM_LOCK, NUMLOCK);
        assertModeTransition(generator, ExactypeMode.Event.ALPHABETIC, LOWERCASE);
    }

    @Test
    public void testNumlock() {
        Generator generator = new Generator() {
            @Override
            public void setUp(ExactypeMode mode) {
                mode.register(ExactypeMode.Event.NUM_LOCK);
                Assert.assertArrayEquals(NUMLOCK, mode.getKeyboard());
            }
        };

        assertModeTransition(generator, ExactypeMode.Event.INSERT_CHAR, NUMLOCK);
        assertModeTransition(generator, ExactypeMode.Event.SHIFT, CAPS);
        assertModeTransition(generator, ExactypeMode.Event.LONG_PRESS, NUMLOCK);

        // The numlock keyboard shouldn't have any numlock key, so anything goes here really
        assertModeTransition(generator, ExactypeMode.Event.NUM_LOCK, LOWERCASE);

        assertModeTransition(generator, ExactypeMode.Event.ALPHABETIC, LOWERCASE);
    }
}
