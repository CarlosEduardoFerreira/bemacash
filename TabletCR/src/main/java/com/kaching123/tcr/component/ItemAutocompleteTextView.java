package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

/**
 * Created by idyuzheva on 13.07.2015.
 */
public class ItemAutocompleteTextView extends AutoCompleteTextView {
    public ItemAutocompleteTextView(Context context) {
        super(context);
    }

    public ItemAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == 1) {
            InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
