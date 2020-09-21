/*
 * Copyright 2020 Johan Walles <johan.walles@gmail.com>
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

package com.gmail.walles.johan.exactype.stats.ui;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModel;

public class StatsViewModel extends ViewModel {
    public static class Entry {
        public final char character;
        public final int count;

        private Entry(char character, int count) {
            this.character = character;
            this.count = count;
        }
    }

    public final List<Entry> entries = new ArrayList<>();

    public StatsViewModel() {
        // Add fake data
        for (int i = 0; i < 20; i++) {
            char character = (char)('a' + i);
            int count = i * 71 + 17;
            entries.add(new Entry(character, count));
        }
    }
}
