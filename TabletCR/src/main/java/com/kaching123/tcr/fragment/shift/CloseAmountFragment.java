package com.kaching123.tcr.fragment.shift;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.DecimalEditFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 06.12.13.
 */
@EFragment
public class CloseAmountFragment extends DecimalEditFragment {

    private static final String DIALOG_NAME = CloseAmountFragment.class.getSimpleName();

    private static final BigDecimal MAX_VALUE = new BigDecimal("9999999.99");

    private OnEditAmountListener editAmountListener;

    @FragmentArg
    protected boolean skipDrawer;

    @FragmentArg
    protected boolean needSync;

    @Override
    protected BigDecimal getMaxValue() {
        return MAX_VALUE;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.shift_close_amount_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_close_amount;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_accept;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (editAmountListener != null) {
                    editAmountListener.onCancel();
                }
                return true;
            }
        };
    }

    @Override
    protected void attachViews() {
        super.attachViews();
        UiHelper.showPrice(editText, decimalValue);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
        enablePositiveButtons(decimalValue != null);
    }

    @Override
    protected void callCommand(String guid, BigDecimal value) {
        if (editAmountListener != null) {
            editAmountListener.onConfirm(skipDrawer, value, needSync);
        }
    }

    @Override
    protected BigDecimal validateForm() {
        BigDecimal value = getDecimalValue();
        BigDecimal maxValue = getMaxValue();
        return value != null && (maxValue == null || value.compareTo(maxValue) != 1) ? value : null;
    }

    public void setEditAmountListener(OnEditAmountListener editAmountListener) {
        this.editAmountListener = editAmountListener;
    }

    public static void show(FragmentActivity activity, boolean skipDrawer, BigDecimal amount, boolean needSync, OnEditAmountListener listener) {
        CloseAmountFragment fragment = CloseAmountFragment_.builder().skipDrawer(skipDrawer).needSync(needSync).decimalValue(amount).build();
        fragment.setEditAmountListener(listener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public static interface OnEditAmountListener {
        void onConfirm(boolean skipDrawer, BigDecimal value, boolean needSync);

        void onCancel();
    }
}
