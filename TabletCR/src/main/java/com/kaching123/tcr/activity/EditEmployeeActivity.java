package com.kaching123.tcr.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.local.EndEmployeeCommand;
import com.kaching123.tcr.commands.local.StartEmployeeCommand;
import com.kaching123.tcr.commands.store.user.BaseEmployeeCommand.BaseEmployeeCallback;
import com.kaching123.tcr.commands.store.user.DeleteEmployeeCommand;
import com.kaching123.tcr.commands.store.user.EditEmployeeCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.ArrayList;


/**
 * Created by vkompaniets on 27.12.13.
 */
@EActivity(R.layout.employee_activity_layout)
@OptionsMenu(R.menu.employee_edit_actions)
public class EditEmployeeActivity extends BaseEmployeeActivity {

    protected static final Uri URI_EMPLOYEE_SYNCED = ShopProvider.getNoNotifyContentUri(ShopStore.EmployeeTable.URI_CONTENT);
    public EditEmployeeCallback editEmployeeCallback = new EditEmployeeCallback();

    @AfterViews
    @Override
    protected void init() {
        mode = EmployeeMode.EDIT;
        super.init();
        initUserAndPwd();

        statusChanged = false;
        presedChanged = false;

        personalInfoFragment.getStatus().setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    statusValue = personalInfoFragment.getStatus().getSelectedItemId();
                }
                return false;
            }
        });
    }

    private void initUserAndPwd() {
        if (model.isSynced) {
            personalInfoFragment.setFieldsEnabled(false);
        }
    }

    @OptionsItem
    protected void actionRemoveSelected() {
        if (model.isSynced) {
            Toast.makeText(EditEmployeeActivity.this, getString(R.string.warning_delete_employee), Toast.LENGTH_LONG).show();
            return;
        }

        StartEmployeeCommand.start(this);
        deleteEmployee();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressReceiver.register(EditEmployeeActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressReceiver.unregister(EditEmployeeActivity.this);
    }

    public void logOut(Context context) {
        setResult(REQUEST_CODE, new Intent().putExtra(DashboardActivity.EXTRA_FORCE_LOGOUT, true));
        finish();
    }

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED);
        intentFilter.addAction(UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED);
    }

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(intentFilter) {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_COMPLETED.equals(intent.getAction())) {
                if (intent.getBooleanExtra(UploadTaskV2.EXTRA_SUCCESS, false))
                    updateEmployeeSyncStatus();
                if (intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE) != null && intent.getStringExtra(UploadTaskV2.EXTRA_ERROR_CODE).equalsIgnoreCase("400"))
                    Toast.makeText(EditEmployeeActivity.this, R.string.warning_employee_upload_fail, Toast.LENGTH_LONG).show();
            }
            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED.equals(intent.getAction())) {

            }
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            finish();
        }
    };

    private void updateEmployeeSyncStatus() {
        ContentResolver cr = EditEmployeeActivity.this.getContentResolver();
        ContentValues v = new ContentValues(1);
        v.put(ShopStore.EmployeeTable.IS_SYNC, "1");
        cr.update(URI_EMPLOYEE_SYNCED, v, ShopStore.EmployeeTable.IS_SYNC + " = ?", new String[]{"0"});
    }

    private void deleteEmployee() {
        AlertDialogFragment.show(
                this,
                DialogType.CONFIRM_NONE,
                R.string.employee_delete_dialog_title,
                getString(R.string.employee_delete_dialog_message),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DeleteEmployeeCommand.start(EditEmployeeActivity.this, model.guid, new DeleteEmployeeCommandListener());
                        return true;
                    }
                }
        );
    }

    private class DeleteEmployeeCommandListener extends DeleteEmployeeCommand.DeleteEmployeeCallback {

        @Override
        protected void onSuccess() {
            EndEmployeeCommand.start(EditEmployeeActivity.this, true);

            if (isCurrentUserDeleted(model.login)) {
                logOut(EditEmployeeActivity.this);
            } else {
                finish();
            }
        }

        @Override
        protected void onFailure() {

        }
    }

    private void updateLastLogin(String login) {
        if (login == null)
            return;
        getApp().setLastUserName(login);
    }

    private void updateLastPassword(String password) {
        if (password == null)
            return;
        getApp().setLastUserPassword(password);
    }

    private String getLastLogin() {
        return getApp().getLastUserName();
    }

    private String getLastPassword() {
        return getApp().getLastUserPassword();
    }

    private boolean isCurrentUserDeleted(String login) {
        return getApp().getOperatorLogin().equalsIgnoreCase(login);
    }

    @Override
    protected boolean validateForm() {
        if (!personalInfoFragment.validateView()) {
           return false;
        }
        return super.validateForm();
    }

    @Override
    protected void callCommand(final EmployeeModel model, ArrayList<Permission> permissions) {
        StartEmployeeCommand.start(this);
        EditEmployeeCommand.start(EditEmployeeActivity.this, model, permissions, editEmployeeCallback);

    }

    @OptionsItem
    protected void actionTimeSelected() {
        EmployeeTimeAttendanceActivity.start(this, model.guid);
    }

    public static void start(Context context, EmployeeModel model) {
        EditEmployeeActivity_.intent(context).model(model).startForResult(EditEmployeeActivity.REQUEST_CODE);
    }

    public class EditEmployeeCallback extends BaseEmployeeCallback {
        @Override
        protected void onSuccess() {
            disableForceLogOut();
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            EndEmployeeCommand.start(EditEmployeeActivity.this, true);
            WaitDialogFragment.show(EditEmployeeActivity.this, getString(R.string.wait_message_save_employee));
//            finish();
        }

        @Override
        protected void onEmployeeError() {
            disableForceLogOut();
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            EndEmployeeCommand.start(EditEmployeeActivity.this);
            AlertDialogFragment.showAlert(EditEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_msg));
        }

        @Override
        protected void onEmployeeAlreadyExists() {
            disableForceLogOut();
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            EndEmployeeCommand.start(EditEmployeeActivity.this);
            AlertDialogFragment.showAlert(EditEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_already_exists_msg, "\"" + model.login + "\""));
        }

        @Override
        protected void onEmailAlreadyExists() {
            disableForceLogOut();
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            EndEmployeeCommand.start(EditEmployeeActivity.this);
            AlertDialogFragment.showAlert(EditEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_email_already_exists_msg, "\"" + model.email + "\""));
        }
    }

}
