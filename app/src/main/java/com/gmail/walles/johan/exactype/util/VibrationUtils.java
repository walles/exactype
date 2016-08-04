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

import android.os.Vibrator;
import android.support.annotation.Nullable;

import timber.log.Timber;

public class VibrationUtils {
    private VibrationUtils() {
        // This constructor is just here to make sure nobody instantiates this class
    }

    /**
     * Vibrate if we can / are allowed to.
     */
    public static void vibrate(@Nullable Vibrator vibrator, int milliseconds) {
        if (vibrator == null) {
            Timber.i("Not vibrating; no vibration support / vibrator not set up");
            return;
        }

        try {
            vibrator.vibrate(milliseconds);
            Timber.d("%dms vibration started...", milliseconds);
        } catch (SecurityException e) {
            Timber.i("Not vibrating: %s", e.getMessage());
        }
    }
}
