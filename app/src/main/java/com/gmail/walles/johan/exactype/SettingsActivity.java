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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    // FIXME: Add a back arrow to the title bar, compare how this looks to any other keyboard
    // settings

    // FIXME: Test rotating the device while showing the slider dialog

    // FIXME: Expose settings activity as a launcher activity

    // FIXME: Test settings back arrow both when launched from System Settings and when launched
    // from the launcher.

    public static final int DEFAULT_VIBRATE_DURATION_MS = 20;
    public static final String VIBRATE_DURATION_MS_KEY = "vibrate_duration_preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new Fragment())
            .commit();
    }

    public static class Fragment
        extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(),
                R.xml.main_preferences, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.main_preferences);

            // Update summaries on preference changes
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferences.registerOnSharedPreferenceChangeListener(this);

            // Update summaries with current values
            int duration = preferences.getInt(VIBRATE_DURATION_MS_KEY, DEFAULT_VIBRATE_DURATION_MS);
            setVibrateDurationSummary(duration);
        }

        private void setVibrateDurationSummary(int ms) {
            findPreference(VIBRATE_DURATION_MS_KEY).setSummary(Integer.toString(ms) + "ms");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (VIBRATE_DURATION_MS_KEY.equals(key)) {
                int duration = sharedPreferences.getInt(
                    VIBRATE_DURATION_MS_KEY,
                    DEFAULT_VIBRATE_DURATION_MS);
                setVibrateDurationSummary(duration);
            }
        }
    }
}
