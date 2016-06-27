package com.kaching123.tcr.fragment.customer;

import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.PlanOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.fragment.UiHelper.isValidEmail;

/**
 * Created by vkompaniets on 24.06.2016.
 */
@EFragment(R.layout.customer_personal_info_fragment)
public class CustomerPersonalInfoFragment extends CustomerBaseFragment implements CustomerView{

    @ViewById protected EditText firstName;
    @ViewById protected EditText lastName;
    @ViewById protected EditText email;

    @Override
    @AfterViews
    protected void init() {
        super.init();
    }

    @Override
    protected void setViews() {
        super.setViews();
        setFieldsEnabled(PlanOptions.isEditingCustomersAllowed());
    }

    protected void setCustomer(){
        final CustomerModel customer = getCustomer();
        firstName.setText(customer.firstName);
        lastName.setText(customer.lastName);
        email.setText(customer.email);
    }

    @Override
    public void collectDataToModel(CustomerModel model) {
        model.firstName = firstName.getText().toString();
        model.lastName = lastName.getText().toString();
        model.email = email.getText().toString();
    }

    @Override
    public void setFieldsEnabled(boolean enabled) {
        firstName.setEnabled(enabled);
        lastName.setEnabled(enabled);
        email.setEnabled(enabled);
    }

    @Override
    public boolean validateView() {
        if (TextUtils.isEmpty(firstName.getText().toString().trim())) {
            Toast.makeText(getActivity(), R.string.customer_edit_first_name_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(lastName.getText().toString().trim())) {
            Toast.makeText(getActivity(), R.string.customer_edit_last_name_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        String emailText = email.getText().toString().trim();
        if (!TextUtils.isEmpty(emailText) && !isValidEmail(emailText)) {
            Toast.makeText(getActivity(), R.string.customer_edit_email_not_valid_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
