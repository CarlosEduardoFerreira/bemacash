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
public class CloseDrawerFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = CloseDrawerFragment.class.getSimpleName();

    private OnCloseDrawerListener closeDrawerListener;

    @FragmentArg
    protected boolean hasBackButton;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.shift_close_drawer_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_close_drawer;
    }

    @Override
    protected boolean hasNegativeButton() {
        return hasBackButton;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (closeDrawerListener != null) {
                    closeDrawerListener.onBack();
                }
                return true;
            }
        };
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

    public static void show(FragmentActivity activity, boolean hasBackButton, OnCloseDrawerListener listener) {
        CloseDrawerFragment fragment = CloseDrawerFragment_.builder().hasBackButton(hasBackButton).build();
        fragment.setCloseDrawerListener(listener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public void setCloseDrawerListener(OnCloseDrawerListener closeDrawerListener) {
        this.closeDrawerListener = closeDrawerListener;
    }

    public static interface OnCloseDrawerListener {
        void onBack();
    }

}
