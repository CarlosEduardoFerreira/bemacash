package com.kaching123.tcr.fragment.user;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.user.LoginFragment.Mode;
import com.kaching123.tcr.fragment.user.LoginFragment.OnDismissListener;
import com.kaching123.tcr.fragment.user.LoginFragment.OnLoginCompleteListener;

/**
 * Created by vkompaniets on 07.03.14.
 */
@EFragment (R.layout.login_outer_fragment)
public class LoginOuterFragment extends SuperBaseDialogFragment {

    private static final String DIALOG_NAME = LoginOuterFragment.class.getSimpleName();

    @ViewById
    protected TextView registerSerialLabel;

    @FragmentArg
    protected Mode mode;

    private OnLoginCompleteListener onLoginCompleteListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);

        LoginFragment.show(this, onLoginCompleteListener, new OnDismissListener() {
            @Override
            public void onDismiss() {
                dismiss();
            }
        }, mode);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.AppTheme_Fullscreen);
        return dialog;
    }

    @AfterViews
    protected void onIniView() {
        registerSerialLabel.setText(getString(R.string.dashboard_register_serial, getApp().getRegisterSerial()));
    }

    public void setOnLoginCompleteListener(OnLoginCompleteListener onLoginCompleteListener) {
        this.onLoginCompleteListener = onLoginCompleteListener;
    }

    public static void show(FragmentActivity activity, Mode mode) {
        show(activity, null, mode);
    }

    public static void show(FragmentActivity activity, OnLoginCompleteListener listener, Mode mode) {
        LoginOuterFragment fragment = LoginOuterFragment_.builder().mode(mode).build();
        fragment.setOnLoginCompleteListener(listener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }




}
