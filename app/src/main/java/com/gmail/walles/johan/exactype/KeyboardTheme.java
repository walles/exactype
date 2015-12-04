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

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.VisibleForTesting;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class KeyboardTheme {
    private static final String TAG = "Exactype";

    public static final int BACKGROUND_COLOR = Color.BLUE;
    private static final int COLOR = Color.WHITE;

    private static final String ALL_HEIGHTS = "M";
    private static final String LONG_ROW = "qwertyuiopå";

    /**
     * Put some air between the letters.
     */
    private static final float LETTER_ZOOM_OUT_FACTOR = 3f;

    @VisibleForTesting
    static final int POPUP_BORDER_WIDTH_DP = 2;

    /**
     * Put some air between the keyboard rows.
     */
    private static final float KEYBOARD_HEIGHT_MULTIPLIER = 1.3f;

    private final int screenHeight;
    private final int screenWidth;

    private final Paint textPaint;
    private final Paint strokePaint;
    private final int fontSize100HeightPx;
    private final float fontSize100CharWidthPx;
    private final float fontSize100LongestRowLength;
    private final float fontSize100VerticalCenterOffset;

    private int width;
    private int height;
    private float verticalCenterOffset;

    /**
     * The full keyboard should be scaled to screen width; the popup keyboards should be scaled
     * down if they don't fit.
     */
    private boolean isFullKeyboard;

    @VisibleForTesting
    KeyboardTheme(int screenWidth, int screenHeight,
                  Paint textPaint,
                  Paint strokePaint,
                  int fontSize100HeightPx,
                  float fontSize100CharWidthPx,
                  float fontSize100LongestRowLength,
                  float fontSize100VerticalCenterOffset)
    {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.textPaint = textPaint;
        this.strokePaint = strokePaint;

        this.fontSize100HeightPx = fontSize100HeightPx;
        this.fontSize100CharWidthPx = fontSize100CharWidthPx;
        this.fontSize100LongestRowLength = fontSize100LongestRowLength;
        this.fontSize100VerticalCenterOffset = fontSize100VerticalCenterOffset;
    }

    public KeyboardTheme(DisplayMetrics displayMetrics) {
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(COLOR);

        float borderWidthPx =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, POPUP_BORDER_WIDTH_DP, displayMetrics);
        strokePaint.setStrokeWidth(borderWidthPx);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setColor(COLOR);

        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.setTextSize(100);

        Rect bounds = new Rect();
        textPaint.getTextBounds(ALL_HEIGHTS, 0, ALL_HEIGHTS.length(), bounds);
        fontSize100HeightPx = bounds.height();

        textPaint.getTextBounds(LONG_ROW, 0, LONG_ROW.length(), bounds);
        fontSize100LongestRowLength = bounds.width();
        fontSize100CharWidthPx = bounds.width() / (float)LONG_ROW.length();

        // From: http://www.slideshare.net/rtc1/intro-todrawingtextandroid
        textPaint.getTextBounds(ALL_HEIGHTS, 0, ALL_HEIGHTS.length(), bounds);
        fontSize100VerticalCenterOffset = -bounds.top - bounds.height() / 2f;

        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public Paint getStrokePaint() {
        return strokePaint;
    }

    /**
     * Recomputes width, height and vertical center offset.
     *
     * @param keys The single line of keys the keyboard should display
     * @param textSize The wanted font size to use
     */
    public void setContents(String keys, float textSize) {
        isFullKeyboard = false;

        verticalCenterOffset = (fontSize100VerticalCenterOffset * textSize) / 100f;

        float fontHeight = (fontSize100HeightPx * textSize) / 100f;
        height = Math.round(fontHeight * LETTER_ZOOM_OUT_FACTOR);

        float fontWidth = (fontSize100CharWidthPx * textSize) / 100f;
        width = Math.round(fontWidth * keys.length() * LETTER_ZOOM_OUT_FACTOR);

        textPaint.setTextSize(textSize);
    }

    public void setIsFullKeyboard() {
        isFullKeyboard = true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getVerticalCenterOffset() {
        return verticalCenterOffset;
    }

    public float getTextSize() {
        return textPaint.getTextSize();
    }

    /**
     * Compute a text size suitable for having the keyboard filling the width of the display
     *
     * @param maxWidth Maximum keyboard width (= screen width)
     * @param maxHeight Maximum keyboard height (= half of the screen height)
     */
    private void scaleToScreenWidth(int maxWidth, int maxHeight) {
        // Scale the font size so that the longest line matches the display width
        float factor = maxWidth / fontSize100LongestRowLength;

        // Sum up the heights of all keyboard rows with the new font size to get height in px
        int computedHeight = Math.round(3 * fontSize100HeightPx * factor * KEYBOARD_HEIGHT_MULTIPLIER);
        Log.i(TAG, "Size set to " + maxWidth + "x" + computedHeight);

        if (computedHeight > maxHeight) {
            // Don't use more than half the height, required in landscape mode
            factor = (maxWidth / fontSize100LongestRowLength) * ((float)maxHeight / computedHeight);
            computedHeight = Math.round(3 * fontSize100HeightPx * factor * KEYBOARD_HEIGHT_MULTIPLIER);
            Log.d(TAG, "Size reset to " + maxWidth + "x" + computedHeight);
        }

        setSize(maxWidth, computedHeight);
    }

    /**
     * Set full size keyboard dimensions (in pixels) and scale the font size to match.
     */
    public void setSize(int width, int height) {
        isFullKeyboard = true;

        this.width = width;
        this.height = height;

        // Scale the font size so that the longest line matches the display width
        float widthFactor = width / fontSize100LongestRowLength;

        // Scale the font size so that there's room for three rows
        float heightFactor = height / (3f * fontSize100HeightPx);

        float factor = Math.min(widthFactor, heightFactor);

        float textSize = 100 * factor / LETTER_ZOOM_OUT_FACTOR;
        textPaint.setTextSize(textSize);
        verticalCenterOffset = (fontSize100VerticalCenterOffset * textSize) / 100f;
    }

    /**
     * Rescale to fit within bounds if necessary.
     *
     * @param widthMeasureSpec From {@link View#onMeasure(int, int)}
     * @param heightMeasureSpec From {@link View#onMeasure(int, int)}
     */
    public void setBounds(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        if (maxWidth > screenWidth) {
            maxWidth = screenWidth;
        }

        int maxHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        if (maxHeight > screenHeight * 0.4) {
            maxHeight = (int)(screenHeight * 0.4);
        }

        Log.d(TAG, "Bounds are " + maxWidth + "x" + maxHeight);

        if (isFullKeyboard) {
            scaleToScreenWidth(maxWidth, maxHeight);

            return;
        }

        if (width > maxWidth) {
            height = (height * maxWidth) / width;
            width = maxWidth;
        }

        if (height > maxHeight) {
            // This shouldn't happen; how can we become too high?
            width = (width * maxHeight) / height;
            height = maxHeight;
        }

        Log.d(TAG, "Size computed to " + width + "x" + height);
    }
}
