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

import android.content.Context;

import com.gmail.walles.johan.exactype.stats.StatsTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.ViewModel;
import timber.log.Timber;

public class StatsViewModel extends ViewModel {
    public static class Entry {
        public String name;
        public int count;
        public int rank;
        public int percentile;
    }
    public final List<Entry> entries = new ArrayList<>();

    public void populate(Context context) {
        if (!entries.isEmpty()) {
            return;
        }

        refresh(context);
    }

    public void refresh(Context context) {
        final Map<String, Integer> counts;
        try {
            counts = StatsTracker.getCounts(context);
        } catch (IOException e) {
            Timber.w(e, "Failed to read key stats");
            return;
        }

        List<Map.Entry<String, Integer>> storedEntries = new ArrayList<>(counts.entrySet());
        Collections.sort(storedEntries, (o1, o2) -> -Integer.compare(o1.getValue(), o2.getValue()));
        int totalKeysCount = 0;
        for (Map.Entry<String, Integer> entry: storedEntries) {
            totalKeysCount += entry.getValue();
        }

        entries.clear();
        int rank = 1;
        int countSoFar = 0;
        for (Map.Entry<String, Integer> countEntry: storedEntries) {
            Entry statsEntry = new Entry();
            statsEntry.rank = rank++;

            statsEntry.name = countEntry.getKey();
            if (" ".equals(statsEntry.name)) {
                statsEntry.name = "space";
            }

            statsEntry.count = countEntry.getValue();
            countSoFar += statsEntry.count;
            statsEntry.percentile = (100 * countSoFar) / totalKeysCount;

            entries.add(statsEntry);
        }
    }
}
