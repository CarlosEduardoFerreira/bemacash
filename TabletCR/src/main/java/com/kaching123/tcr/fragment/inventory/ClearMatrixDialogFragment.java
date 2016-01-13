package com.kaching123.tcr.fragment.inventory;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import org.androidannotations.annotations.EFragment;

/**
 * Created by aakimov on 16/06/15.
 */

@EFragment
public class ClearMatrixDialogFragment extends StyledDialogFragment {
    private static final String DIALOG_NAME = ClearMatrixDialogFragment.class.getName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);

    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.clear_matrix_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.variants_clear;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return android.R.string.no;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return android.R.string.yes;
    }

    @Override
    protected StyledDialogFragment.OnDialogClickListener getPositiveButtonListener() {

        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                Fragment f = getTargetFragment();
                if (f instanceof VariantsMatrixFragment) {
                    ((VariantsMatrixFragment) f).clearChildrenInMatrixItem();
                }
                return true;
            }
        };
    }

    public static void show(FragmentActivity activity, Fragment f) {
        ClearMatrixDialogFragment clearMatrixDialogFragment = ClearMatrixDialogFragment_.builder().build();
        clearMatrixDialogFragment.setTargetFragment(f, 0);
        DialogUtil.show(activity, DIALOG_NAME, clearMatrixDialogFragment);
    }
}
