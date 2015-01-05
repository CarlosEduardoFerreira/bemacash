package com.kaching123.tcr.fragment.wireless;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * Created by vkompaniets on 30.07.2014.
 */
@EFragment
public class WarrantyFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = WarrantyFragment.class.getSimpleName();

    @ViewById
    protected EditText warranty;

    private IWarrantyListener listener;

    @ColorRes(R.color.light_gray)
    protected int normalTextColor;

    @ColorRes(R.color.subtotal_tax_empty)
    protected int errorTextColor;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelSize(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);

        warranty.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    onSubmitForm();
                    return true;
                }
                return false;
            }
        });
        checkPositiveButtonCondition();
    }

    @AfterTextChange
    protected void warrantyAfterTextChanged(Editable s) {
        checkPositiveButtonCondition();
    }

    private void checkPositiveButtonCondition() {
        boolean warrantyOk = false;
        try {
            int i = Integer.parseInt(warranty.getText().toString());
            warrantyOk = i >= 0;
        } catch (NumberFormatException ignore) {
        } catch (NullPointerException ignore) { }

        enablePositiveButton(warrantyOk, normalBtnColor);
        warranty.setTextColor(warrantyOk ? normalTextColor : errorTextColor);
    }

    public void setListener(IWarrantyListener listener) {
        this.listener = listener;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.warranty_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_unit_warranty_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                onSubmitForm();
                return false;
            }
        };
    }

    private void onSubmitForm(){
        try {
            int i = Integer.parseInt(warranty.getText().toString());
            if (listener != null)
                listener.onConfirm(i);
        } catch (NumberFormatException e) {}
    }

    public static interface IWarrantyListener {
        void onConfirm(int warranty);
    }

    public static void show(FragmentActivity activity, IWarrantyListener listener){
        DialogUtil.show(activity, DIALOG_NAME, WarrantyFragment_.builder().build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity){
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
