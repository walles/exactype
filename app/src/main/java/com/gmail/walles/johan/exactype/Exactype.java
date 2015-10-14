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

import android.inputmethodservice.InputMethodService;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.PopupWindow;

import com.gmail.walles.johan.exactype.util.Timer;

import java.util.HashMap;
import java.util.Map;

public class Exactype extends InputMethodService {
    private static final String TAG = "Exactype";

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

    private static final String[] NUMERIC = new String[] {
        "1234567890",
        "&/:;()-+$",
        "@'\"*#?!,."
    };

    private final Map<Character, String> popupKeysForKey;

    private PopupKeyboardView popupKeyboardView;
    private PopupWindow popupKeyboardWindow;

    private FeedbackWindow feedbackWindow;

    private final ExactypeMode mode;

    private ExactypeView view;
    private EditorInfo editorInfo;

    private float popupX0;
    private float popupY0;

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

        feedbackWindow = new FeedbackWindow(view);

        return view;
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        // The initialCapsMode docs say that you should generally just take a non-zero value to mean
        // "start out in caps mode":
        // http://developer.android.com/reference/android/view/inputmethod/EditorInfo.html#initialCapsMode
        mode.setShifted(editorInfo.initialCapsMode != 0);

        this.editorInfo = editorInfo;
    }

    public void onLongPress() {
        mode.register(ExactypeMode.Event.LONG_PRESS);
    }

    public void onKeyTapped(char tappedKey) {
        popupKeyboardWindow.dismiss();
        Timer timer = new Timer();
        getCurrentInputConnection().commitText(Character.toString(tappedKey), 1);
        Log.d(TAG, "PERF: Committing a char took " + timer);

        mode.register(ExactypeMode.Event.INSERT_CHAR);
    }

    public void onDeleteTapped() {
        InputConnection inputConnection = getCurrentInputConnection();
        Timer timer = new Timer();
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

    public void onKeyboardModeSwitchRequested() {
        mode.register(ExactypeMode.Event.NEXT_MODE);
    }

    public void onActionTapped() {
        Timer timer = new Timer();
        if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            getCurrentInputConnection().commitText("\n", 1);
            Log.d(TAG, "PERF: Committing a newline took " + timer);

            mode.register(ExactypeMode.Event.INSERT_CHAR);

            return;
        }

        getCurrentInputConnection()
            .performEditorAction(editorInfo.imeOptions | EditorInfo.IME_MASK_ACTION);
        Log.d(TAG, "PERF: Performing editor action took " + timer);
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

        Log.d(TAG, String.format("Popup keyboard window size set to %dx%d",
            popupKeyboardView.getWidth(),
            popupKeyboardView.getHeight()));

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

    public void onPopupKeyboardTapped(float x, float y) {
        // FIXME: Are we even on the popup keyboard? Abort otherwise.

        float popupX = x - popupX0;
        float popupY = y - popupY0;

        onKeyTapped(popupKeyboardView.getClosestKey(popupX, popupY));
    }

    public void onTouchStart(float x, float y) {
        feedbackWindow.show(x, y);
    }

    public void onTouchMove(float x, float y) {
        feedbackWindow.update(x, y);
    }

    public void onTouchEnd() {
        feedbackWindow.close();
    }
}
