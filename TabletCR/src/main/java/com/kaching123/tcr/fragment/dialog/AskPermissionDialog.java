package com.kaching123.tcr.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Random;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;

/**
 * Created by Carlos on 04/04/17.
 */

@EFragment
public class AskPermissionDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = AskPermissionDialog.class.getSimpleName();

    private static boolean on;

    @ViewById
    protected EditText mEtPwd;

    @ViewById
    protected TextView mTvCountersign;

    private String mPwd;

    private OnCallback mOnCallback;

    public synchronized static void show(FragmentActivity activity, OnCallback callback) {
        if (on) {
            hide(activity);
            show(activity, callback);
            return;
        }

        on = true;

        DialogUtil.show(activity, DIALOG_NAME, AskPermissionDialog_.builder().build()).setOnCallback(callback);
    }

    public void setOnCallback(OnCallback mOnCallback) {
        this.mOnCallback = mOnCallback;
    }

    public synchronized static void hide(FragmentActivity activity) {

        Logger.d("public static void hide");

        DialogUtil.hide(activity, DIALOG_NAME);
        on = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.default_dlg_width), getDialog().getWindow().getAttributes().height);
        setCancelable(true);
    }

    @AfterViews
    void init() {
        mPwd = String.valueOf(new Random().nextInt(999999999 - 111111111 + 1));
        mTvCountersign.setText(String.valueOf(Integer.toHexString(Integer.parseInt(mPwd))));
        mPwd = String.valueOf(Long.parseLong(mPwd) * 12 / 7);

        if (mPwd.length() > 6) mPwd = mPwd.substring(0, 6);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.ask_permission_dialog;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.ask_permission;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.button_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_to_check;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (mPwd.equals(mEtPwd.getText().toString())) {
                    mEtPwd.setError(null);
                    mOnCallback.result(true);
                    hide(getActivity());

                } else {
                    mEtPwd.setError(getApp().getString(R.string.pwd_incorrect));
                }

                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                hide(getActivity());
                mOnCallback.result(false);
                return false;
            }
        };
    }

    public interface OnCallback {
        void result(boolean success);
    }
}
