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

package com.gmail.walles.johan.exactype.util;

import java.util.LinkedList;
import java.util.List;

/**
 * Time some events and present a human readable rendering of how long things took.
 */
public class Timer {
    private final long t0;
    private static class Leg {
        public final String name;
        public final long start;
        public Leg(String name, long start) {
            this.name = name;
            this.start = start;
        }
    }
    private List<Leg> legs;

    public Timer() {
        t0 = System.currentTimeMillis();
    }

    public void addLeg(String name) {
        if (legs == null) {
            legs = new LinkedList<>();
        }
        legs.add(new Leg(name, System.currentTimeMillis()));
    }

    /**
     * Returns for how long this timer has been executing.
     */
    public long getMs() {
        return System.currentTimeMillis() - t0;
    }

    /**
     * @return "47ms" or "100ms = 13ms setup + 87ms something else"
     */
    @Override
    public String toString() {
        long now = System.currentTimeMillis();
        if (legs == null) {
            long dtMs = now - t0;
            return "" + dtMs + "ms";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(now - t0);
        builder.append("ms = ");

        long lastT0 = t0;
        String name = "setup";
        boolean firstLap = true;
        for (Leg leg : legs) {
            if (!firstLap) {
                builder.append(" + ");
            }

            long dt = leg.start - lastT0;
            builder.append(dt);
            builder.append("ms ");
            builder.append(name);

            firstLap = false;
            name = leg.name;
            lastT0 = leg.start;
        }

        builder.append(" + ");
        long lastLegStart = legs.get(legs.size() - 1).start;
        builder.append(now - lastLegStart);
        builder.append("ms ");
        builder.append(name);

        return builder.toString();
    }
}
