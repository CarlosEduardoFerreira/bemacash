package com.kaching123.tcr.fragment.customer;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.PlanOptions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static com.kaching123.tcr.fragment.UiHelper.showPhone;
import static com.kaching123.tcr.util.PhoneUtil.isValid;
import static com.kaching123.tcr.util.PhoneUtil.onlyDigits;

/**
 * Created by vkompaniets on 24.06.2016.
 */
@EFragment(R.layout.customer_general_info_fragment)
public class CustomerGeneralInfoFragment extends CustomerBaseFragment implements CustomerView{

    @ViewById protected EditText street;
    @ViewById protected EditText street2;
    @ViewById protected EditText city;
    @ViewById protected EditText state;
    @ViewById protected EditText country;
    @ViewById protected EditText zip;
    @ViewById protected EditText phone;
    @ViewById protected EditText identification;
    @ViewById protected EditText bonusPoints;
    @ViewById protected EditText birthday;
    @ViewById protected EditText loyaltyBarcode;
    @ViewById protected Spinner gender;
    @ViewById protected Spinner loyaltyPlan;
    @ViewById protected CheckBox emailPromotion;

    @Override
    @AfterViews
    protected void init(){
        super.init();
    }

    @Override
    protected void setViews() {
        super.setViews();
        gender.setAdapter(new SexAdapter());
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        setFieldsEnabled(PlanOptions.isEditingCustomersAllowed());
    }

    @Override
    protected void setCustomer() {
        super.setCustomer();

        CustomerModel model = getCustomer();
        street.setText(model.street);
        street2.setText(model.street);
        city.setText(model.city);
        state.setText(model.state);
        country.setText(model.country);
        zip.setText(model.zip);
        gender.setSelection(model.sex ? Sex.MALE.ordinal() : Sex.FEMALE.ordinal());
        identification.setText(model.customerIdentification);
        emailPromotion.setChecked(model.consentPromotions);
        identification.setText(model.customerIdentification);
        showPhone(phone, model.phone);
    }

    @Override
    public void collectDataToModel(CustomerModel model) {
        model.street = street.getText().toString();
        model.city = city.getText().toString();
        model.state = state.getText().toString();
        model.country = country.getText().toString();
        model.zip = zip.getText().toString();
        model.phone = onlyDigits(phone.getText().toString());
        model.customerIdentification = identification.getText().toString();
        model.sex = gender.getSelectedItem() == Sex.MALE;
        model.consentPromotions = emailPromotion.isChecked();
        model.customerIdentification = identification.getText().toString();
    }

    @Override
    public void setFieldsEnabled(boolean enabled) {
        street.setEnabled(enabled);
        street2.setEnabled(enabled);
        city.setEnabled(enabled);
        state.setEnabled(enabled);
        country.setEnabled(enabled);
        zip.setEnabled(enabled);
        phone.setEnabled(enabled);
        identification.setEnabled(enabled);
        bonusPoints.setEnabled(enabled);
        birthday.setEnabled(enabled);
        loyaltyBarcode.setEnabled(enabled);
        gender.setEnabled(enabled);
        loyaltyPlan.setEnabled(enabled);
        emailPromotion.setEnabled(enabled);
    }

    @Override
    public boolean validateView() {
        String phoneText = phone.getText().toString().trim();
        if (!TextUtils.isEmpty(phoneText) && !isValid(phoneText)) {
            Toast.makeText(getActivity(), R.string.customer_edit_phone_not_valid_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

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
            return LayoutInflater.from(getActivity()).inflate(R.layout.spinner_dropdown_item, parent, false);
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
            return LayoutInflater.from(getActivity()).inflate(R.layout.spinner_item, parent, false);
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
}
