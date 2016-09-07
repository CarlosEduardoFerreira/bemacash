package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.SignedCurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.model.LabaledEnum;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PermissionPreset;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.isValidEmail;
import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.util.PhoneUtil.onlyDigits;

/**
 * Created by vkompaniets on 23.12.13.
 */
@EActivity
public abstract class BaseEmployeeActivity extends SuperBaseActivity {

    protected static final Uri URI_EMPLOYEE = ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT);
    protected static final int CUSTOM_PRESET_INDEX = PermissionPreset.values().length - 1;
    public static final int LOGIN_MIN_LEN = 3;
    public static final int PASSWORD_MIN_LEN = 4;

    public static final int PERMISSIONS_REQUEST_INDEX = 1;
    protected CurrencyTextWatcher currencyTextWatcher;

    @Extra
    protected EmployeeModel model;

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
    protected EditText login;
    @ViewById
    protected EditText password;
    @ViewById
    protected EditText passwordConfirm;
    @ViewById
    protected EditText hourlyRate;
    @ViewById
    protected Spinner preset;
    @ViewById
    protected ListView permissionList;
    @ViewById
    protected Spinner status;
    @ViewById
    protected CheckBox tipsEligible;
    @ViewById
    protected View commissionsEligibleContainer;
    @ViewById
    protected View commissionsContainer;
    @ViewById
    protected CheckBox commissionsEligible;
    @ViewById
    protected EditText commissions;

    private ArrayList<Permission> permissions = new ArrayList<Permission>();
    private ArrayList<PresetWrapper> presetDataList;

    protected abstract void callCommand(EmployeeModel model, ArrayList<Permission> permissions);

    private boolean isFirstSpinnerCall = true;

    private int spinnerLastPos;

    protected void init() {
        login.setTransformationMethod(SingleLineTransformationMethod.getInstance());
        presetDataList = new ArrayList<PresetWrapper>();
        for (PermissionPreset preset : PermissionPreset.values()) {
            presetDataList.add(new PresetWrapper(preset));
        }

        final ArrayAdapter<PresetWrapper> presetAdapter = new ArrayAdapter<PresetWrapper>(this,
                R.layout.spinner_item_light, presetDataList);
        presetAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        preset.setAdapter(presetAdapter);
        preset.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //if (!isFirstSpinnerCall) {
                //if (!PlanOptions.isCustomPermissionAllowed() &&
                //        presetDataList.get(i).getItem().getId() == PermissionPreset.CUSTOM.getId()) {
                //     AlertDialogFragment.showAlert(BaseEmployeeActivity.this,
                //           R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
                //   preset.setSelection(spinnerLastPos);
                //} else {
                displayPermissions(i);
                //spinnerLastPos = i;
                //}
                //}
                //isFirstSpinnerCall = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        passwordConfirm.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        passwordConfirm.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    email.requestFocus();
                    return true;
                }
                return false;
            }
        });
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        street.setImeOptions(EditorInfo.IME_ACTION_DONE);

        InputFilter[] signedDecimalFilter = new InputFilter[]{new SignedCurrencyFormatInputFilter()};
        InputFilter[] decimalFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
        currencyTextWatcher = new CurrencyTextWatcher(hourlyRate);
        hourlyRate.setFilters(signedDecimalFilter);
        hourlyRate.addTextChangedListener(currencyTextWatcher);
        commissions.setFilters(decimalFilter);

        status.setAdapter(new StatusAdapter());

        tipsEligible.setVisibility(getApp().isTipsEnabled() ? View.VISIBLE : View.GONE);
        commissionsEligibleContainer.setVisibility(getApp().isCommissionsEnabled() ? View.VISIBLE : View.GONE);
        commissionsContainer.setVisibility(getApp().isCommissionsEnabled() ? View.VISIBLE : View.GONE);
    }

    @Click
    protected void btnSaveClicked() {
        if (!validateForm()) {
            return;
        }
        if (login.isEnabled())
            model.isSynced = false;
        bindModel();
        doCommand();
    }

    protected void bindModel() {
        model.firstName = firstName.getText().toString();
        model.lastName = lastName.getText().toString();
        model.login = login.getText().toString().trim();
        model.email = email.getText().toString();
        model.phone = onlyDigits(phone.getText().toString());
        model.street = street.getText().toString();
        model.complementary = complementary.getText().toString();
        model.city = city.getText().toString();
        model.state = state.getText().toString();
        model.country = country.getText().toString();
        model.zip = zip.getText().toString();
        model.hRate = parseBigDecimal(hourlyRate, BigDecimal.ZERO);
        model.status = (EmployeeStatus) status.getSelectedItem();
        model.tipsEligible = tipsEligible.isChecked();
        model.commissionEligible = commissionsEligible.isChecked();
        model.commission = parseBigDecimal(commissions, BigDecimal.ZERO);
    }

    protected void doCommand() {
        WaitDialogFragment.show(BaseEmployeeActivity.this, getString(R.string.employee_edit_wait_msg));
        callCommand(model, permissions);
    }

    protected boolean validateForm() {
        if (TextUtils.isEmpty(firstName.getText())) {
            Toast.makeText(this, R.string.employee_edit_first_name_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(lastName.getText())) {
            Toast.makeText(this, R.string.employee_edit_last_name_error, Toast.LENGTH_SHORT).show();
            return false;
        }

    /*    if(isPhoneNumberExists(phone.getText().toString())){
            Toast.makeText(this, R.string.employee_edit_phone_confirm_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(lastName.getText())) {
            Toast.makeText(this, R.string.employee_edit_last_name_error, Toast.LENGTH_SHORT).show();
            return false;
        }*/

        String loginText = login.getText().toString().trim();
        if (TextUtils.isEmpty(loginText)) {
            Toast.makeText(this, R.string.employee_edit_login_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (loginText.length() < LOGIN_MIN_LEN) {
            Toast.makeText(this, R.string.employee_edit_login_min_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        String emailText = email.getText().toString().trim();
        if (!isValidEmail(emailText)) {
            Toast.makeText(this, R.string.employee_edit_email_not_valid_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TextUtils.isEmpty(commissions.getText())) {
            if (parseBigDecimal(commissions, BigDecimal.ZERO).compareTo(CalculationUtil.ONE_HUNDRED) == 1) {
                Toast.makeText(this, R.string.commission_validation_alert_msg, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }


    @Click
    protected void btnEditPermissionClicked() {
        //if (!PlanOptions.isCustomPermissionAllowed()) {
        //    AlertDialogFragment.showAlert(BaseEmployeeActivity.this,
        //            R.string.unavailable_option_title, getString(R.string.unavailable_option_message));
        //} else {
        PermissionActivity.start(this, PERMISSIONS_REQUEST_INDEX, permissions);
        //}
    }

    @OnActivityResult(PERMISSIONS_REQUEST_INDEX)
    protected void onResult(Intent data) {
        if (data.getExtras() == null)
            return;
        List<Permission> result = (List<Permission>) data.getExtras().getSerializable(PermissionActivity.EXTRA_PERMISSIONS);
        try2SetupPermissions(result);
    }

    protected void try2SetupPermissions(List<Permission> result) {
        if (!updatePreset(result)) {
            PresetWrapper customPreset = this.presetDataList.get(CUSTOM_PRESET_INDEX);
            customPreset.setCustomPermissions(result);
            if (this.preset.getSelectedItemPosition() == CUSTOM_PRESET_INDEX) {
                displayPermissions(customPreset.getPermissions());
            } else {
                this.preset.setSelection(CUSTOM_PRESET_INDEX);
            }
        }
    }

    private boolean updatePreset(Collection<Permission> list) {
        int len = presetDataList.size();
        for (int i = 0; i < len; i++) {
            EnumWrapper<PermissionPreset> preset = presetDataList.get(i);
            if (preset.getItem().isPreset(list)) {
                this.preset.setSelection(i);
                return true;
            }
        }
        return false;
    }

    private void displayPermissions(Collection<Permission> list) {
        permissions.clear();
        if (list != null) {
            permissions.addAll(list);
        }
        permissionList.setAdapter(new PermissionAdapter(this, permissions));
    }

    private void displayPermissions(int i) {
        displayPermissions(presetDataList.get(i).getPermissions());
    }

    private class PresetWrapper extends EnumWrapper<PermissionPreset> {

        private Collection<Permission> customPermissions;

        private PresetWrapper(PermissionPreset item) {
            super(item);
            customPermissions = item.getPermissions();
        }

        public Collection<Permission> getPermissions() {
            return customPermissions;
        }

        public void setCustomPermissions(Collection<Permission> customPermissions) {
            if (customPermissions == null) {
                this.customPermissions = Collections.emptyList();
                return;
            }
            this.customPermissions = customPermissions;
        }
    }

    private class EnumWrapper<E extends LabaledEnum> {
        final E item;

        private EnumWrapper(E item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return getString(item.getLabelId());
        }

        public E getItem() {
            return item;
        }
    }


    private class PermissionAdapter extends ObjectsCursorAdapter<Permission> {

        public PermissionAdapter(Context context, List<Permission> permissions) {
            super(context);
            changeCursor(permissions);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.permission_preset_list_item, parent, false);
            assert convertView != null;

            ViewHolder holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.header = (TextView) convertView.findViewById(R.id.header);
            holder.divider = convertView.findViewById(R.id.divider);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, Permission item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            Permission i = getItem(position);

            if (i == null) {
                return convertView;
            }

            holder.name.setText(getString(i.getLabelId()));

            if (position == 0 || item.getGroup() != getItem(position - 1).getGroup()) {
                holder.header.setVisibility(View.VISIBLE);
                holder.header.setText(item.getGroup().getLabelId());
            } else {
                holder.header.setVisibility(View.GONE);
            }

            if (position == getCount() - 1 || item.getGroup() != getItem(position + 1).getGroup()) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        @Override
        public synchronized void changeCursor(List<Permission> list) {
            Collections.sort(list);
            super.changeCursor(list);
        }

        private class ViewHolder {
            TextView header;
            TextView name;
            View divider;
        }
    }

    private class StatusAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return EmployeeStatus.values().length;
        }

        @Override
        public Object getItem(int i) {
            return EmployeeStatus.values()[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
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
            return LayoutInflater.from(BaseEmployeeActivity.this).inflate(R.layout.spinner_dropdown_item, parent, false);
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
            return LayoutInflater.from(BaseEmployeeActivity.this).inflate(R.layout.spinner_item, parent, false);
        }

        private void bindView(View convertView, ViewGroup parent, int position) {
            TextView label = (TextView) convertView;
            EmployeeStatus item = (EmployeeStatus) getItem(position);
            label.setText(item.getLabelRes());
        }
    }


}
