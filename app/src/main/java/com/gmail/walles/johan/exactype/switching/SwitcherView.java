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

package com.gmail.walles.johan.exactype.switching;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.gmail.walles.johan.exactype.KeyboardTheme;

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
    private MotionEvent downEvent;
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
        linearLayout.addView(nextView, layoutParams);
        addView(linearLayout);

        setLayoutParams(layoutParams);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                // We must return true here otherwise gesture detection won't work:
                // http://stackoverflow.com/a/5464365/473672
                return true;
            }

            // FIXME: Override onFling and animate!

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                scrollBy(Math.round(distanceX), 0);
                return true;
            }
        });

        gestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // We always want all touch events
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (downEvent != null) {
                downEvent.recycle();
            }

            downEvent = MotionEvent.obtain(ev);
        }

        if (!isSwitching /* && !isAnimating */) {
            // Pass all touch events through to the current keyboard
            return currentView.onTouchEvent(ev);
        }

        // FIXME: Cancel any running animation; new touches have precedence!

        // Pass the touch to our gesture detector and scroll accordingly
        gestureDetector.onTouchEvent(ev);

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            isSwitching = false;

            // FIXME: Don't do any of these if we're already animating
            Log.d(TAG, "Scroll is " + getScrollX() + "/" + getContentWidth());
            FIXME: This test doesn't work; we never animate right
            if (getScrollX() < getContentWidth() / 2) {
                animateLeft();
            } else {
                animateRight();
            }
        }

        return true;
    }

    private void animateLeft() {
        // FIXME: Actually animate this...
        setScrollX(0);
    }

    private void animateRight() {
        // FIXME: Actually animate, then finish with this...

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

    private int getContentWidth() {
        return currentView.getWidth() + nextView.getWidth();
    }

    public void enableSwitchingMode() {
        isSwitching = true;

        // Inform our gesture detection on when and where the down event happened
        gestureDetector.onTouchEvent(downEvent);
    }
}
