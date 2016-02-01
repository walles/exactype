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
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.PopupWindow;

import com.gmail.walles.johan.exactype.gestures.GestureDetector;
import com.gmail.walles.johan.exactype.util.Timer;
import com.gmail.walles.johan.exactype.util.VibrationUtil;

public class ExactypeView extends View implements ExactypeMode.ModeChangeListener {
    private static final String TAG = "Exactype";

    /**
     * While doing word-by-word deletion, how far back should we look when attempting to find the
     * previous word?
     */
    private static final int DELETE_LOOKBACK = 22;

    private static final String[] UNSHIFTED = new String[] {
        "qwertyuiopå",
        "asdfghjklöä",
        "zxcvbnm" // ⇧ = SHIFT, ⌫ = Backspace
    };

    private static final String[] SHIFTED = new String[] {
        "QWERTYUIOPÅ",
        "ASDFGHJKLÖÄ",
        "ZXCVBNM" // ⇧ = SHIFT, ⌫ = Backspace
    };

    @VisibleForTesting
    static final String[] NUMERIC = new String[] {
        "1234567890",
        "@&/:;()-+$",
        "'\"*#?!,."
    };

    private final GestureDetector gestureDetector;
    private final ExactypeMode mode;
    private final VibrationUtil vibrator;
    private final PopupKeys popupKeys;

    private final PopupKeyboardView popupKeyboardView;
    private final PopupWindow popupKeyboardWindow;
    private float popupX0;
    private float popupY0;

    @VisibleForTesting
    protected FeedbackWindow feedbackWindow;

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

    private final ExactypeService exactypeService;

    private final SingleThreadedExecutor inputConnectionExecutor;

    public ExactypeView(Context context) {
        super(context);
        exactypeService = (ExactypeService)context;

        theme = new KeyboardTheme(context.getResources().getDisplayMetrics());

        KeyboardGestureListener gestureListener = new KeyboardGestureListener(this);
        gestureDetector = new GestureDetector(exactypeService, new Handler(), gestureListener);

        inputConnectionExecutor = new SingleThreadedExecutor();

        mode = new ExactypeMode(UNSHIFTED, SHIFTED, NUMERIC);
        mode.addModeChangeListener(this);

        popupKeys = new PopupKeys(UNSHIFTED, SHIFTED, NUMERIC);
        popupKeyboardView = new PopupKeyboardView(context);
        popupKeyboardWindow = new PopupWindow(popupKeyboardView);

        feedbackWindow = new FeedbackWindow(context, this);

        vibrator = new VibrationUtil(context);
    }

    public float getTextSize() {
        return theme.getTextSize();
    }

    public void onModeChange(String[] rows, ExactypeMode.SwitchKey switchKey) {
        theme.setIsFullKeyboard();

        keyCoordinator = new KeyCoordinator(rows);
        keyCoordinator.setSize(theme.getWidth(), theme.getHeight());

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
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        theme.setSize(width, height);
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

    public char getClosestKey(float x, float y) {
        return keyCoordinator.getClosestKey(x, y);
    }

    public interface UpdatedListener {
        void onKeyboardChanged();
    }

    @Nullable
    protected InputConnection getCurrentInputConnection() {
        return exactypeService.getCurrentInputConnection();
    }

    protected void enqueue(Runnable inputConnectionAction) {
        inputConnectionExecutor.execute(inputConnectionAction);
    }

    public void onKeyTapped(final char tappedKey) {
        popupKeyboardWindow.dismiss();
        enqueue(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();

                final InputConnection inputConnection = getCurrentInputConnection();
                if (inputConnection == null) {
                    return;
                }

                inputConnection.commitText(Character.toString(tappedKey), 1);
                Log.d(TAG, "PERF: Committing a char took " + timer);
            }
        });

        mode.register(ExactypeMode.Event.INSERT_CHAR);
    }

    public void onActionTapped() {
        final EditorInfo editorInfo = exactypeService.getCurrentInputEditorInfo();

        enqueue(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();

                final InputConnection inputConnection = getCurrentInputConnection();
                if (inputConnection == null) {
                    return;
                }

                if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
                    inputConnection.commitText("\n", 1);
                    Log.d(TAG, "PERF: Committing a newline took " + timer);

                    mode.register(ExactypeMode.Event.INSERT_CHAR);

                    return;
                }

                inputConnection.
                    performEditorAction(editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION);
                Log.d(TAG, "PERF: Performing editor action took " + timer);
            }
        });
    }

    public void onDeleteTapped() {
        enqueue(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();

                final InputConnection inputConnection = getCurrentInputConnection();
                if (inputConnection == null) {
                    return;
                }

                timer.addLeg("get selection");
                CharSequence selection = inputConnection.getSelectedText(0);
                if (TextUtils.isEmpty(selection)) {
                    // Nothing selected, just backspace
                    timer.addLeg("backspace");
                    inputConnection.deleteSurroundingText(1, 0);
                } else {
                    // Delete selection
                    timer.addLeg("delete selection");
                    inputConnection.commitText("", 1);
                }
                Log.d(TAG, "PERF: Delete took " + timer);
            }
        });
    }

    public void onKeyboardModeSwitchRequested() {
        mode.register(ExactypeMode.Event.NEXT_MODE);
    }

    public void onLongPress(float x, float y) {
        feedbackWindow.show(x, y);

        mode.register(ExactypeMode.Event.LONG_PRESS);
    }

    public void onRequestPopupKeyboard(char baseKey, float x, float y) {
        String keys = popupKeys.getPopupKeysForKey(baseKey);
        if (keys == null) {
            // No popup keys available for this keypress
            return;
        }

        popupKeyboardView.setKeys(keys);
        popupKeyboardView.setTextSize(getTextSize());

        popupKeyboardWindow.setWidth(popupKeyboardView.getMeasuredWidth());
        popupKeyboardWindow.setHeight(popupKeyboardView.getMeasuredHeight());

        // Without this the popup window will be constrained to the inside of the keyboard view
        popupKeyboardWindow.setClippingEnabled(false);

        Log.d(TAG, String.format("Popup keyboard window size set to %dx%d",
            popupKeyboardView.getWidth(),
            popupKeyboardView.getHeight()));

        popupX0 = x;
        if (popupX0 + popupKeyboardWindow.getWidth() > getWidth()) {
            // Make sure the popup keyboard is left enough to not extend outside of the screen
            popupX0 = getWidth() - popupKeyboardWindow.getWidth();
        }
        popupY0 = y;

        // Note that the gravity here decides *where the popup window anchors inside its parent*.
        //
        // This means that if we want the popup window anywhere but to the bottom right of where
        // the user touched, we'll need do the math ourselves.
        popupKeyboardWindow.showAtLocation(this, Gravity.NO_GRAVITY, (int)popupX0, (int)popupY0);
    }

    public boolean isPopupKeyboardShowing() {
        return popupKeyboardWindow.isShowing();
    }

    public void onPopupKeyboardTapped(float x, float y) {
        // FIXME: Are we even on the popup keyboard? Abort otherwise.

        float popupX = x - popupX0;
        float popupY = y - popupY0;

        onKeyTapped(popupKeyboardView.getClosestKey(popupX, popupY));
    }

    public void onDeleteHeld() {
        closePopups();

        if (!queueIsEmpty()) {
            // Don't enqueue new things repetetively if there are already outstanding entries, this
            // is best practices from HyperKey on my DOS machine ages ago. I still miss it.
            return;
        }

        enqueue(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();

                final InputConnection inputConnection = getCurrentInputConnection();
                if (inputConnection == null) {
                    return;
                }

                timer.addLeg("get selection");
                CharSequence selection = inputConnection.getSelectedText(0);
                if (selection == null || selection.length() == 0) {
                    // Nothing selected, delete words
                    timer.addLeg("get preceding text");
                    CharSequence before = inputConnection.getTextBeforeCursor(DELETE_LOOKBACK, 0);
                    timer.addLeg("analyze text");
                    int to_delete = countCharsToDelete(before);
                    timer.addLeg("delete word");
                    inputConnection.deleteSurroundingText(to_delete, 0);
                } else {
                    // Delete selection
                    timer.addLeg("delete selection");
                    inputConnection.commitText("", 1);
                }
                Log.d(TAG, "PERF: Delete took " + timer);
            }
        });

        vibrator.vibrate();
    }

    protected boolean queueIsEmpty() {
        return inputConnectionExecutor.isEmpty();
    }

    /**
     * To remove the last word, how many chars would that be?
     * @param before Text before cursor
     * @return How many characters we should remove
     */
    private int countCharsToDelete(CharSequence before) {
        int index = before.length() - 1;

        // Count non-alphanumeric characters from the end
        while (index >=0 && !Character.isLetterOrDigit(before.charAt(index))) {
            index--;
        }

        // Count the number of alphanumeric characters preceding those
        while (index >=0 && Character.isLetterOrDigit(before.charAt(index))) {
            index--;
        }

        return before.length() - 1 - index;
    }

    public void onTouchStart() {
        vibrator.vibrate();
    }

    public void onTouchMove(float x, float y) {
        feedbackWindow.update(x, y);
    }

    public void onTouchEnd() {
        closePopups();
    }

    public void closePopups() {
        if (feedbackWindow != null) {
            feedbackWindow.close();
        }

        if (popupKeyboardWindow != null) {
            popupKeyboardWindow.dismiss();
        }
    }

    public void setShifted(boolean enabled) {
        mode.setShifted(enabled);
    }

    public void setNumeric() {
        mode.setNumeric();
    }
}
