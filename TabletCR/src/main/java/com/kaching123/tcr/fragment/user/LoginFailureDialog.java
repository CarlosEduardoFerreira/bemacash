package com.kaching123.tcr.fragment.user;

import android.support.v4.app.FragmentActivity;

import com.googlecode.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;

/**
 * Created by vkompaniets on 11.11.2014.
 */
@EFragment
public class LoginFailureDialog extends AlertDialogFragment {

    protected static final String DIALOG_NAME = LoginFailureDialog.class.getSimpleName();

    @Override
    protected int getDialogTitle() {
        return R.string.error_message_login_title;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_send_logs;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    public static void show (FragmentActivity activity, String message, OnDialogClickListener onSendListener){
        DialogUtil.show(activity, DIALOG_NAME, LoginFailureDialog_.builder().dialogType(DialogType.ALERT).errorMsg(message).build().setOnNegativeListener(onSendListener));
    }
}
