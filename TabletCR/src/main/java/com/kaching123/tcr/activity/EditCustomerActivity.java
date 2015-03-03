package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.user.AddCustomerCommand;
import com.kaching123.tcr.commands.store.user.AddCustomerCommand.BaseAddCustomerCallback;
import com.kaching123.tcr.commands.store.user.DeleteCustomerCommand;
import com.kaching123.tcr.commands.store.user.DeleteCustomerCommand.BaseDeleteCustomerCallback;
import com.kaching123.tcr.commands.store.user.EditCustomerCommand;
import com.kaching123.tcr.commands.store.user.EditCustomerCommand.BaseEditCustomerCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.Permission;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.kaching123.tcr.fragment.UiHelper.isValidEmail;
import static com.kaching123.tcr.fragment.UiHelper.showPhone;
import static com.kaching123.tcr.util.PhoneUtil.isValid;
import static com.kaching123.tcr.util.PhoneUtil.onlyDigits;

/**
 * Created by pkabakov on 10.02.14.
 */
@EActivity(R.layout.edit_customer_activity)
@OptionsMenu(R.menu.edit_customer_activity)
public class EditCustomerActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();
    static {
        permissions.add(Permission.CUSTOMER_MANAGEMENT);
    }

    public static final String EXTRA_CUSTOMER = "extra_customer";

    @Extra
    protected CustomerModel model;

    @ViewById
    protected EditText firstName;
    @ViewById
    protected EditText lastName;
    @ViewById
    protected EditText email;
    @ViewById
    protected EditText phone;
    @ViewById
    protected EditText street;
    @ViewById
    protected EditText complementary;
    @ViewById
    protected EditText city;
    @ViewById
    protected EditText state;
    @ViewById
    protected EditText country;
    @ViewById
    protected EditText zip;

    @ViewById
    protected Spinner sexSpinner;

    @ViewById
    protected CheckBox consentPromotions;

    private Mode mode;

    public static void start(Context context, CustomerModel model) {
        EditCustomerActivity_.intent(context).model(model).start();
    }

    public static void startForResult(Context context, CustomerModel model, int requestCode) {
        EditCustomerActivity_.intent(context).model(model).startForResult(requestCode);
    }

    public static void startForResult(Fragment fragment, Context context, CustomerModel model, int requestCode) {
        Intent intent = EditCustomerActivity_.intent(context).model(model).get();
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    @AfterViews
    protected void initViews() {
        mode = model == null ? Mode.CREATE : Mode.UPDATE;
        setTitle();
        sexSpinner.setAdapter(new SexAdapter());
        setCustomer();

        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

    }

    private void setTitle() {
        switch (mode) {
            case CREATE:
                setTitle(R.string.add_customer_activity_label);
                break;
            case UPDATE:
                setTitle(model.getFullName());
                break;
        }
    }

    private void setCustomer() {
        if (mode == Mode.CREATE)
            return;

        firstName.setText(model.firstName);
        lastName.setText(model.lastName);
        email.setText(model.email);
        showPhone(phone, model.phone);
        street.setText(model.street);
        complementary.setText(model.complementary);
        city.setText(model.city);
        state.setText(model.state);
        country.setText(model.country);
        zip.setText(model.zip);
        sexSpinner.setSelection(model.sex ? Sex.MALE.ordinal() : Sex.FEMALE.ordinal());
        consentPromotions.setChecked(model.consentPromotions);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem delete = menu.findItem(R.id.action_remove);
        delete.setVisible(mode == Mode.UPDATE);
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem
    protected void actionRemoveSelected(){
        deleteCustomer();
    }

    private void deleteCustomer() {
        AlertDialogFragment.show(
                this,
                DialogType.CONFIRM_NONE,
                R.string.customer_delete_dialog_title,
                getString(R.string.customer_delete_dialog_message),
                R.string.btn_confirm,
                new OnDialogClickListener() {
                    @Override
                    public boolean onClick() {
                        WaitDialogFragment.show(EditCustomerActivity.this, getString(R.string.customer_delete_wait_message));
                        DeleteCustomerCommand.start(EditCustomerActivity.this, model, deleteCustomerCallback);
                        return true;
                    }
                }

        );
    }

    @Click
    protected void saveButtonClicked() {
        if (!validateForm()) {
            return;
        }
        startCommand();
    }

    protected boolean validateForm() {

        if (TextUtils.isEmpty(firstName.getText().toString().trim())) {
            Toast.makeText(this, R.string.customer_edit_first_name_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        String emailText = email.getText().toString().trim();
        String phoneText = phone.getText().toString().trim();

        if (TextUtils.isEmpty(emailText) && TextUtils.isEmpty(phoneText)){
            Toast.makeText(this, R.string.customer_edit_email_phone_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TextUtils.isEmpty(emailText) && !isValidEmail(emailText)){
            Toast.makeText(this, R.string.customer_edit_email_not_valid_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TextUtils.isEmpty(phoneText) && !isValid(phoneText)){
            Toast.makeText(this, R.string.customer_edit_phone_not_valid_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    protected void startCommand() {
        updateModel();
        WaitDialogFragment.show(EditCustomerActivity.this, getString(R.string.customer_save_wait_message));
        if (mode == Mode.CREATE) {
            AddCustomerCommand.start(EditCustomerActivity.this, addCustomerCallback, model);
        } else {
            EditCustomerCommand.start(EditCustomerActivity.this, editCustomerCallback, model);
        }

    }

    private void updateModel() {
        if (mode == Mode.CREATE) {
            model = new CustomerModel(UUID.randomUUID().toString(), new Date());
        }

        model.firstName = firstName.getText().toString().trim();
        model.lastName= lastName.getText().toString().trim();
        model.email = email.getText().toString().trim();
        model.phone = onlyDigits(phone.getText().toString().trim());
        model.street = street.getText().toString().trim();
        model.complementary = complementary.getText().toString().trim();
        model.city = city.getText().toString().trim();
        model.state = state.getText().toString().trim();
        model.country = country.getText().toString().trim();
        model.zip = zip.getText().toString().trim();
        model.sex = sexSpinner.getSelectedItem() == Sex.MALE;
        model.consentPromotions = consentPromotions.isChecked();
    }

    private void setResult() {
        Intent data = new Intent();
        data.putExtra(EXTRA_CUSTOMER, model);
        setResult(RESULT_OK, data);
    }

    private BaseAddCustomerCallback addCustomerCallback = new BaseAddCustomerCallback() {

        @Override
        protected void onCustomerAdded() {
            WaitDialogFragment.hide(EditCustomerActivity.this);

            setResult();

            finish();
        }

        @Override
        protected void onCustomerAddError() {
            WaitDialogFragment.hide(EditCustomerActivity.this);
            AlertDialogFragment.showAlert(EditCustomerActivity.this, R.string.error_dialog_title, getString(R.string.customer_edit_error_msg));
        }

        @Override
        protected void onEmailExists() {
            WaitDialogFragment.hide(EditCustomerActivity.this);
            AlertDialogFragment.showAlert(EditCustomerActivity.this, R.string.error_dialog_title, getString(R.string.customer_edit_email_exists_error_msg));
        }

    };

    private BaseEditCustomerCallback editCustomerCallback = new BaseEditCustomerCallback() {

        @Override
        protected void onCustomerUpdated() {
            WaitDialogFragment.hide(EditCustomerActivity.this);
            finish();
        }

        @Override
        protected void onCustomerUpdateError() {
            WaitDialogFragment.hide(EditCustomerActivity.this);
            AlertDialogFragment.showAlert(EditCustomerActivity.this, R.string.error_dialog_title, getString(R.string.customer_edit_error_msg));
        }

        @Override
        protected void onEmailExists() {
            WaitDialogFragment.hide(EditCustomerActivity.this);
            AlertDialogFragment.showAlert(EditCustomerActivity.this, R.string.error_dialog_title, getString(R.string.customer_edit_email_exists_error_msg));
        }
    };

    private BaseDeleteCustomerCallback deleteCustomerCallback = new BaseDeleteCustomerCallback() {
        @Override
        protected void onCustomerDeleted() {
            WaitDialogFragment.hide(EditCustomerActivity.this);
            finish();
        }

        @Override
        protected void onCustomerDeleteError() {
            WaitDialogFragment.hide(EditCustomerActivity.this);
            AlertDialogFragment.showAlert(EditCustomerActivity.this, R.string.error_dialog_title, getString(R.string.customer_delete_dialog_error_message));
        }
    };

    private class SexAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return Sex.values().length;
        }

        @Override
        public Object getItem(int position) {
            return Sex.values()[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newDropDownView(parent, position);
            }
            bindView(convertView, parent, position);
            return convertView;
        }

        private View newDropDownView(ViewGroup parent, int position) {
            return LayoutInflater.from(EditCustomerActivity.this).inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent, position);
            }
            bindView(convertView, parent, position);
            return convertView;
        }

        private View newView(ViewGroup parent, int position) {
            return LayoutInflater.from(EditCustomerActivity.this).inflate(R.layout.spinner_item, parent, false);
        }

        private void bindView(View convertView, ViewGroup parent, int position) {
            TextView label = (TextView) convertView;
            Sex item = (Sex) getItem(position);
            label.setText(item.getLabelRes());
        }

    }

    private enum Sex {
        MALE(R.string.sex_male), FEMALE(R.string.sex_female);

        private final int labelRes;

        Sex(int labelRes) {
            this.labelRes = labelRes;
        }

        public int getLabelRes() {
            return labelRes;
        }
    }

    private enum Mode {
        CREATE, UPDATE;
    }

}
