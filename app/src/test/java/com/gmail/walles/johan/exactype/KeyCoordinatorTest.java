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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class KeyCoordinatorTest {
    @Test
    public void testSingleChar() throws Exception {
        KeyCoordinator testMe = new KeyCoordinator(new String[] {"A"}, 200, 100);
        List<KeyCoordinator.KeyInfo> coordinates = new ArrayList<>();
        for (KeyCoordinator.KeyInfo keyInfo : testMe) {
            coordinates.add(keyInfo);
        }

        KeyCoordinator.KeyInfo[] expectedCoordinates = new KeyCoordinator.KeyInfo[] {
            new KeyCoordinator.KeyInfo(100, 50, 'A')
        };

        Assert.assertArrayEquals(expectedCoordinates, coordinates.toArray());
    }

    @Test
    public void test2x2() throws Exception {
        Assert.fail("This test has not been written yet");
    }
}
