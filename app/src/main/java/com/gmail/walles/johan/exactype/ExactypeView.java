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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ExactypeView extends View {
    private static final String TAG = "Exactype";
    private static final float LETTER_ZOOM_OUT_FACTOR = 3f;

    /**
     * Put some air between the keyboard rows.
     */
    private static final float KEYBOARD_HEIGHT_MULTIPLIER = 1.3f;

    private static final String ALL_HEIGHTS = "M";
    private static final String LONG_ROW = "qwertyuiopå";

    private final float fontSize100HeightPx;
    private final float fontSize100LongestRowLength;

    private final Paint foreground;

    private final GestureDetector gestureDetector;
    private final GestureListener gestureListener;

    private float verticalCenterOffset;
    private KeyCoordinator keyCoordinator;

    public ExactypeView(Context context) {
        super(context);
        Exactype exactype = (Exactype)context;

        foreground = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        foreground.setColor(Color.WHITE);
        foreground.setTextAlign(Paint.Align.CENTER);

        foreground.setTextSize(100);

        Rect bounds = new Rect();
        foreground.getTextBounds(ALL_HEIGHTS, 0, ALL_HEIGHTS.length(), bounds);
        fontSize100HeightPx = bounds.height();

        foreground.getTextBounds(LONG_ROW, 0, LONG_ROW.length(), bounds);
        fontSize100LongestRowLength = bounds.width();

        gestureListener = new GestureListener(exactype);
        gestureDetector = new GestureDetector(exactype, new Handler(), gestureListener);
    }

    public void setRows(String[] rows) {
        if (keyCoordinator != null && keyCoordinator.hasRows(rows)) {
            return;
        }

        keyCoordinator = new KeyCoordinator(rows);
        keyCoordinator.setSize(getWidth(), getHeight());

        gestureListener.setKeyCoordinator(keyCoordinator);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Clear the background
        canvas.drawColor(Color.BLUE);

        // Draw the keys
        for (KeyCoordinator.KeyInfo keyInfo : keyCoordinator.getKeys()) {
            String drawMe;
            if (keyInfo.character == '⌫') {
                drawMe = "Bs";
            } else if (keyInfo.character == '⇧') {
                drawMe = "Sh";
            } else {
                drawMe = Character.toString(keyInfo.character);
            }

            canvas.drawText(
                drawMe,
                keyInfo.getX(),
                keyInfo.getY() + verticalCenterOffset,
                foreground);
        }
    }

    private static float computeVerticalCenterOffset(Paint paint) {
        // From: http://www.slideshare.net/rtc1/intro-todrawingtextandroid
        Rect bounds = new Rect();
        paint.getTextBounds("M", 0, 1, bounds);
        Log.i(TAG, bounds.toString());
        return -bounds.top - bounds.height() / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG,
             MeasureSpec.toString(widthMeasureSpec) +
             "x" +
             MeasureSpec.toString(heightMeasureSpec));

        int width = MeasureSpec.getSize(widthMeasureSpec);

        // Scale the font size so that the longest line matches the display width
        float factor = width / fontSize100LongestRowLength;
        foreground.setTextSize(100 * factor);

        // Sum up the heights of all keyboard rows with the new font size to get height in px
        int height = Math.round(3 * fontSize100HeightPx * factor * KEYBOARD_HEIGHT_MULTIPLIER);

        foreground.setTextSize(foreground.getTextSize() / LETTER_ZOOM_OUT_FACTOR);
        verticalCenterOffset = computeVerticalCenterOffset(foreground);

        Log.i(TAG, "Setting dimensions to: " + width + "x" + height);
        setMeasuredDimension(width, height);
        keyCoordinator.setSize(width, height);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}
