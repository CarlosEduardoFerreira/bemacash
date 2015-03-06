package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

@EViewGroup(R.layout.component_keyboard_view)
public class KeyboardView extends FrameLayout {

    @ViewById
    protected ViewGroup numKeyboard;

    @ViewById
    protected ImageButton numEnter;

    @ViewById
    protected ImageButton numDot;

    @ViewById
    protected ImageButton numMinus;

    @ViewById
    protected ImageButton num0;

    @ViewById
    protected ImageButton numDelete;

    protected CustomEditBox currentTextView;

    public KeyboardView(Context context) {
        super(context);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @AfterViews
    protected void init() {
        setMinusVisible(false);
        for (int i = 0; i < numKeyboard.getChildCount(); i++) {
            numKeyboard.getChildAt(i).setOnClickListener(listener);
        }
    }

    private void appendChar(char ch) {
        if (currentTextView == null)
            return;
        currentTextView.append(String.valueOf(ch));
    }

    private void doEnter() {
        if (currentTextView == null)
            return;
        currentTextView.onEnter();
    }

    private void doDelete() {
        if (currentTextView == null)
            return;
        currentTextView.onDelete();
    }

    public void setEnterEnabled(boolean enabled) {
        numEnter.setEnabled(enabled);
    }
    public void setNumMinusEnabled(boolean enabled) {
        numMinus.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled) {
        setEnterEnabled(enabled);
        setDotEnabled(enabled);
        numDelete.setEnabled(enabled);
        for (int i = 0; i < numKeyboard.getChildCount(); i++) {
            numKeyboard.getChildAt(i).setEnabled(enabled);
        }
        // do not see any need of numMinus key.
        numMinus.setEnabled(false);
    }

    public void setDotEnabled(boolean enabled) {
        numDot.setEnabled(enabled);
    }

    public void setMinusVisible(boolean enabled) {
        numMinus.setEnabled(enabled);
        //numMinus.setVisibility(enabled ? View.VISIBLE : View.GONE);
        //(((GridLayout.LayoutParams)num0.getLayoutParams())).columnSpec = 2;
    }

    public void attachEditView(CustomEditBox currentTextView) {
        this.currentTextView = currentTextView;
    }

    public void detachEditView() {
        attachEditView(null);
    }

    private static char getChar(int id) {
        switch (id) {
            case R.id.num0:
                return '0';
            case R.id.num1:
                return '1';
            case R.id.num2:
                return '2';
            case R.id.num3:
                return '3';
            case R.id.num4:
                return '4';
            case R.id.num5:
                return '5';
            case R.id.num6:
                return '6';
            case R.id.num7:
                return '7';
            case R.id.num8:
                return '8';
            case R.id.num9:
                return '9';
            case R.id.num_dot:
                return '.';
            case R.id.num_minus:
                return '-';
        }
        return 0;
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.num_delete:
                    doDelete();
                    return;
                case R.id.num_enter:
                    doEnter();
                    return;
            }
            char ch = getChar(view.getId());
            if (ch == 0)
                return;
            appendChar(ch);
        }
    };

}
