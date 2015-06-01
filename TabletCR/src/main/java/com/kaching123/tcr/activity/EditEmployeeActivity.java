package com.kaching123.tcr.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
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
import com.kaching123.tcr.fragment.user.LoginFragment;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showPhone;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 27.12.13.
 */
@EActivity(R.layout.employee_edit_activity)
@OptionsMenu(R.menu.employee_edit_actions)
public class EditEmployeeActivity extends BaseEmployeeActivity {
    public static final int REQUEST_CODE = 1;

    private static final Uri URI_PERMISSIONS = ShopProvider.getContentUri(EmployeePermissionTable.URI_CONTENT);
    protected static final Uri URI_EMPLOYEE_SYNCED = ShopProvider.getNoNotifyContentUri(ShopStore.EmployeeTable.URI_CONTENT);

    public EditEmployeeCallback editEmployeeCallback = new EditEmployeeCallback();
    private static final Uri EMPLOYEE_URI = ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT);

    @AfterViews
    @Override
    protected void init() {
        super.init();
        fillFields();
        initUserAndPwd();
        getSupportLoaderManager().initLoader(0, null, new UserPermissionsLoader());
    }

    private void initUserAndPwd() {
        if (model.isSynced) {
            login.setEnabled(false);

            login.setBackgroundColor(getResources().getColor(R.color.password_gray));

            password.setEnabled(false);
            password.setText(getString(R.string.employee_password_grayed));
            password.setBackgroundColor(getResources().getColor(R.color.password_gray));

            passwordConfirm.setText(getString(R.string.employee_password_grayed));
            passwordConfirm.setEnabled(false);
            passwordConfirm.setBackgroundColor(getResources().getColor(R.color.password_gray));
        }
    }

    /*@BeforeTextChange
    protected void testBeforeTextChanged(CharSequence s){
        BCFormatter formatter = new BCFormatter();
        s = formatter.format(s.toString());
    }*/

    private void fillFields() {
        firstName.setText(model.firstName);
        lastName.setText(model.lastName);
        login.setText(model.login);
        email.setText(model.email);
        showPhone(phone, model.phone);
        street.setText(model.street);
        complementary.setText(model.complementary);
        city.setText(model.city);
        state.setText(model.state);
        country.setText(model.country);
        zip.setText(model.zip);
        showPrice(hourlyRate, model.hRate);
        this.preset.setSelection(CUSTOM_PRESET_INDEX);
        status.setSelection(model.status.ordinal());
        tipsEligible.setChecked(model.tipsEligible);
        commissionsEligible.setChecked(model.commissionEligible);
        showPrice(commissions, model.commission);
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
            } else
                finish();


        }

        @Override
        protected void onFailure() {

        }
    }

//    private boolean updateLastSuccessfulLoginUser() {
//
//        Cursor c = ProviderAction.query(EMPLOYEE_URI)
//                .projection(ShopStore.EmployeeTable.LOGIN, ShopStore.EmployeeTable.PASSWORD)
//                .where(ShopStore.EmployeeTable.LOGIN + " !=?", model.login)
//                .orderBy(ShopStore.EmployeeTable.UPDATE_TIME + " desc ")
//                .perform(EditEmployeeActivity.this);
//        if (c.moveToFirst()) {
//            updateLastLogin(c.getString(0));
//            updateLastPassword(c.getString(1));
//        }
//        c.close();
//        return true;
//
//    }

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
    protected void bindModel() {
        super.bindModel();
        String passwordText = password.getText().toString().trim();
        if (!TextUtils.isEmpty(passwordText)) {
            model.password = LoginFragment.md5(passwordText);
        }
    }

    @Override
    protected boolean validateForm() {
        if (!super.validateForm()) {
            return false;
        }
        String passwordText = password.getText().toString().trim();
        String passwordConfirmText = passwordConfirm.getText().toString().trim();
        boolean isPswdEmpty = TextUtils.isEmpty(passwordText);
        if (model.isSynced || isPswdEmpty && TextUtils.isEmpty(passwordConfirmText))
            return true;

        if (model.isSynced || isPswdEmpty) {
            Toast.makeText(this, R.string.employee_edit_password_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (model.isSynced || passwordText.length() < PASSWORD_MIN_LEN) {
            Toast.makeText(this, R.string.employee_edit_password_min_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (model.isSynced || !passwordConfirmText.equals(passwordText)) {
            Toast.makeText(this, R.string.employee_edit_password_confirm_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disableForceLogOut();
    }

    private void disableForceLogOut() {
        setResult(REQUEST_CODE, new Intent().putExtra(DashboardActivity.EXTRA_FORCE_LOGOUT, false));
    }

    private class UserPermissionsLoader implements LoaderCallbacks<List<Permission>> {

        @Override
        public Loader<List<Permission>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_PERMISSIONS)
                    .projection(EmployeePermissionTable.PERMISSION_ID)
                    .where(EmployeePermissionTable.USER_GUID + " = ?", model.guid)
                    .where(EmployeePermissionTable.ENABLED + " = ?", 1)
                    .wrap(new Function<Cursor, List<Permission>>() {
                        @Override
                        public List<Permission> apply(Cursor c) {
                            List<Permission> permissions = new ArrayList<Permission>(c.getCount());
                            while (c.moveToNext()) {
                                Permission p = Permission.valueOfOrNull(c.getLong(0));
                                if (p != null) {
                                    permissions.add(p);
                                }
                            }
                            Collections.sort(permissions, new Comparator<Permission>() {
                                @Override
                                public int compare(Permission p1, Permission p2) {
                                    return p1.getGroup().compareTo(p2.getGroup());
                                }
                            });
                            return permissions;
                        }
                    })
                    .build(EditEmployeeActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<Permission>> mapLoader, List<Permission> permissions) {
            try2SetupPermissions(permissions);
            getSupportLoaderManager().destroyLoader(0);
        }

        @Override
        public void onLoaderReset(Loader<List<Permission>> mapLoader) {

        }

    }
}
