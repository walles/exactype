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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Vibrator;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import com.gmail.walles.johan.exactype.util.VibrationUtil;

public class VibrationDurationPreference extends DialogPreference {
    private static final int MIN_MS = 10;
    private static final int MAX_MS = 100;

    private int milliseconds;

    private Vibrator vibrator;

    public VibrationDurationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        setDialogLayoutResource(R.layout.slider);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(R.drawable.ic_launcher);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SeekBar seekBar = (SeekBar)view.findViewById(R.id.seekbar);
        seekBar.setMax(MAX_MS - MIN_MS);
        seekBar.setProgress(milliseconds - MIN_MS);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                milliseconds = progress + MIN_MS;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // This method intentionally left blank
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                VibrationUtil.vibrate(vibrator, milliseconds);
            }
        });
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            milliseconds = this.getPersistedInt(0);
        } else {
            // Set default state from the XML attribute
            milliseconds = (Integer)defaultValue;
            persistInt(milliseconds);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistInt(milliseconds);
        }
    }
}
