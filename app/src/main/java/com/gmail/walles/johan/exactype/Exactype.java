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
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.PopupWindow;

import java.util.HashMap;
import java.util.Map;

public class Exactype extends InputMethodService {
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
        popupKeyboardWindow.showAtLocation(view, Gravity.CENTER, (int)x, (int)y);
    }
}
