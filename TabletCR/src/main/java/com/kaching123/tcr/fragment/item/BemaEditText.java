package com.kaching123.tcr.fragment.item;

import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.kaching123.tcr.Logger;

/**
 * Created by ferre on 30/01/2017.
 */

public class BemaEditText extends EditText {

    public BemaEditText(Context context) {
        super(context);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // User has pressed Back key. So hide the keyboard
            InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
            // TODO: Hide your view as you do it in your activity
            Logger.d("BemaCarl.key code: " + keyCode);
        }
        return false;
    }

}
