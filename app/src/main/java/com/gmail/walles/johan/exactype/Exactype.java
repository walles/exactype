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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class Exactype extends InputMethodService {
    private final static String[] UNSHIFTED = new String[] {
        "qwertyuiopå",
        "asdfghjklöä",
        "⇧zxcvbnm⌫" // ⇧ = SHIFT, ⌫ = Backspace
    };

    private final static String[] SHIFTED = new String[] {
        "QWERTYUIOPÅ",
        "ASDFGHJKLÖÄ",
        "⇧ZXCVBNM⌫" // ⇧ = SHIFT, ⌫ = Backspace
    };

    private boolean shifted = false;
    private ExactypeView view;

    @Override
    public View onCreateInputView() {
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
    }

    public void setShifted(boolean shifted) {
        if (shifted == this.shifted) {
            return;
        }
        this.shifted = shifted;
        view.setRows(shifted ? SHIFTED : UNSHIFTED);
    }

    public void onKeyTapped(char tappedKey) {
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
}
