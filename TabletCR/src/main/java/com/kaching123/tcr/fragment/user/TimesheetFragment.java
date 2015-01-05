package com.kaching123.tcr.fragment.user;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * Created by pkabakov on 06.01.14.
 */
@EFragment
public class TimesheetFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = TimesheetFragment.class.getSimpleName();

    public enum Type {
        CLOCK_IN, CLOCK_OUT
    }

    @FragmentArg
    protected Type type;

    @FragmentArg
    protected String clarification;

    @ViewById
    protected EditText login;

    @ViewById
    protected TextView clarificationView;

    /*@ViewById
    protected EditText password;*/

    private OnTimesheetListener onTimesheetListener;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.timesheet_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.timesheet_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return type.equals(Type.CLOCK_IN) ? R.string.btn_clock_in : R.string.btn_clock_out;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return true;
            }
        };
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return verifyAndComplete();
            }
        };
    }

    public void setOnTimesheetListener(OnTimesheetListener onTimesheetListener) {
        this.onTimesheetListener = onTimesheetListener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.clockin_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void onIniView() {
        if (!TextUtils.isEmpty(clarification)){
            clarificationView.setVisibility(View.VISIBLE);
            clarificationView.setText(clarification);
        }else{
            clarificationView.setVisibility(View.GONE);
        }

        login.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    return verifyAndComplete();
                }
                return false;
            }
        });
        login.setTransformationMethod(SingleLineTransformationMethod.getInstance());
    }

    public static void show(FragmentActivity activity, Type type, String clarification, OnTimesheetListener onTimesheetListener) {
        TimesheetFragment fragment = TimesheetFragment_.builder().type(type).clarification(clarification).build();
        fragment.setOnTimesheetListener(onTimesheetListener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    protected boolean verifyAndComplete() {
        String login = this.login.getText().toString();
        //String password = this.password.getText().toString();
        if (TextUtils.isEmpty(login) /*|| TextUtils.isEmpty(password)*/)
            return false;
        if (onTimesheetListener != null)
            onTimesheetListener.onCredentialsEntered(login/*, Hashing.md5().newHasher().putString(password, Charsets.UTF_8).hash().toString()*/);
        return true;
    }

    public interface OnTimesheetListener {

        public void onCredentialsEntered(String login/*, String password*/);

    }
}
