package com.kaching123.tcr.fragment.user;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.user.LoginFragment.Mode;
import com.kaching123.tcr.fragment.user.LoginFragment.OnDismissListener;
import com.kaching123.tcr.fragment.user.LoginFragment.OnLoginCompleteListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by vkompaniets on 07.03.14.
 */
@EFragment(R.layout.login_outer_fragment)
public class LoginOuterFragment extends SuperBaseDialogFragment {

    private static final String DIALOG_NAME = LoginOuterFragment.class.getSimpleName();
    private final String VER = "Ver: ";

    @ViewById
    protected TextView registerSerialLabel, versionName;

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
        versionName.setText(getVersionName());
    }

    private String getVersionName() {
        PackageManager manager = getActivity().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            return VER + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
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
