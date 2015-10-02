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
    private static final String[] BASE = new String[] { "Base" };
    private static final String[] CAPS = new String[] { "Caps" };
    private static final String[] NUMERIC = new String[] { "Numeric" };
    private static final String[] NUMLOCK = new String[] { "Numlocked" };

    private Set<ExactypeMode.Event> needsTesting;

    private abstract static class Generator {
        private final ExactypeMode mode;

        public Generator() {
            mode = setUp(new ExactypeMode(BASE, CAPS, NUMERIC, NUMLOCK));
        }

        public ExactypeMode getMode() {
            return mode;
        }

        public abstract ExactypeMode setUp(ExactypeMode mode);
    }

    @Before
    public void setUp() {
        needsTesting = new HashSet<>();

        Collections.addAll(needsTesting, ExactypeMode.Event.values());
    }

    @After
    public void postVerification() {
        Assert.assertEquals("These weren't tested: " + needsTesting.toString(),
            0, needsTesting.size());
    }

    private void assertModeTransition(
        Generator generator, ExactypeMode.Event event, String[] expected_outcome)
    {
        Assert.assertTrue(needsTesting.remove(event));

        ExactypeMode mode = generator.getMode();
        mode.register(event);
        Assert.assertArrayEquals(expected_outcome, mode.getKeyboard());
    }

    @Test
    public void testInitial() {
        Generator generator = new Generator() {
            @Override
            public ExactypeMode setUp(ExactypeMode mode) {
                return mode;
            }
        };

        assertModeTransition(generator, ExactypeMode.Event.INSERT_CHAR, BASE);
        assertModeTransition(generator, ExactypeMode.Event.SHIFT, CAPS);
        assertModeTransition(generator, ExactypeMode.Event.LONG_PRESS, NUMERIC);
        assertModeTransition(generator, ExactypeMode.Event.NUM_LOCK, NUMLOCK);
        assertModeTransition(generator, ExactypeMode.Event.ALPHABETIC, CAPS);
    }
}
