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

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.GridView;

import com.gmail.walles.johan.exactype.gestures.GestureDetector;
import com.gmail.walles.johan.exactype.gestures.GestureListenerAdapter;

@SuppressLint("ViewConstructor")
public class EmojiView extends GridView implements ExactypeMode.ModeChangeListener {
    private final KeyboardTheme theme;
    private final Exactype exactype;

    private GestureDetector startScrollLeftDetector;
    private boolean startScrollLeftDetected;

    public EmojiView(Exactype exactype) {
        super(exactype);

        this.exactype = exactype;
        theme = new KeyboardTheme(exactype.getResources().getDisplayMetrics());
        startScrollLeftDetector =
            new GestureDetector(exactype, new Handler(), new GestureListenerAdapter()
            {
                @Override
                public void onStartSwipe(float dx, float dy) {
                    if (dx >= 0) {
                        // Right swipe, never mind
                        return;
                    }

                    if (Math.abs(dy) > Math.abs(dx)) {
                        // Up / down swipe, never mind
                    }

                    startScrollLeftDetected = true;
                }
            });
    }

    @Override
    public void onModeChange(String[] rows, ExactypeMode.SwitchKey switchKey) {
        // This call intentionally ignored; we have only one mode
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        theme.setSize(width, height);
    }

    // FIXME: Intercept scroll events, and if the user scrolls left, tell the SwitcherView to take over

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        startScrollLeftDetected = false;
        startScrollLeftDetector.onTouchEvent(ev);
        if (startScrollLeftDetected) {
            startScrollLeftDetected = false;

            // User is swiping left, tell ourselves to cancel and the switcher view to take over
            exactype.onStartLeftSwipe();

            MotionEvent cancel = MotionEvent.obtain(ev);
            cancel.setAction(MotionEvent.ACTION_CANCEL);
            try {
                return super.onTouchEvent(cancel);
            } finally {
                cancel.recycle();
            }
        }

        // Otherwise, just pass the touch event through to super
        return super.onTouchEvent(ev);
    }

    // FIXME: Put Emoji in here: http://developer.android.com/guide/topics/ui/layout/gridview.html
}
