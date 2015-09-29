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
import android.view.View;

public class PopupKeyboardView extends View {
    private KeyCoordinator keyCoordinator;
    private KeyboardTheme theme;
    private String keys;

    private float textSize;

    public PopupKeyboardView(Context context) {
        super(context);

        theme = new KeyboardTheme(getResources().getDisplayMetrics());
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setKeys(String keys) {
        this.keys = keys;
        this.keyCoordinator = new KeyCoordinator(new String[] { keys });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Clear the background
        canvas.drawColor(KeyboardTheme.BACKGROUND_COLOR);

        // Draw surrounding box
        canvas.drawRect(0, 0, getWidth() - 1, getHeight() - 1, theme.getStrokePaint());

        // Draw keys
        for (KeyCoordinator.KeyInfo keyInfo : keyCoordinator.getKeys()) {
            canvas.drawText(
                Character.toString(keyInfo.character),
                keyInfo.getX(),
                keyInfo.getY() + theme.getVerticalCenterOffset(),
                theme.getTextPaint());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        theme.setBounds(widthMeasureSpec, heightMeasureSpec, keys, textSize);
        keyCoordinator.setSize(theme.getWidth(), theme.getHeight());
        setMeasuredDimension(theme.getWidth(), theme.getHeight());
    }
}
