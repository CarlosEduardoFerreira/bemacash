package com.kaching123.tcr.fragment.user;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.SingleLineTransformationMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.DashboardActivity;
import com.kaching123.tcr.commands.store.user.TempLoginCommand;
import com.kaching123.tcr.commands.store.user.TempLoginCommand.BaseTempLoginCommandCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.EmployeePermissionsModel;
import com.kaching123.tcr.model.Permission;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by pkabakov on 18.12.13.
 */
@EFragment
public class PermissionFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = PermissionFragment.class.getSimpleName();

    public enum Type {
        CANCELABLE, REDIRECTING
    }

    @FragmentArg
    protected Type type;

    @FragmentArg
    protected ArrayList<Permission> permissions;

    @ViewById
    protected EditText login;

    @ViewById
    protected EditText password;

    private LoginFragment.OnLoginCompleteListener onLoginCompleteListener;

    private OnDialogClickListener onCancelClickListener;

    private TempLoginCommandCallback tempLoginCommandCallback = new TempLoginCommandCallback();

    @Override
    protected int getDialogContentLayout() {
        return R.layout.permission_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.error_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return type.equals(Type.CANCELABLE) ? R.string.btn_cancel : R.string.btn_back_to_dashboard;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_login;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                login();
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (type.equals(Type.REDIRECTING)) {
                    DashboardActivity.startClearTop(getActivity());
                    return true;
                }
                if (onCancelClickListener != null)
                    return onCancelClickListener.onClick();
                return true;
            }
        };
    }

    public void setOnLoginCompleteListener(LoginFragment.OnLoginCompleteListener onLoginCompleteListener) {
        this.onLoginCompleteListener = onLoginCompleteListener;
    }

    public void setOnCancelClickListener(OnDialogClickListener onCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getDialog().getWindow().getAttributes().height);
        setCancelable(false);
    }

    @AfterViews
    protected void onIniView() {
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    login();
                    return true;
                }
                return false;
            }
        });
        login.setTransformationMethod(SingleLineTransformationMethod.getInstance());
    }

    public static void showRedirecting(FragmentActivity activity, LoginFragment.OnLoginCompleteListener onLoginCompleteListener, Permission... permissions) {
        show(activity, Type.REDIRECTING, null, onLoginCompleteListener, permissions);
    }

    public static void showCancelable(FragmentActivity activity, LoginFragment.OnLoginCompleteListener onLoginCompleteListener, Permission... permissions) {
        show(activity, Type.CANCELABLE, null, onLoginCompleteListener, permissions);
    }

    public static void showCancelable(FragmentActivity activity, OnDialogClickListener onCancelClickListener, LoginFragment.OnLoginCompleteListener onLoginCompleteListener, Permission... permissions) {
        show(activity, Type.CANCELABLE, onCancelClickListener, onLoginCompleteListener, permissions);
    }

    private static void show(FragmentActivity activity, Type type, OnDialogClickListener onCancelClickListener, LoginFragment.OnLoginCompleteListener onLoginCompleteListener, Permission... permissions) {
        ArrayList permissionsList = new ArrayList<Permission>();
        if (permissions != null) {
            Collections.addAll(permissionsList, permissions);
        }
        PermissionFragment fragment = PermissionFragment_.builder()
                .type(type)
                .permissions(permissionsList)
                .build();
        fragment.setOnCancelClickListener(onCancelClickListener);
        fragment.setOnLoginCompleteListener(onLoginCompleteListener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    protected void login() {
        String loginString = login.getText().toString().trim();
        String passwordString = password.getText().toString();

        WaitDialogFragment.show(getActivity(), getString(R.string.login_wait));

        TempLoginCommand.start(getActivity(), tempLoginCommandCallback, loginString, Hashing.md5().newHasher().putString(passwordString, Charsets.UTF_8).hash().toString(), permissions);
    }

    public class TempLoginCommandCallback extends BaseTempLoginCommandCallback {

        @Override
        protected void onLoginSuccess(EmployeePermissionsModel employee) {
            WaitDialogFragment.hide(getActivity());

            getApp().setOperatorWithPermissions(employee, false);
            if (onLoginCompleteListener != null) {
                onLoginCompleteListener.onLoginComplete();
            }
            dismiss();
        }

        @Override
        protected void onLoginError() {
            showError(R.string.error_message_login);
        }

        @Override
        protected void onDuplicateLoginError() {
            showError(R.string.error_message_login_duplicate);
        }

        @Override
        protected void onNoPermissionLoginError() {
            showError(R.string.error_message_login_no_permission);
        }

        @Override
        protected void onEmployeeNotActive() {
            showError(R.string.error_message_employee_not_active);
        }

        private void showError(int errorId) {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.error_message_login_title, getString(errorId));
        }
    }
}
