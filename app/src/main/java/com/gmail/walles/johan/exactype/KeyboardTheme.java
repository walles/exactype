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

public class KeyboardTheme {
    private static final String ALL_HEIGHTS = "M";
    private static final String LONG_ROW = "qwertyuiopå";

    private final Paint paint;
    private final float fontSize100HeightPx;
    private final float fontSize100CharWidthPx;

    public KeyboardTheme() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTextSize(100);

        Rect bounds = new Rect();
        paint.getTextBounds(ALL_HEIGHTS, 0, ALL_HEIGHTS.length(), bounds);
        fontSize100HeightPx = bounds.height();

        paint.getTextBounds(LONG_ROW, 0, LONG_ROW.length(), bounds);
        fontSize100CharWidthPx = bounds.width() / (float)LONG_ROW.length();
    }

    public Paint getPaint() {
        return paint;
    }

    /**
     * Helper for {@link android.view.View#onMeasure(int, int)}
     *
     * @param widthMeasureSpec Width spec from onMeasure()
     * @param heightMeasureSpec Height spec from onMeasure()
     * @param keys The single line of keys the keyboard should display
     * @param textSize The wanted font size to use
     */
    public void setBounds(int widthMeasureSpec, int heightMeasureSpec, String keys, float textSize) {

    }

    public int getWidth() {

    }

    public int getHeight() {

    }
}
