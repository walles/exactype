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
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.PopupWindow;

/**
 * A box somewhere above the keyboard showing where the keyboard is being touched.
 *
 * TODO:
 * * Test with base keyboard, both lowercase and caps
 * * Test with longpressing, should show first letters then numbers and things
 * * Test with popup keyboard
 * * Think about what we should show outside of our views, transparent perhaps?
 * * Test with rotating the phone 90 degrees
 * * Fade out on release rather than just disappearing?
 */
public class FeedbackWindow implements ExactypeView.UpdatedListener {
    private final PopupWindow window;
    private final ImageView imageView;
    private final Canvas canvas;

    private final ExactypeView exactypeView;

    private int size;

    private float lastX;
    private float lastY;

    public FeedbackWindow(Context context, ExactypeView exactypeView) {
        this.exactypeView = exactypeView;
        exactypeView.setUpdatedListener(this);

        imageView = new ImageView(context);

        size = exactypeView.getHeight() / 2;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        imageView.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        canvas = new Canvas(bitmap);

        window = new PopupWindow(imageView, size, size);
        window.setClippingEnabled(false);
    }

    /**
     * Copy pixels from our ExactypeView into our ImageView
     */
    private void update() {
        // Copy keyboard pixels into our .canvas
        Rect source = new Rect(
            (int)(lastX - size / 2),
            (int)(lastY - size / 2),
            (int)(lastX + size / 2),
            (int)(lastY + size / 2));
        Rect dest = new Rect(0, 0, size - 1, size -1);
        canvas.drawBitmap(exactypeView.getBitmap(), source, dest, null);

        // FIXME: Do we need to trigger a refresh of the ImageView after the Canvas operation?
    }

    public void onKeyboardChanged() {
        update();
    }

    /**
     * Display the feedback window
     * @param x The X coordinate where the user is touching the view
     * @param y The Y coordinate where the user is touching the view
     */
    public void show(float x, float y) {
        lastX = x;
        lastY = y;
        update();

        float xCenter = exactypeView.getWidth() / 2;
        float x0 = xCenter - (window.getWidth() / 2);

        float y0 = -window.getHeight();

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
        window.dismiss();
    }
}
