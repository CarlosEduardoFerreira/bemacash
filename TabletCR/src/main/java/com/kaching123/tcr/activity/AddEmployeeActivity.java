package com.kaching123.tcr.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.local.EndEmployeeCommand;
import com.kaching123.tcr.commands.local.StartEmployeeCommand;
import com.kaching123.tcr.commands.store.user.AddEmployeeCommand;
import com.kaching123.tcr.commands.store.user.BaseEmployeeCommand.BaseEmployeeCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.user.LoginFragment;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by vkompaniets on 27.12.13.
 */
@EActivity(R.layout.employee_add_activity)
public class AddEmployeeActivity extends BaseEmployeeActivity {

    private AddEmployeeCallback addEmployeeCallback = new AddEmployeeCallback();
    protected static final Uri URI_EMPLOYEE_SYNCED = ShopProvider.getNoNotifyContentUri(ShopStore.EmployeeTable.URI_CONTENT);
    protected static final Uri URI_EMPLOYEE = ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT);

    @AfterViews
    @Override
    protected void init() {
        super.init();
        tipsEligible.setChecked(getApp().isTipsEnabled());
    }

    @Override
    protected void bindModel() {
        super.bindModel();
        model.password = LoginFragment.md5(password.getText().toString().trim());
        model.hireDate = new Date();
    }

    @Override
    protected boolean validateForm() {
        if (!super.validateForm()) {
            return false;
        }


        if(isPhoneNumberExists(phone.getText().toString())){
            Toast.makeText(this, R.string.employee_edit_phone_confirm_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        String passwordText = password.getText().toString().trim();
        if ((TextUtils.isEmpty(passwordText))) {
            Toast.makeText(this, R.string.employee_edit_password_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if ((passwordText.length() < PASSWORD_MIN_LEN)) {
            Toast.makeText(this, R.string.employee_edit_password_min_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if ((!passwordConfirm.getText().toString().equals(passwordText))) {
            Toast.makeText(this, R.string.employee_edit_password_confirm_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isPhoneNumberExists(String phone) {
        if(TextUtils.isEmpty(phone)) {
            return false;
        } else {
            Cursor c = ProviderAction
                    .query(URI_EMPLOYEE)
                    .where(ShopStore.EmployeeTable.PHONE + " = ?", phone)
                    .perform(getBaseContext());
            boolean exists = c.moveToFirst();
            c.close();
            return exists;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        progressReceiver.register(AddEmployeeActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressReceiver.unregister(AddEmployeeActivity.this);
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
                    Toast.makeText(AddEmployeeActivity.this, R.string.warning_employee_upload_fail, Toast.LENGTH_LONG).show();
            }
            if (UploadTask.ACTION_EMPLOYEE_UPLOAD_FAILED.equals(intent.getAction())) {

            }
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            finish();
        }
    };

    @Override
    protected void callCommand(final EmployeeModel model, ArrayList<Permission> permissions) {
        StartEmployeeCommand.start(this);
        AddEmployeeCommand.start(AddEmployeeActivity.this, model, permissions, addEmployeeCallback);
    }

    public static void start(Context context, EmployeeModel model) {
        AddEmployeeActivity_.intent(context).model(model).start();
    }

    public class AddEmployeeCallback extends BaseEmployeeCallback {

        @Override
        protected void onSuccess() {
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            EndEmployeeCommand.start(AddEmployeeActivity.this, true);
            WaitDialogFragment.show(AddEmployeeActivity.this, getString(R.string.wait_message_save_employee));
//            finish();
        }

        @Override
        protected void onEmployeeError() {
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            EndEmployeeCommand.start(AddEmployeeActivity.this);
            AlertDialogFragment.showAlert(AddEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_msg));
        }

        @Override
        protected void onEmployeeAlreadyExists() {
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            EndEmployeeCommand.start(AddEmployeeActivity.this);
            AlertDialogFragment.showAlert(AddEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_already_exists_msg, "\"" + model.login + "\""));
        }

        @Override
        protected void onEmailAlreadyExists() {
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            EndEmployeeCommand.start(AddEmployeeActivity.this);
            AlertDialogFragment.showAlert(AddEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_email_already_exists_msg, "\"" + model.email + "\""));
        }
    }

    private void updateEmployeeSyncStatus() {
        ContentResolver cr = AddEmployeeActivity.this.getContentResolver();
        ContentValues v = new ContentValues(1);
        v.put(ShopStore.EmployeeTable.IS_SYNC, "1");
        cr.update(URI_EMPLOYEE_SYNCED, v, ShopStore.EmployeeTable.IS_SYNC + " = ?", new String[]{"0"});
    }
}
