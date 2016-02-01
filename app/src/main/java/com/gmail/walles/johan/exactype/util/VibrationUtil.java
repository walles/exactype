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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gmail.walles.johan.exactype.SettingsActivity;

public class VibrationUtil implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "Exactype";

    @Nullable
    private final Vibrator vibrator;

    private int vibrate_duration_ms = SettingsActivity.DEFAULT_VIBRATE_DURATION_MS;

    public VibrationUtil(Context context) {
        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        final SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);
        vibrate_duration_ms =
            preferences.getInt(SettingsActivity.VIBRATE_DURATION_MS_KEY,
                SettingsActivity.DEFAULT_VIBRATE_DURATION_MS);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (SettingsActivity.VIBRATE_DURATION_MS_KEY.equals(key)) {
            vibrate_duration_ms =
                preferences.getInt(SettingsActivity.VIBRATE_DURATION_MS_KEY,
                    SettingsActivity.DEFAULT_VIBRATE_DURATION_MS);
        }
    }

    /**
     * Vibrate according to the vibration preference.
     */
    public void vibrate() {
        vibrate(vibrate_duration_ms);
    }

    /**
     * Vibrate if we can / are allowed to.
     *
     * @see #vibrate()
     */
    public void vibrate(int milliseconds) {
        if (vibrator == null) {
            Log.i(TAG, "Not vibrating; no vibration support / vibrator not set up");
            return;
        }

        try {
            vibrator.vibrate(milliseconds);
            Log.d(TAG, milliseconds + "ms vibration started...");
        } catch (SecurityException e) {
            Log.i(TAG, "Not vibrating: " + e.getMessage());
        }
    }
}
