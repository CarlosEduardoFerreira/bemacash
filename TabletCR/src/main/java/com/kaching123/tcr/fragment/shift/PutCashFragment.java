package com.kaching123.tcr.fragment.shift;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * Created by pkabakov on 06.12.13.
 */
@EFragment
public class PutCashFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = PutCashFragment.class.getSimpleName();

    @FragmentArg
    protected boolean hasDoneButton;

    @FragmentArg
    protected boolean isInsert;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.shift_put_cash_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return isInsert ? R.string.dlg_put_cash : R.string.dlg_remove_cash;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.shift_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.shift_dlg_heigth));
        setCancelable(false);
    }

    public static void show(FragmentActivity activity, boolean isInsert) {
        PutCashFragment fragment = PutCashFragment_.builder().isInsert(isInsert).build();
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

}
