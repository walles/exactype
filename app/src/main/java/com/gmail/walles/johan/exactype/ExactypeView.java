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
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class ExactypeView extends View {
    private static final String TAG = "Exactype";
    private final Paint foreground;
    private float verticalCenterOffset;

    private final static String[] ROWS = new String[] {
            "qwertyuiopå",
            "asdfghjklöä",
            "SzxcvbnmB" // S=SHIFT, B=Backspace
    };

    public ExactypeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
        foreground.setColor(Color.WHITE);
        foreground.setTextAlign(Paint.Align.CENTER);

        // FIXME: Compute a good size
        final int FONT_SIZE_DP = 50;

        float fontSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                FONT_SIZE_DP, getResources().getDisplayMetrics());
        foreground.setTextSize(fontSizePx);

        // From: http://www.slideshare.net/rtc1/intro-todrawingtextandroid
        Rect bounds = new Rect();
        foreground.getTextBounds("M", 0, 1, bounds);
        Log.i(TAG, bounds.toString());
        verticalCenterOffset = -bounds.top - bounds.height() / 2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // FIXME: Compute coordinates for all keys:
        // http://developer.android.com/training/custom-views/custom-drawing.html#layoutevent
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Clear the background
        canvas.drawColor(Color.BLACK);

        // Draw the keys
        for (int row_number = 0; row_number < ROWS.length; row_number++) {
            String row = ROWS[row_number];

            for (int char_number = 0; char_number < row.length(); char_number++) {
                char current_char = row.charAt(char_number);

                float x =
                        ((char_number + 1f) * getWidth()) / (row.length() + 1f);
                float y =
                        ((row_number + 1f) * getHeight()) / (ROWS.length + 1f) + verticalCenterOffset;

                canvas.drawText(Character.toString(current_char), x, y, foreground);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // FIXME: Make up a reasonable size
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
