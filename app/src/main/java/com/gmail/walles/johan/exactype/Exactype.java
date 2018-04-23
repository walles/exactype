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

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.PopupWindow;

import com.crashlytics.android.answers.CustomEvent;
import com.gmail.walles.johan.exactype.util.LoggingUtils;
import com.gmail.walles.johan.exactype.util.Timer;
import com.gmail.walles.johan.exactype.util.VibrationUtils;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class Exactype
    extends InputMethodService
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String PERF_EVENT = "Perf";
    private int vibrate_duration_ms = SettingsActivity.DEFAULT_VIBRATE_DURATION_MS;

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

    // Default protection for testing purposes
    static final String[] NUMERIC = new String[] {
        "1234567890",
        "@&/:;()-+$",
        "'\"*%#?!,."
    };

    private final Map<Character, String> popupKeysForKey;

    private PopupKeyboardView popupKeyboardView;
    private PopupWindow popupKeyboardWindow;

    // Protected for testing purposes, should otherwise be private
    protected FeedbackWindow feedbackWindow;

    private final ExactypeMode mode;

    private ExactypeView view;

    private float popupX0;
    private float popupY0;

    @Nullable
    private Vibrator vibrator;

    private ExactypeExecutor inputConnectionExecutor;

    // We override this method only to add the @Nullable annotation and get the corresponding
    // warnings
    @Override
    @Nullable
    public InputConnection getCurrentInputConnection() {
        return super.getCurrentInputConnection();
    }

    @Override
    public void onCreate() {
        LoggingUtils.setUpLogging(this);

        super.onCreate();

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        vibrate_duration_ms =
            preferences.getInt(SettingsActivity.VIBRATE_DURATION_MS_KEY,
                SettingsActivity.DEFAULT_VIBRATE_DURATION_MS);

        inputConnectionExecutor = new ExactypeExecutor();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (SettingsActivity.VIBRATE_DURATION_MS_KEY.equals(key)) {
            vibrate_duration_ms =
                preferences.getInt(SettingsActivity.VIBRATE_DURATION_MS_KEY,
                    SettingsActivity.DEFAULT_VIBRATE_DURATION_MS);
        }
    }

    public Exactype() {
        popupKeysForKey = new HashMap<>();

        // FIXME: Maybe we should implicitly have the base key at the end of each of these lists?

        // Since we already have å and ä on the primary keyboard, they shouldn't be part of the
        // popup keys for a
        popupKeysForKey.put('a', "@áàa");
        popupKeysForKey.put('A', "@ÁÀA");
        popupKeysForKey.put('e', "éèëe");
        popupKeysForKey.put('E', "ÉÈË€E");

        mode = new ExactypeMode(UNSHIFTED, SHIFTED, NUMERIC);
    }

    @Override
    public View onCreateInputView() {
        popupKeyboardView = new PopupKeyboardView(this);
        popupKeyboardWindow = new PopupWindow(popupKeyboardView);

        view = new ExactypeView(this);
        mode.addModeChangeListener(view);

        feedbackWindow = new FeedbackWindow(this, view);

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Make sure we don't leave stray popup windows behind that are impossible to get rid of
        if (feedbackWindow != null) {
            feedbackWindow.close();
        }
        if (popupKeyboardWindow != null) {
            popupKeyboardWindow.dismiss();
        }

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        if (restarting) {
            return;
        }

        // The initialCapsMode docs say that you should generally just take a non-zero value to mean
        // "start out in caps mode":
        // http://developer.android.com/reference/android/view/inputmethod/EditorInfo.html#initialCapsMode
        mode.setShifted(editorInfo.initialCapsMode != 0);

        if ((editorInfo.inputType & InputType.TYPE_MASK_CLASS) != InputType.TYPE_CLASS_TEXT) {
            mode.setNumeric();
        }
    }

    public void onLongPress(float x, float y) {
        feedbackWindow.show(x, y);

        mode.register(ExactypeMode.Event.LONG_PRESS);
    }

    protected void enqueue(Runnable inputConnectionAction) {
        inputConnectionExecutor.execute(inputConnectionAction);
    }

    public void onKeyTapped(final char tappedKey) {
        popupKeyboardWindow.dismiss();
        enqueue(() -> {
            Timer timer = new Timer();

            final InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection == null) {
                return;
            }

            inputConnection.commitText(Character.toString(tappedKey), 1);
            LoggingUtils.logCustom(new CustomEvent(PERF_EVENT).putCustomAttribute(
                "Commit char ms", timer.getMs()));
        });

        mode.register(ExactypeMode.Event.INSERT_CHAR);
    }

    public void onDeleteTapped() {
        enqueue(() -> {
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
            LoggingUtils.logCustom(new CustomEvent(PERF_EVENT).putCustomAttribute(
                "Delete char ms", timer.getMs()));
        });
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

    protected boolean queueIsEmpty() {
        return inputConnectionExecutor.isEmpty();
    }

    public void onDeleteHeld() {
        feedbackWindow.close();

        if (!queueIsEmpty()) {
            // Don't enqueue new things repeatedly if there are already outstanding entries. This is
            // best practices from HyperKey on my DOS machine ages ago. I still miss it.
            return;
        }

        enqueue(() -> {
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
                LoggingUtils.logCustom(new CustomEvent(PERF_EVENT).putCustomAttribute(
                    "Delete word ms", timer.getMs()));
            } else {
                // Delete selection
                timer.addLeg("delete selection");
                inputConnection.commitText("", 1);
                LoggingUtils.logCustom(new CustomEvent(PERF_EVENT).putCustomAttribute(
                    "Delete selection ms", timer.getMs()));
            }
        });

        VibrationUtils.vibrate(vibrator, vibrate_duration_ms);
    }

    public void onKeyboardModeSwitchRequested() {
        mode.register(ExactypeMode.Event.NEXT_MODE);
    }

    public void onActionTapped() {
        final EditorInfo editorInfo = getCurrentInputEditorInfo();

        enqueue(() -> {
            Timer timer = new Timer();

            final InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection == null) {
                return;
            }

            if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
                inputConnection.commitText("\n", 1);
                LoggingUtils.logCustom(new CustomEvent(PERF_EVENT).putCustomAttribute(
                    "Commit newline ms", timer.getMs()));

                mode.register(ExactypeMode.Event.INSERT_CHAR);

                return;
            }

            inputConnection.
                performEditorAction(editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION);
            LoggingUtils.logCustom(new CustomEvent(PERF_EVENT).putCustomAttribute(
                "Perform editor action ms", timer.getMs()));
        });
    }

    public void onRequestPopupKeyboard(char baseKey, float x, float y) {
        String popupKeys = popupKeysForKey.get(baseKey);
        if (popupKeys == null) {
            // No popup keys available for this keypress
            return;
        }

        popupKeyboardView.setKeys(popupKeys);
        popupKeyboardView.setTextSize(view.getTextSize());

        popupKeyboardWindow.setWidth(popupKeyboardView.getMeasuredWidth());
        popupKeyboardWindow.setHeight(popupKeyboardView.getMeasuredHeight());

        // Without this the popup window will be constrained to the inside of the keyboard view
        popupKeyboardWindow.setClippingEnabled(false);

        Timber.d("Popup keyboard window size set to %dx%d",
            popupKeyboardView.getWidth(),
            popupKeyboardView.getHeight());

        popupX0 = x;
        if (popupX0 + popupKeyboardWindow.getWidth() > view.getWidth()) {
            // Make sure the popup keyboard is left enough to not extend outside of the screen
            popupX0 = view.getWidth() - popupKeyboardWindow.getWidth();
        }
        popupY0 = y;

        // Note that the gravity here decides *where the popup window anchors inside its parent*.
        //
        // This means that if we want the popup window anywhere but to the bottom right of where
        // the user touched, we'll need do the math ourselves.
        popupKeyboardWindow.showAtLocation(view, Gravity.NO_GRAVITY, (int)popupX0, (int)popupY0);
    }

    public boolean isPopupKeyboardShowing() {
        return popupKeyboardWindow.isShowing();
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        // Johan finds the full screen mode ugly and confusing
        return false;
    }

    public void onPopupKeyboardTapped(float x, float y) {
        // FIXME: Are we even on the popup keyboard? Abort otherwise.

        float popupX = x - popupX0;
        float popupY = y - popupY0;

        onKeyTapped(popupKeyboardView.getClosestKey(popupX, popupY));
    }

    public void onTouchStart() {
        VibrationUtils.vibrate(vibrator, vibrate_duration_ms);
    }

    public void onTouchMove(float x, float y) {
        feedbackWindow.update(x, y);
    }

    public void onTouchEnd() {
        feedbackWindow.close();
    }

    @Override
    public void onWindowHidden() {
        feedbackWindow.close();
    }
}
