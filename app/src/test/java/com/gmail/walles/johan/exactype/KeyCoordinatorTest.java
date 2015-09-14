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
import java.util.List;

public class KeyCoordinatorTest {
    private void testIteration(
        String[] rows, int width, int height, KeyCoordinator.KeyInfo[] expectedCoordinates)
    {
        KeyCoordinator testMe = new KeyCoordinator(rows, width, height);
        List<KeyCoordinator.KeyInfo> coordinates = new ArrayList<>();
        for (KeyCoordinator.KeyInfo keyInfo : testMe) {
            coordinates.add(keyInfo);
        }

        Assert.assertArrayEquals(expectedCoordinates, coordinates.toArray());
    }

    @Test
    public void testPlacement() {
        // Test single key keyboard
        testIteration(new String[] {"A"}, 200, 100, new KeyCoordinator.KeyInfo[] {
            new KeyCoordinator.KeyInfo(100, 50, 'A')
        });

        // Test 2x2 keyboard
        testIteration(new String[] {"AB", "CD"}, 200, 100, new KeyCoordinator.KeyInfo[] {
            new KeyCoordinator.KeyInfo(50, 25, 'A'),
            new KeyCoordinator.KeyInfo(150, 25, 'B'),
            new KeyCoordinator.KeyInfo(50, 75, 'C'),
            new KeyCoordinator.KeyInfo(150, 75, 'D'),
        });
    }
}
