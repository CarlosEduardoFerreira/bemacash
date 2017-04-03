package com.kaching123.tcr.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
import com.kaching123.tcr.fragment.dialog.DialogUtil;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
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

    EmployeeModel initEmployeeModel;

    long statusValue;
    long presedValue;
    boolean statusChanged = false;
    boolean presedChanged = false;

    protected Collection<Permission> customPermissionInitial;

    @AfterViews
    @Override
    protected void init() {
        super.init();
        initUserAndPwd();
        getSupportLoaderManager().initLoader(0, null, new UserPermissionsLoader());

        statusChanged = false;
        presedChanged = false;

        status.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    statusValue = status.getSelectedItemId();
                }
                return false;
            }
        });

        preset.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    presedValue = preset.getSelectedItemId();
                }
                return false;
            }
        });

        fillFields();
    }

    private void initUserAndPwd() {
        if (model.isSynced) {
            login.setEnabled(false);

            login.setBackgroundColor(getResources().getColor(R.color.password_gray));

            password.setEnabled(false);
            password.setBackgroundColor(getResources().getColor(R.color.password_gray));

            passwordConfirm.setEnabled(false);
            passwordConfirm.setBackgroundColor(getResources().getColor(R.color.password_gray));
        }
    }

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

        initEmployeeModel = model;
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
        if(employeeHasChanges() || permissionsHasChanges()) {
            final FragmentActivity actv = this;
            AlertDialogFragment.showAlert(
                this,
                R.string.dlg_title_back_button,
                getApplicationContext().getResources().getString(R.string.dlg_text_back_button),
                R.string.btn_yes,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        onBackPressedDialog();
                        disableForceLogOut();
                        return false;
                    }
                }, new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        DialogUtil.hide(actv, "errorDialogFragment");
                        return false;
                    }
                }
            );
        }else{
            onBackPressedDialog();
        }
    }

    private void onBackPressedDialog(){
        super.onBackPressed();
        disableForceLogOut();
    }



    private boolean employeeHasChanges(){

        DecimalFormat formatar = new DecimalFormat("###,###,###,###,###.##");
        formatar.setMinimumFractionDigits(2);

        BigDecimal hR = hourlyRate.getText().toString().equals("") ? BigDecimal.ZERO : new BigDecimal(hourlyRate.getText().toString());
        String hRate1 = formatar.format(initEmployeeModel.hRate);
        String hRate2 = formatar.format(hR);

        statusChanged = statusValue != status.getSelectedItemId();

        BigDecimal co = commissions.getText().toString().equals("") ? BigDecimal.ZERO : new BigDecimal(commissions.getText().toString());
        String commission1 = formatar.format(initEmployeeModel.commission);
        String commission2 = formatar.format(co);

        presedChanged = presedValue != preset.getSelectedItemId();

        if(!initEmployeeModel.firstName.equals(firstName.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.firstName"); return true;
        }
        if(!initEmployeeModel.lastName.equals(lastName.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.lastName"); return true;
        }
        if(!initEmployeeModel.login.equals(login.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.login"); return true;
        }
        if(initEmployeeModel.email != null)
        if(!initEmployeeModel.email.equals(email.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.email"); return true;
        }
        if(initEmployeeModel.phone != null) {
            String phoneOnlyNumbers = phone.getText().toString().replaceAll("[^0-9]", "");
            if (!initEmployeeModel.phone.equals(phoneOnlyNumbers)) {
                Log.d("BemaCarl3", "EditEmployeeActivity.employeeHasChanges.phone: |" + initEmployeeModel.phone + "|" + phoneOnlyNumbers + "|");
                return true;
            }
        }
        if(initEmployeeModel.street != null)
        if(!initEmployeeModel.street.equals(street.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.street"); return true;
        }
        if(initEmployeeModel.complementary != null)
        if(!initEmployeeModel.complementary.equals(complementary.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.complementary"); return true;
        }
        if(initEmployeeModel.city != null)
        if(!initEmployeeModel.city.equals(city.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.city"); return true;
        }
        if(initEmployeeModel.state != null)
        if(!initEmployeeModel.state.equals(state.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.state"); return true;
        }
        if(initEmployeeModel.country != null)
        if(!initEmployeeModel.country.equals(country.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.country"); return true;
        }
        if(initEmployeeModel.zip != null)
        if(!initEmployeeModel.zip.equals(zip.getText().toString())) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.zip"); return true;
        }
        if(!hRate1.equals(hRate2)) {
            Log.d("BemaCarl3", "EditEmployeeActivity.employeeHasChanges.hourlyRate: |" + hRate1 + "|" + hRate2 + "|");
            return true;
        }
        if(initEmployeeModel.tipsEligible != tipsEligible.isChecked()) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.tipsEligible"); return true;
        }
        if(initEmployeeModel.commissionEligible != commissionsEligible.isChecked()) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.commissionsEligible"); return true;
        }
        if(!commission1.equals(commission2)) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.commissions"); return true;
        }
        if(statusChanged) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.statusChanged"); return true;
        }
        if(presedChanged) {
            Log.d("BemaCarl3","EditEmployeeActivity.employeeHasChanges.presedChanged"); return true;
        }

        return false;
    }


    private boolean permissionsHasChanges(){
        if(customPermissionInitial!=null && customPermissionsBase!=null) {
            for (Permission a : customPermissionInitial) {
                boolean hasOnBase = customPermissionsBase.contains(a);
                if (!hasOnBase){
                    Log.d("BemaCarl3","EditEmployeeActivity.permissionsHasChanges.hasOnBase: " + hasOnBase);
                    Log.d("BemaCarl3","EditEmployeeActivity.permissionsHasChanges.a.getId(): " + a.getId());
                    return true;
                }
            }
            for (Permission b : customPermissionsBase) {
                boolean hasOnInitial = customPermissionInitial.contains(b);
                if (!hasOnInitial){
                    Log.d("BemaCarl3","EditEmployeeActivity.permissionsHasChanges.hasOnInitial: " + hasOnInitial);
                    Log.d("BemaCarl3","EditEmployeeActivity.permissionsHasChanges.b.getId(): " + b.getId());
                    return true;
                }
            }
        }
        return false;
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
                    .transform(new Function<Cursor, List<Permission>>() {
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
                            customPermissionInitial = permissions;
                            return permissions;
                        }
                    })
                    .build(EditEmployeeActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<Permission>> mapLoader, List<Permission> permissions) {
            try2SetupPermissions(permissions);
            getSupportLoaderManager().destroyLoader(0);
            presedValue = preset.getSelectedItemId();
        }

        @Override
        public void onLoaderReset(Loader<List<Permission>> mapLoader) {

        }

    }
}
