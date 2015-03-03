package com.kaching123.tcr.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;
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
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;

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

    private static final Uri URI_PERMISSIONS = ShopProvider.getContentUri(EmployeePermissionTable.URI_CONTENT);

    public EditEmployeeCallback editEmployeeCallback = new EditEmployeeCallback();

    @AfterViews
    @Override
    protected void init() {
        super.init();
        fillFields();
        getSupportLoaderManager().initLoader(0, null, new UserPermissionsLoader());
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
    protected void actionRemoveSelected(){
        deleteEmployee();
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
                        DeleteEmployeeCommand.start(EditEmployeeActivity.this, model.guid);
                        finish();
                        return true;
                    }
                }
        );
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
        if (isPswdEmpty && TextUtils.isEmpty(passwordConfirmText))
            return true;

        if (isPswdEmpty) {
            Toast.makeText(this, R.string.employee_edit_password_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passwordText.length() < PASSWORD_MIN_LEN) {
            Toast.makeText(this, R.string.employee_edit_password_min_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!passwordConfirmText.equals(passwordText)) {
            Toast.makeText(this, R.string.employee_edit_password_confirm_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void callCommand(final EmployeeModel model, ArrayList<Permission> permissions) {
        EditEmployeeCommand.start(EditEmployeeActivity.this, model, permissions, editEmployeeCallback);
    }

    @OptionsItem
    protected void actionTimeSelected() {
        EmployeeTimeAttendanceActivity.start(this, model.guid);
    }

    public static void start(Context context, EmployeeModel model) {
        EditEmployeeActivity_.intent(context).model(model).start();
    }

    public class EditEmployeeCallback extends BaseEmployeeCallback {
        @Override
        protected void onSuccess() {
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            finish();
        }

        @Override
        protected void onEmployeeError() {
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            AlertDialogFragment.showAlert(EditEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_msg));
        }

        @Override
        protected void onEmployeeAlreadyExists() {
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            AlertDialogFragment.showAlert(EditEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_already_exists_msg, "\"" + model.login + "\""));
        }

        @Override
        protected void onEmailAlreadyExists() {
            WaitDialogFragment.hide(EditEmployeeActivity.this);
            AlertDialogFragment.showAlert(EditEmployeeActivity.this, R.string.error_dialog_title, getString(R.string.employee_edit_error_email_already_exists_msg, "\"" + model.email + "\""));
        }
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
