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
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.gmail.walles.johan.exactype.gestures.GestureDetector;
import com.gmail.walles.johan.exactype.gestures.GestureListenerAdapter;

/**
 * View for switching between different keyboard layouts.
 * <p>
 * We suppress the view-constructor-used-by-tools warning since we don't expect this view to be used
 * by any tool. So far all our layout has been programmatic.
 * </p>
 */
@SuppressLint("ViewConstructor")
public class SwitcherView extends HorizontalScrollView {
    private static final String TAG = "Exactype";

    private View currentView;
    private View nextView;

    private boolean isSwitching;
    private final GestureDetector gestureDetector;

    public SwitcherView(Context context, View currentView, View nextView) {
        super(context);

        this.currentView = currentView;
        this.nextView = nextView;
        isSwitching = false;

        // FIXME: Do we need to react to device orientation change and recalculate these?
        ViewGroup.LayoutParams layoutParams = KeyboardTheme.getLayoutParams(context);

        // Default to showing currentView, have nextView on standby
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(currentView, layoutParams);
        linearLayout.addView(nextView, 1, layoutParams);
        addView(linearLayout);

        setLayoutParams(layoutParams);

        gestureDetector = new GestureDetector(
            context, new Handler(), new GestureListenerAdapter()
        {
            @Override
            public void onMove(float x, float y) {
                scrollTo(Math.round(x), 0);
            }

            @Override
            public void onStartSwipe(float dx, float dy) {
                if (Math.abs(dy) > Math.abs(dx)) {
                    // More vertical than horizontal
                    return;
                }

                if (dx > 0) {
                    // More right than left
                    return;
                }

                // It's a left swipe, enable switching mode!
                isSwitching = true;
            }

            @Override
            public void onUp() {
                isSwitching = false;

                int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
                Log.d(TAG, "Scroll is " + getScrollX() + "/" + screenWidth);
                if (getScrollX() < screenWidth / 2) {
                    switchLeft();
                } else {
                    switchRight();
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // We always want all touch events
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = gestureDetector.onTouchEvent(ev);

        if (isSwitching) {
            // Don't pass any events through while switching
            return result;
        }

        // Pass all touch events through to the current keyboard
        return currentView.onTouchEvent(ev);
    }

    private void switchLeft() {
        setScrollX(0);
    }

    private void switchRight() {
        // Move currentView last...
        LinearLayout linearLayout = (LinearLayout)getChildAt(0);
        linearLayout.removeView(currentView);
        linearLayout.addView(currentView, KeyboardTheme.getLayoutParams(getContext()));

        // ... and switch places between current and next view
        View tempView = currentView;
        currentView = nextView;
        nextView = tempView;

        // ... and scroll fully left
        setScrollX(0);
    }
}
