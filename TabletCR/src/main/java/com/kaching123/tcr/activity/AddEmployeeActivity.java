package com.kaching123.tcr.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.user.AddEmployeeCommand;
import com.kaching123.tcr.commands.store.user.BaseEmployeeCommand.BaseEmployeeCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.user.LoginFragment;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.Permission;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by vkompaniets on 27.12.13.
 */
@EActivity(R.layout.employee_add_activity)
public class AddEmployeeActivity extends BaseEmployeeActivity {

    private AddEmployeeCallback addEmployeeCallback = new AddEmployeeCallback();

    @AfterViews
    @Override
    protected void init(){
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
        if(!super.validateForm()){
            return false;
        }
        String passwordText = password.getText().toString().trim();
        if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(this, R.string.employee_edit_password_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passwordText.length() < PASSWORD_MIN_LEN) {
            Toast.makeText(this, R.string.employee_edit_password_min_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!passwordConfirm.getText().toString().equals(passwordText)) {
            Toast.makeText(this, R.string.employee_edit_password_confirm_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void callCommand(final EmployeeModel model, ArrayList<Permission> permissions) {
        AddEmployeeCommand.start(AddEmployeeActivity.this, model, permissions, addEmployeeCallback);
    }

    public static void start(Context context, EmployeeModel model) {
        AddEmployeeActivity_.intent(context).model(model).start();
    }

    public class AddEmployeeCallback extends BaseEmployeeCallback {

        @Override
        protected void onSuccess() {
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            finish();
        }

        @Override
        protected void onEmployeeError() {
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            AlertDialogFragment.showAlert(AddEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_msg));
        }

        @Override
        protected void onEmployeeAlreadyExists() {
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            AlertDialogFragment.showAlert(AddEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_already_exists_msg, "\"" + model.login + "\""));
        }

        @Override
        protected void onEmailAlreadyExists() {
            WaitDialogFragment.hide(AddEmployeeActivity.this);
            AlertDialogFragment.showAlert(AddEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_email_already_exists_msg, "\"" + model.email + "\""));
        }
    }
}
