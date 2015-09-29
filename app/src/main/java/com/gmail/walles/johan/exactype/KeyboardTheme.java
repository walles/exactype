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
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class KeyboardTheme {
    public static final int BACKGROUND_COLOR = Color.BLUE;
    private static final int COLOR = Color.WHITE;

    private static final String ALL_HEIGHTS = "M";
    private static final String LONG_ROW = "qwertyuiopå";

    private static final float LETTER_ZOOM_OUT_FACTOR = 3f;
    private static final int POPUP_BORDER_WIDTH_DP = 2;

    private final Paint textPaint;
    private final Paint strokePaint;
    private final int fontSize100HeightPx;
    private final float fontSize100CharWidthPx;
    private final float fontSize100VerticalCenterOffset;

    private int width;
    private int height;
    private float verticalCenterOffset;

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
        fontSize100CharWidthPx = bounds.width() / (float)LONG_ROW.length();

        // From: http://www.slideshare.net/rtc1/intro-todrawingtextandroid
        textPaint.getTextBounds(ALL_HEIGHTS, 0, ALL_HEIGHTS.length(), bounds);
        fontSize100VerticalCenterOffset = -bounds.top - bounds.height() / 2;
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
        verticalCenterOffset = (fontSize100VerticalCenterOffset * textSize) / 100f;

        float fontHeight = (fontSize100HeightPx * textSize) / 100f;
        height = Math.round(fontHeight * LETTER_ZOOM_OUT_FACTOR);

        float fontWidth = (fontSize100CharWidthPx * textSize) / 100f;
        width = Math.round(fontWidth * keys.length() * LETTER_ZOOM_OUT_FACTOR);

        textPaint.setTextSize(textSize);
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

    /**
     * Rescale to fit within bounds if necessary.
     *
     * @param widthMeasureSpec From {@link View#onMeasure(int, int)}
     * @param heightMeasureSpec From {@link View#onMeasure(int, int)}
     */
    public void setBounds(int widthMeasureSpec, int heightMeasureSpec) {
        if (width > View.MeasureSpec.getSize(widthMeasureSpec)) {
            height = (height * View.MeasureSpec.getSize(widthMeasureSpec)) / width;
            width = View.MeasureSpec.getSize(widthMeasureSpec);
        }

        if (height > View.MeasureSpec.getSize(heightMeasureSpec)) {
            // This shouldn't happen; how can we become too high?
            width = (width * View.MeasureSpec.getSize(heightMeasureSpec)) / height;
            height = View.MeasureSpec.getSize(heightMeasureSpec);
        }
    }
}
