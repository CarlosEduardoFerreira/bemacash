package com.kaching123.tcr.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

/**
 * Created by gdubina on 11/11/13.
 */
@EFragment
public class WaitDialogFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "progressDialog";

    @FragmentArg
    protected String msg;

    @ViewById
    protected TextView progressMsg;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getDialog().getWindow().getAttributes().height);
        setCancelable(false);
        progressMsg.setText(msg);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.dialog_wait_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.wait_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return 0;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected boolean hasNegativeButton() {
        return false;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public static void show(FragmentActivity activity, String msg) {
        DialogUtil.show(activity, DIALOG_NAME, WaitDialogFragment_.builder().msg(msg).build());
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
