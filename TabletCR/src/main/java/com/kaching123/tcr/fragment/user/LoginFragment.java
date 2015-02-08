package com.kaching123.tcr.fragment.user;

/**
 * Created by hamst_000 on 08/11/13.
 */

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.DashboardActivity;
import com.kaching123.tcr.activity.SignupActivity;
import com.kaching123.tcr.commands.store.user.LocalLoginCommand;
import com.kaching123.tcr.commands.store.user.LocalLoginCommand.BaseLocalLoginCommandCallback;
import com.kaching123.tcr.commands.store.user.LoginCommand;
import com.kaching123.tcr.commands.store.user.LoginCommand.BaseLoginCommandCallback;
import com.kaching123.tcr.commands.support.SendLogCommand;
import com.kaching123.tcr.commands.support.SendLogCommand.BaseSendLogCallback;
import com.kaching123.tcr.fragment.SuperBaseDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.EmployeePermissionsModel;
import com.kaching123.tcr.receiver.NetworkStateListener;
import com.kaching123.tcr.util.ScreenUtils;

@EFragment(R.layout.login_fragment)
public class LoginFragment extends SuperBaseDialogFragment {

    private static final String DIALOG_NAME = LoginFragment.class.getSimpleName();

    @ViewById
    protected TextView loggedInLabel;

    @ViewById
    protected EditText login;

    @ViewById
    protected EditText password;

    @ViewById
    protected View userContainer;

    @ViewById
    protected TextView userLabel;

    @FragmentArg
    protected Mode mode;

    private OnDismissListener onDismissListener;

    private OnLoginCompleteListener onLoginCompleteListener;
    private LoginCommandCallback loginCommandCallback = new LoginCommandCallback();
    private LocalLoginCommandCallback localLoginCommandCallback = new LocalLoginCommandCallback();

    public enum Mode {
        LOGIN, UNLOCK, SWITCH
    }

    @AfterViews
    protected void onIniView() {
        login.setTypeface(Typeface.DEFAULT);
        password.setTypeface(Typeface.DEFAULT);

        password.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    btnLoginClicked();
                    return true;
                }
                return false;
            }
        });
        login.setTransformationMethod(SingleLineTransformationMethod.getInstance());
        updateUI();
    }

    private void updateUI() {
        switch (mode) {
            case LOGIN:
                login.setVisibility(View.VISIBLE);
                loggedInLabel.setVisibility(View.GONE);
                userContainer.setVisibility(View.GONE);
                break;
            case SWITCH:
                login.setVisibility(View.VISIBLE);
                loggedInLabel.setVisibility(View.GONE);
                userContainer.setVisibility(View.GONE);

                password.setText("");
                login.requestFocus();
                break;
            case UNLOCK:
                login.setVisibility(View.GONE);
                loggedInLabel.setVisibility(View.VISIBLE);
                userContainer.setVisibility(View.VISIBLE);

                String currentUserName = getApp().getOperator().fullName();
                userLabel.setText(currentUserName);
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.login_form_width),
                getDialog().getWindow().getAttributes().height);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.AppTheme_Transparent);
        //changed for mintpos
        Window window = dialog.getWindow();

        // set "origin" to top left corner, so to speak
        window.setGravity(Gravity.TOP|Gravity.LEFT);

        // after that, setting values for x and y works "naturally"
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 630;
        params.y = 0;
        window.setAttributes(params);
        return dialog;
    }

    @Click
    protected void btnSignupClicked() {
        String url = getString(R.string.merchant_register_url);
        SignupActivity.start(getActivity(), url);
    }

    @Click
    protected void btnLoginClicked() {
        switch (mode) {
            case LOGIN:
            case SWITCH: {
                final String sLogin = login.getText().toString().trim();
                final String sPassword = password.getText().toString();
                if (TextUtils.isEmpty(sLogin) || TextUtils.isEmpty(sPassword)) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.error_message_login_title, getString(R.string.error_message_login_empty));
                    return;
                }

                if (mode == Mode.LOGIN && !getApp().isTrainingMode() && getApp().isOfflineModeExpired()) {
                    showOfflineModeError();
                } else if (mode == Mode.LOGIN && !getApp().isTrainingMode() && getApp().isOfflineModeNearExpiration()) {
                    AlertDialogFragment.show(getActivity(), AlertDialogFragment.DialogType.ALERT, R.string.offline_mode_warning_dialog_title, getString(R.string.offline_mode_warning_dialog_message), R.string.btn_ok, new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            SyncWaitDialogFragment.show(getActivity(), getString(R.string.login_wait));
                            LoginCommand.start(getActivity(), loginCommandCallback, sLogin, md5(sPassword), mode == Mode.LOGIN ? LoginCommand.Mode.LOGIN : LoginCommand.Mode.SWITCH);
                            return true;
                        }
                    }, null, null);
                    return;
                }

                SyncWaitDialogFragment.show(getActivity(), getString(R.string.login_wait));
                LoginCommand.start(getActivity(), loginCommandCallback, sLogin, md5(sPassword), mode == Mode.LOGIN ? LoginCommand.Mode.LOGIN : LoginCommand.Mode.SWITCH);
                break;
            }
            case UNLOCK: {
                String sPassword = password.getText().toString();
                if (TextUtils.isEmpty(sPassword)) {
                    AlertDialogFragment.showAlert(getActivity(), R.string.error_message_login_title, getString(R.string.error_message_login_empty));
                    return;
                }
                SyncWaitDialogFragment.show(getActivity(), getString(R.string.login_wait));
                LocalLoginCommand.start(getActivity(), localLoginCommandCallback, md5(sPassword));
                break;
            }
        }
    }

    @Click
    protected void switchUserClicked() {
        mode = Mode.SWITCH;
        updateUI();
    }

    public static String md5(String value) {
        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher().putString(value, Charsets.UTF_8).hash();
        return hc.toString();
    }

    public static void show(Fragment parent, OnLoginCompleteListener listener, OnDismissListener onDismissListener, Mode mode) {
        LoginFragment fragment = LoginFragment_.builder().mode(mode).build();
        fragment.setOnLoginCompleteListener(listener);
        fragment.setOnDismissListener(onDismissListener);
        DialogUtil.show(parent.getChildFragmentManager(), DIALOG_NAME, fragment);
    }

    public void setOnLoginCompleteListener(OnLoginCompleteListener onLoginCompleteListener) {
        this.onLoginCompleteListener = onLoginCompleteListener;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    private void setScreenOffTimeout() {
        storeSystemScreenOffTimeout();
        ScreenUtils.setScreenOffTimeout(getActivity());
    }

    private void storeSystemScreenOffTimeout() {
        getApp().getShopPref().prevScreenTimeout().put(ScreenUtils.geScreenOffTimeout(getActivity()));
    }

    private void showError(int errorId) {
        SyncWaitDialogFragment.hide(getActivity());
        LoginFailureDialog.show(getActivity(), getString(errorId), new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                sendDevLog();
                return true;
            }
        });
    }


    private void showOfflineModeError() {
        Toast.makeText(getActivity(), R.string.offline_mode_error_toast_message, Toast.LENGTH_SHORT).show();
    }

    public class LoginCommandCallback extends BaseLoginCommandCallback {

        @Override
        protected void onLoginSuccess(EmployeePermissionsModel employee) {
            if (getActivity() == null)
                return;

            SyncWaitDialogFragment.hide(getActivity());

            onLogin(employee, null);
        }

        @Override
        protected void onInvalidUploadTransaction(final EmployeePermissionsModel employeeModel, final String lastUncompletedSaleOrderGuid) {
            Logger.d("LoginFragment.onInvalidUploadTransaction(): lastUncompletedSaleOrderGuid: " + lastUncompletedSaleOrderGuid);
            if (getActivity() == null)
                return;

            SyncWaitDialogFragment.hide(getActivity());

            if (!TextUtils.isEmpty(lastUncompletedSaleOrderGuid)) {
                AlertDialogFragment.showAlertWithSkip(getActivity(), R.string.error_dialog_title, getString(R.string.warning_message_incomplete_order_try_to_complete), R.string.btn_ok,
                        new OnDialogClickListener() {
                            @Override
                            public boolean onClick() {
                                onLogin(employeeModel, lastUncompletedSaleOrderGuid);
                                return true;
                            }
                        },
                        new OnDialogClickListener() {
                            @Override
                            public boolean onClick() {
                                onLogin(employeeModel, null);
                                return true;
                            }
                        }
                );
                return;
            }

            AlertDialogFragment.show(getActivity(), DialogType.ALERT, R.string.error_dialog_title, getString(R.string.warning_message_incomplete_order), R.string.btn_ok, new OnDialogClickListener() {
                @Override
                public boolean onClick() {
                    onLogin(employeeModel, null);
                    return true;
                }
            });
        }

        private void onLogin(EmployeePermissionsModel employee, String lastUncompletedSaleOrderGuid) {
            if (mode == Mode.LOGIN) {
                setScreenOffTimeout();
            }

            if (employee != null) {
                getApp().setOperatorWithPermissions(employee, true);

                if (mode == Mode.LOGIN) {
                    //started listening to network state, check current value
                    NetworkStateListener.checkConnectivity(getActivity());
                }
            }

            boolean goToSaleOrder = false;
            if (onLoginCompleteListener != null) {
                if (TextUtils.isEmpty(lastUncompletedSaleOrderGuid))
                    onLoginCompleteListener.onLoginComplete();
                else
                    goToSaleOrder = onLoginCompleteListener.onLoginComplete(lastUncompletedSaleOrderGuid);
            }

            boolean goToDashboard = mode == Mode.SWITCH && !(getActivity() instanceof DashboardActivity);

            if (goToDashboard || goToSaleOrder) {
                instantDismiss();
            } else {
                delayedDismiss();
            }

            if (mode == Mode.SWITCH) {
                DashboardActivity.startClearTop(getActivity());
            }
        }

        @Override
        protected void onSyncError() {
            showError(R.string.error_message_login_db);
        }

        @Override
        protected void onLoginError() {
            showError(R.string.error_message_login);
        }

        @Override
        protected void onRegisterCheckError() {
            showError(R.string.error_message_register_check);
        }

        @Override
        protected void onRegisterPending() {
            showError(R.string.error_message_register_pending);
        }

        @Override
        protected void onEmployeeNotActive() {
            showError(R.string.error_message_employee_not_active);
        }

        @Override
        protected void onOutDated() {
            showError(R.string.error_message_outdated);
        }

        @Override
        protected void onOffline() {
            showError(R.string.error_message_offline);
        }

        @Override
        protected void onSyncInconsistent() {
            showError(R.string.error_message_sync_inconsistent);
        }

        @Override
        protected void onLoginOfflineFailed() {
            showError(R.string.error_message_login_offline_failed);
        }
    }

    private void sendDevLog(){
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_email));
        SendLogCommand.start(
                getActivity(),
                this.login.getText().toString(),
                this.login.getText().toString(),
                new SendLogCallback()
        );
    }

    public class SendLogCallback extends BaseSendLogCallback {

        @Override
        protected void handleOnSuccess() {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            Toast.makeText(getActivity(), getString(R.string.success_message_email), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void handleOnFailure() {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(
                    getActivity(),
                    R.string.error_dialog_title,
                    getString(R.string.error_message_email),
                    R.string.btn_try_again,
                    new OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            sendDevLog();
                            return true;
                        }
                    }
            );
        }
    }

    private class LocalLoginCommandCallback extends BaseLocalLoginCommandCallback {

        @Override
        protected void onLoginSuccess() {
            if (getActivity() == null)
                return;

            SyncWaitDialogFragment.hide(getActivity());

            if (onLoginCompleteListener != null) {
                onLoginCompleteListener.onLoginComplete();
            }

            delayedDismiss();
        }

        @Override
        protected void onLoginError() {
            showError(R.string.error_message_login);
        }
    }

    private void instantDismiss() {
        dismiss();
        if (onDismissListener != null)
            onDismissListener.onDismiss();
    }

    private void delayedDismiss() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                instantDismiss();
            }
        }, 450);
    }

    public static interface OnLoginCompleteListener {
        void onLoginComplete();

        boolean onLoginComplete(String lastUncompletedSaleOrderGuid);
    }

    public static interface OnDismissListener {
        void onDismiss();
    }
}
