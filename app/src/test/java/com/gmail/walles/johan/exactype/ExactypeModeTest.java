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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExactypeModeTest {
    private static final String[] BASE = new String[] { "Base" };
    private static final String[] CAPS = new String[] { "Caps" };
    private static final String[] NUMERIC = new String[] { "Numeric" };
    private static final String[] NUMLOCK = new String[] { "Numlocked" };

    private ExactypeMode testMe;

    @Before
    public void setUp() {
        testMe = new ExactypeMode(BASE, CAPS, NUMERIC, NUMLOCK);
    }

    @Test
    public void testInitial() {
        Assert.assertArrayEquals(CAPS, testMe.getKeyboard());
    }

    @Test
    public void testShiftWhenCaps() {
        testMe.registerShift();
        Assert.assertArrayEquals(BASE, testMe.getKeyboard());
    }

    @Test
    public void testInsertWhenCaps() {
        testMe.registerInsertChar();
        Assert.assertArrayEquals(BASE, testMe.getKeyboard());
    }

    @Test
    public void testLongPressWhenCaps() {
        testMe.registerLongPress();
        Assert.assertArrayEquals(NUMERIC, testMe.getKeyboard());
    }

    @Test
    public void testShiftWhenBase() {
        testMe.registerShift();
        Assert.assertArrayEquals(BASE, testMe.getKeyboard());

        testMe.registerShift();
        Assert.assertArrayEquals(CAPS, testMe.getKeyboard());
    }

    fixme: add more tests
}
