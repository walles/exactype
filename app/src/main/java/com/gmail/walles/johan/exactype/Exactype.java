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

import java.util.HashMap;
import java.util.Map;

public class Exactype extends InputMethodService {
    private static final String TAG = "Exactype";

    private static final String[] UNSHIFTED = new String[] {
        "qwertyuiopå",
        "asdfghjklöä",
        "⇧zxcvbnm⌫" // ⇧ = SHIFT, ⌫ = Backspace
    };

    private static final String[] SHIFTED = new String[] {
        "QWERTYUIOPÅ",
        "ASDFGHJKLÖÄ",
        "⇧ZXCVBNM⌫" // ⇧ = SHIFT, ⌫ = Backspace
    };

    private static final String[] NUMERIC = new String[] {
        "1234567890",
        "&/:;()-+$",
        "@'\"*#?!,."
    };

    private final Map<Character, String> popupKeysForKey;

    private PopupKeyboardView popupKeyboardView;
    private PopupWindow popupKeyboardWindow;

    private boolean shifted = false;
    private ExactypeView view;
    private EditorInfo editorInfo;

    private float popupX0;
    private float popupY0;

    public Exactype() {
        popupKeysForKey = new HashMap<>();

        // FIXME: Since we already have å and ä on the primary keyboard, they really shouldn't be
        // part of the popup keys for a
        popupKeysForKey.put('a', "@áàäå");
    }

    @Override
    public View onCreateInputView() {
        popupKeyboardView = new PopupKeyboardView(this);
        popupKeyboardWindow = new PopupWindow(popupKeyboardView);

        view = new ExactypeView(this);
        view.setRows(shifted ? SHIFTED : UNSHIFTED);
        return view;
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        // The initialCapsMode docs say that you should generally just take a non-zero value to mean
        // "start out in caps mode":
        // http://developer.android.com/reference/android/view/inputmethod/EditorInfo.html#initialCapsMode
        setShifted(editorInfo.initialCapsMode != 0);
        this.editorInfo = editorInfo;
    }

    public void setShifted(boolean shifted) {
        this.shifted = shifted;
        view.setRows(shifted ? SHIFTED : UNSHIFTED);
    }

    public void onLongPress() {
        view.setRows(NUMERIC);
    }

    public void onKeyTapped(char tappedKey) {
        popupKeyboardWindow.dismiss();
        getCurrentInputConnection().commitText(Character.toString(tappedKey), 1);
        setShifted(false);
    }

    public void onDeleteTapped() {
        InputConnection inputConnection = getCurrentInputConnection();
        CharSequence selection = inputConnection.getSelectedText(0);
        if (TextUtils.isEmpty(selection)) {
            // Nothing selected, just backspace
            inputConnection.deleteSurroundingText(1, 0);
        } else {
            // Delete selection
            inputConnection.commitText("", 1);
        }
    }

    public void shiftTapped() {
        setShifted(!shifted);
    }

    public void onActionTapped() {
        if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            getCurrentInputConnection().commitText("\n", 1);
            return;
        }

        getCurrentInputConnection()
            .performEditorAction(editorInfo.imeOptions | EditorInfo.IME_MASK_ACTION);
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

        Log.d(TAG, String.format("Popup keyboard window size set to %dx%d",
            popupKeyboardView.getWidth(),
            popupKeyboardView.getHeight()));

        // Note that the gravity here decides *where the popup window anchors inside its parent*.
        //
        // This means that if we want the popup window anywhere but to the bottom right of where
        // the user touched, we'll need do the math ourselves.
        popupX0 = x;
        popupY0 = y;
        popupKeyboardWindow.showAtLocation(view, Gravity.NO_GRAVITY, (int)x, (int)y);
    }

    public boolean isPopupKeyboardShowing() {
        return popupKeyboardWindow.isShowing();
    }

    public void popupKeyboardTapped(float x, float y) {
        // FIXME: Are we even on the popup keyboard? Abort otherwise.

        float popupX = x - popupX0;
        float popupY = y - popupY0;

        onKeyTapped(popupKeyboardView.getClosestKey(popupX, popupY));
    }
}
