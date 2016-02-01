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

import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.gmail.walles.johan.exactype.emoji.EmojiView;

public class ExactypeService extends InputMethodService {
    private ExactypeView keyboardView;
    private EmojiView emojiView;
    private SwitcherView switcherView;

    @Override
    public View onCreateInputView() {
        keyboardView = new ExactypeView(this);

        emojiView = new EmojiView(this);
        switcherView = new SwitcherView(this, keyboardView, emojiView);

        return switcherView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Make sure we don't leave stray popup windows behind that are impossible to get rid of
        keyboardView.closePopups();

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
        keyboardView.setShifted(editorInfo.initialCapsMode != 0);

        if ((editorInfo.inputType & InputType.TYPE_MASK_CLASS) != InputType.TYPE_CLASS_TEXT) {
            keyboardView.setNumeric();
        }
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        // Johan finds the full screen mode ugly and confusing
        return false;
    }

    @Override
    public void onWindowHidden() {
        keyboardView.closePopups();
    }
}
