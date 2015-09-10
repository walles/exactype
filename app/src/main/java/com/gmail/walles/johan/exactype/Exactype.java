package com.gmail.walles.johan.exactype;

import android.inputmethodservice.InputMethodService;
import android.view.View;

public class Exactype extends InputMethodService {
    @Override
    public View onCreateInputView() {
        ExactypeView view = new ExactypeView(this, null);
        return view;
    }
}
