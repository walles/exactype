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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;

/**
 * A box somewhere above the keyboard showing where the keyboard is being touched.
 */
public class FeedbackWindow implements ExactypeView.UpdatedListener {
    private PopupWindow window;
    private ImageView imageView;
    private Canvas canvas;

    private final Context context;

    private final ExactypeView exactypeView;

    private final int fadeoutDurationMs;

    @Nullable
    private ViewPropertyAnimator fadeout;

    private int size;

    private float lastX;
    private float lastY;

    public FeedbackWindow(Context context, ExactypeView exactypeView) {
        this.exactypeView = exactypeView;
        exactypeView.setUpdatedListener(this);

        this.context = context;

        fadeoutDurationMs =
            context.getResources().getInteger(android.R.integer.config_longAnimTime);
    }

    private void setUpCanvas() {
        size = exactypeView.getHeight() / 3;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        imageView = new ImageView(context);
        imageView.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));

        canvas = new Canvas(bitmap);

        window = new PopupWindow(imageView, size, size);
        window.setClippingEnabled(false);
    }

    /**
     * Copy pixels from our ExactypeView into our ImageView
     */
    private void update() {
        if (canvas == null) {
            setUpCanvas();
        }

        // Use half transparent background color for pixels outside of the keyboard
        canvas.drawColor(
            (KeyboardTheme.BACKGROUND_COLOR & 0xffffff) | 0x80_00_00_00,
            PorterDuff.Mode.SRC);

        // Copy keyboard pixels into our .canvas
        Rect source = new Rect(
            (int)(lastX - size / 2f),
            (int)(lastY - size / 2f),
            (int)(lastX + size / 2f),
            (int)(lastY + size / 2f));
        Rect dest = new Rect(0, 0, size - 1, size -1);
        canvas.drawBitmap(exactypeView.getBitmap(), source, dest, null);

        // Need to invalidate for move-around tracking to work
        imageView.invalidate();
    }

    public void onKeyboardChanged() {
        if (fadeout == null) {
            update();
        } else {
            // We're fading, we should keep displaying whatever the user pressed to initiate the
            // keyboard change.
        }
    }

    /**
     * Display the feedback window
     * @param x The X coordinate where the user is touching the view
     * @param y The Y coordinate where the user is touching the view
     */
    public void show(float x, float y) {
        if (fadeout != null) {
            fadeout.cancel();
            fadeout = null;
        }

        lastX = x;
        lastY = y;
        update();

        float xCenter = exactypeView.getWidth() / 2f;
        float x0 = xCenter - (window.getWidth() / 2f);

        float y0 = -window.getHeight();

        imageView.setAlpha(1.0f);
        window.showAtLocation(exactypeView, Gravity.NO_GRAVITY, (int)x0, (int)y0);
    }

    /**
     * Update the image shown by the feedback window
     * @param x The X coordinate where the user is touching the view
     * @param y The Y coordinate where the user is touching the view
     */
    public void update(float x, float y) {
        lastX = x;
        lastY = y;
        update();
    }

    /**
     * Close the feedback window
     */
    public void close() {
        if (fadeout != null) {
            // Fadeout already in progress, just let that one run its course
            return;
        }

        if (imageView == null) {
            // https://fabric.io/johan-walles-projects/android/apps/com.gmail.walles.johan.exactype/issues/57b18503ffcdc04250abc844
            //
            // Note that if get more NPEs on imageView accesses we should tag it with @Nullable
            return;
        }

        fadeout = imageView.animate();
        fadeout.alpha(0f).setDuration(fadeoutDurationMs).setListener(
            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    window.dismiss();
                    fadeout = null;
                }
            }
        );
    }
}
