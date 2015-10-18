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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

public class ExactypeView extends View implements ExactypeMode.ModeChangeListener {
    private final GestureDetector gestureDetector;
    private final GestureListener gestureListener;

    private KeyCoordinator keyCoordinator;
    private ExactypeMode.SwitchKey switchKey;

    private final KeyboardTheme theme;
    private UpdatedListener updatedListener;

    /**
     * We do all drawing operations via this bitmap.
     *
     * Except for in the ExactypeView, the contents of this bitmap is also displayed by the
     * FeedbackView.
     */
    private Bitmap bitmap;

    /**
     * This Canvas will always draw onto {@link #bitmap}.
     */
    private Canvas bitmapCanvas;

    public ExactypeView(Context context) {
        super(context);
        Exactype exactype = (Exactype)context;

        theme = new KeyboardTheme(context.getResources().getDisplayMetrics());

        gestureListener = new GestureListener(exactype);
        gestureDetector = new GestureDetector(exactype, new Handler(), gestureListener);
    }

    public float getTextSize() {
        return theme.getTextSize();
    }

    public void onModeChange(String[] rows, ExactypeMode.SwitchKey switchKey) {
        theme.setShouldComputeTextSize();

        keyCoordinator = new KeyCoordinator(rows);
        keyCoordinator.setSize(theme.getWidth(), theme.getHeight());

        gestureListener.setKeyCoordinator(keyCoordinator);

        this.switchKey = switchKey;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateBitmap(canvas.getWidth(), canvas.getHeight());

        canvas.drawBitmap(bitmap, 0, 0, null);

        updatedListener.onKeyboardChanged();
    }

    /**
     * This is the drawing logic for {@link #onDraw(Canvas)}.
     */
    private void updateBitmap(int width, int height) {
        prepareBitmap(width, height);

        // Clear the background
        bitmapCanvas.drawColor(KeyboardTheme.BACKGROUND_COLOR);

        // Draw the keys
        for (KeyCoordinator.KeyInfo keyInfo : keyCoordinator.getKeys()) {
            String drawMe;
            if (keyInfo.character == '⌫') {
                drawMe = "Bs";
            } else if (keyInfo.character == ExactypeMode.SwitchKey.MARKER) {
                drawMe = switchKey.decoration;
            } else {
                drawMe = Character.toString(keyInfo.character);
            }

            bitmapCanvas.drawText(
                drawMe,
                keyInfo.getX(),
                keyInfo.getY() + theme.getVerticalCenterOffset(),
                theme.getTextPaint());
        }
    }

    /**
     * Make sure {@link #bitmapCanvas} is ready to take drawing operations
     */
    private void prepareBitmap(int width, int height) {
        if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            if (bitmapCanvas == null) {
                bitmapCanvas = new Canvas(bitmap);
            } else {
                bitmapCanvas.setBitmap(bitmap);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        theme.setBounds(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(theme.getWidth(), theme.getHeight());
        keyCoordinator.setSize(theme.getWidth(), theme.getHeight());
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void setUpdatedListener(UpdatedListener updatedListener) {
        this.updatedListener = updatedListener;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public interface UpdatedListener {
        void onKeyboardChanged();
    }
}
