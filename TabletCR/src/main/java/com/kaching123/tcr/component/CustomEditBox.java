package com.kaching123.tcr.component;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

import com.kaching123.tcr.Logger;

/**
 * Created by gdubina on 07/11/13.
 */
public class CustomEditBox extends EditText {

    private IEditListener editListener;
    private IKeyboardSupport keyboardSupportConteiner;

    public CustomEditBox(Context context) {
        super(context);
        setLongClickable(false);
        hideKeyboard();
    }

    public CustomEditBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLongClickable(false);
        hideKeyboard();
    }

    public CustomEditBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLongClickable(false);
        hideKeyboard();
    }

    public boolean valid() {
        return false;
    }

   /* @Override
    public void selectAll() {

    }*/

    public void hideKeyboard()
    {
        super.setInputType(0);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        Logger.d("TEXT_VIEW: onFocusChanged %b", focused);
        if (focused) {
            attachMe2Keyboard();
        } else {
            detachMe4Keyboard();
        }
    }

    private IKeyboardSupport getKeyboardListener() {
        return keyboardSupportConteiner;
    }

    public void setKeyboardSupportConteiner(IKeyboardSupport keyboardSupportConteiner) {
        this.keyboardSupportConteiner = keyboardSupportConteiner;
    }

    public void attachMe2Keyboard() {
        getKeyboardListener().attachMe2Keyboard(this);
    }

    private void detachMe4Keyboard() {
        getKeyboardListener().detachMe4Keyboard(this);
    }

    public void onEnter() {
        if (editListener != null) {
            boolean b = editListener.onChanged(getText().toString());
            if (b) {
                detachMe4Keyboard();
            }
        }
    }

    public void onDelete() {
        String str = getText().toString();
        if (str.length() == 0)
            return;
        setText(str.substring(0, str.length() - 1));
    }

    public void clear() {
        String str = getText().toString();
        if (str.length() == 0)
            return;
        setText("");
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }

    public void setEditListener(IEditListener editListener) {
        this.editListener = editListener;
    }

    public interface IKeyboardSupport {
        void attachMe2Keyboard(CustomEditBox v);

        void detachMe4Keyboard(CustomEditBox v);
    }

    public static interface IEditListener {
        boolean onChanged(String text);
    }
}
