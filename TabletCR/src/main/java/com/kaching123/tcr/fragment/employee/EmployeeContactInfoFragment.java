package com.kaching123.tcr.fragment.employee;

import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseEmployeeActivity;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.fragment.UiHelper.isValidEmail;
import static com.kaching123.tcr.fragment.UiHelper.showPhone;
import static com.kaching123.tcr.util.PhoneUtil.onlyDigits;

/**
 * Created by mboychenko on 5/11/2017.
 */
@EFragment(R.layout.employee_contact_info_fragment)
public class EmployeeContactInfoFragment extends EmployeeBaseFragment implements EmployeeView {

    protected static final Uri URI_EMPLOYEE = ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT);

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

    @Override
    protected void setViews() {
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        street.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    @Override
    protected void setEmployee() {
        final EmployeeModel model = getEmployee();
        email.setText(model.email);
        showPhone(phone, model.phone);
        street.setText(model.street);
        complementary.setText(model.complementary);
        city.setText(model.city);
        state.setText(model.state);
        country.setText(model.country);
        zip.setText(model.zip);
    }

    @Override
    public void collectDataToModel(EmployeeModel model) {
        model.email = email.getText().toString();
        model.phone = onlyDigits(phone.getText().toString());
        model.street = street.getText().toString();
        model.complementary = complementary.getText().toString();
        model.city = city.getText().toString();
        model.state = state.getText().toString();
        model.country = country.getText().toString();
        model.zip = zip.getText().toString();
    }

    @Override
    public void setFieldsEnabled(boolean enabled) {

    }

    @Override
    public boolean validateView() {
        if (getMode() == BaseEmployeeActivity.EmployeeMode.CREATE) {
            if (isPhoneNumberExists(phone.getText().toString())) {
                Toast.makeText(getContext(), R.string.employee_edit_phone_confirm_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        String emailText = email.getText().toString().trim();
        if (!isValidEmail(emailText)) {
            Toast.makeText(getContext(), R.string.employee_edit_email_not_valid_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isPhoneNumberExists(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        } else {
            Cursor c = ProviderAction
                    .query(URI_EMPLOYEE)
                    .where(ShopStore.EmployeeTable.PHONE + " = ?", phone)
                    .perform(getContext());
            boolean exists = c.moveToFirst();
            c.close();
            return exists;
        }
    }

    @Override
    public boolean hasChanges(EmployeeModel initModel) {
        if (initModel.email != null)
            if (!initModel.email.equals(email.getText().toString())) {
                Log.d("BemaCarl3", "EmployeeContactInfoFragment.employeeHasChanges.email");
                return true;
            }
        if (initModel.phone != null) {
            String phoneOnlyNumbers = phone.getText().toString().replaceAll("[^0-9]", "");
            if (!initModel.phone.equals(phoneOnlyNumbers)) {
                Log.d("BemaCarl3", "EmployeeContactInfoFragment.employeeHasChanges.phone: |" + initModel.phone + "|" + phoneOnlyNumbers + "|");
                return true;
            }
        }
        if (initModel.street != null)
            if (!initModel.street.equals(street.getText().toString())) {
                Log.d("BemaCarl3", "EmployeeContactInfoFragment.employeeHasChanges.street");
                return true;
            }
        if (initModel.complementary != null)
            if (!initModel.complementary.equals(complementary.getText().toString())) {
                Log.d("BemaCarl3", "EmployeeContactInfoFragment.employeeHasChanges.complementary");
                return true;
            }
        if (initModel.city != null)
            if (!initModel.city.equals(city.getText().toString())) {
                Log.d("BemaCarl3", "EmployeeContactInfoFragment.employeeHasChanges.city");
                return true;
            }
        if (initModel.state != null)
            if (!initModel.state.equals(state.getText().toString())) {
                Log.d("BemaCarl3", "EmployeeContactInfoFragment.employeeHasChanges.state");
                return true;
            }
        if (initModel.country != null)
            if (!initModel.country.equals(country.getText().toString())) {
                Log.d("BemaCarl3", "EmployeeContactInfoFragment.employeeHasChanges.country");
                return true;
            }
        if (initModel.zip != null)
            if (!initModel.zip.equals(zip.getText().toString())) {
                Log.d("BemaCarl3", "EmployeeContactInfoFragment.employeeHasChanges.zip");
                return true;
            }
        return false;
    }
}
