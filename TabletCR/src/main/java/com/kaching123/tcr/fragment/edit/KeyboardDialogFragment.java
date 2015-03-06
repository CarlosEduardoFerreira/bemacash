package com.kaching123.tcr.fragment.edit;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.KeyboardView;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/*
 * Created by gdubina on 14/11/13.
 */
@EFragment
public abstract class KeyboardDialogFragment extends StyledDialogFragment implements CustomEditBox.IKeyboardSupport {

    private static final int DOUBLE_PADDING = 40;

    @ViewById
    protected KeyboardView keyboard;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelSize(getPreferredContentWidth()),
                getDialog().getWindow().getAttributes().height);
        enablePositiveButtons(false);
    }

    protected int getPreferredContentWidth() {
        return R.dimen.sn_dialog_key_width;
    }

    protected View createDialogContentView() {
        View v = layoutInflater.inflate(R.layout.edit_base_keyboard_dialog_fragment, null, false);

        ViewGroup group = (ViewGroup) v.findViewById(R.id.dialog_content);
        if (R.dimen.sn_dialog_key_width != getPreferredContentWidth()) {
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) ( ( getResources().getDimensionPixelSize(getPreferredContentWidth()) - DOUBLE_PADDING * scale ) + 0.5f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(pixels, FrameLayout.LayoutParams.WRAP_CONTENT);
            group.setLayoutParams(layoutParams);
            group.invalidate();
        }


        layoutInflater.inflate(getDialogContentLayout(), group, true);
        return v;
    }


    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        keyboard.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        keyboard.detachEditView();
    }

    @Override
    protected void enablePositiveButtons(boolean enable) {
        enablePositiveButton(enable, greenBtnColor);
        keyboard.setEnterEnabled(enable);
    }
}
