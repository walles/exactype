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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ExactypeView extends View {
    private static final String TAG = "Exactype";
    private static final float LETTER_ZOOM_OUT_FACTOR = 3f;

    private final Paint foreground;
    private float verticalCenterOffset;
    private KeyCoordinator keyCoordinator;
    private GestureDetector gestureDetector;

    private String[] rows;

    public ExactypeView(Context exactype) {
        super(exactype);

        foreground = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        foreground.setColor(Color.WHITE);
        foreground.setTextAlign(Paint.Align.CENTER);
    }

    public void setRows(String[] rows) {
        this.rows = rows;

        keyCoordinator = new KeyCoordinator(rows);

        Exactype exactype = (Exactype)getContext();
        gestureDetector =
            new GestureDetector(exactype, new GestureListener(exactype, keyCoordinator));

        requestLayout();
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

    private static int getLongestRowLength(Paint paint, String rows[]) {
        int maxRowLength = 0;
        Rect bounds = new Rect();
        for (String row : rows) {
            paint.getTextBounds(row, 0, row.length(), bounds);
            maxRowLength = Math.max(maxRowLength, bounds.width());
        }
        return maxRowLength;
    }

    private static int sumRowHeights(Paint paint, String rows[]) {
        int rowLengthSum = 0;
        Rect bounds = new Rect();
        for (String row : rows) {
            paint.getTextBounds(row, 0, row.length(), bounds);
            rowLengthSum += bounds.height();
        }
        return rowLengthSum;
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

        // Set foreground font size to 100 and compute the length of the longest line in px
        foreground.setTextSize(100);
        float longestRowLength = getLongestRowLength(foreground, rows);

        // Scale the font size so that the longest line matches the display width
        float factor = width / longestRowLength;
        foreground.setTextSize(100 * factor);

        // Sum up the heights of all keyboard rows with the new font size to get height in px
        int height = sumRowHeights(foreground, rows);

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
