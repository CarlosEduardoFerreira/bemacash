package com.kaching123.tcr.fragment.customer;

import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.component.DatePickerDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.LoyaltyPlanModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.LoyaltyPlanTable;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showIntegralInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPhone;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.DateUtils.dateOnlyFormat;
import static com.kaching123.tcr.util.PhoneUtil.isValid;
import static com.kaching123.tcr.util.PhoneUtil.onlyDigits;

/**
 * Created by vkompaniets on 24.06.2016.
 */
@EFragment(R.layout.customer_general_info_fragment)
public class CustomerGeneralInfoFragment extends CustomerBaseFragment implements CustomerView, BarcodeReceiver{

    @ViewById protected EditText street;
    @ViewById protected EditText street2;
    @ViewById protected EditText city;
    @ViewById protected EditText state;
    @ViewById protected EditText country;
    @ViewById protected EditText zip;
    @ViewById protected EditText phone;
    @ViewById protected EditText identification;
    @ViewById protected EditText bonusPoints;
    @ViewById protected TextView birthday;
    @ViewById protected EditText loyaltyBarcode;
    @ViewById protected Spinner gender;
    @ViewById protected Spinner loyaltyPlan;
    @ViewById protected CheckBox emailPromotion;

    private Calendar birthdayDate = Calendar.getInstance();
    private boolean birthdaySet = false;

    private LoyaltyPlanAdapter loyaltyPlanAdapter;

    @Override
    @AfterViews
    protected void init(){
        birthdayDate.set(1990, 0, 1);
        super.init();
    }

    @Override
    protected void setViews() {
        super.setViews();
        gender.setAdapter(new SexAdapter());
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        setFieldsEnabled(PlanOptions.isEditingCustomersAllowed());
        loyaltyPlanAdapter = new LoyaltyPlanAdapter(getActivity());
        loyaltyPlan.setAdapter(loyaltyPlanAdapter);
        bonusPoints.setEnabled(getApp().hasPermission(Permission.CUSTOMER_LOYALTY_BONUS_POINTS));
        getLoaderManager().restartLoader(0, null, loyaltyPlanLoader);
    }

    @Override
    protected void setCustomer() {
        super.setCustomer();

        CustomerModel model = getCustomer();
        street.setText(model.street);
        street2.setText(model.complementary);
        city.setText(model.city);
        state.setText(model.state);
        country.setText(model.country);
        zip.setText(model.zip);
        gender.setSelection(model.sex ? Sex.MALE.ordinal() : Sex.FEMALE.ordinal());
        identification.setText(model.customerIdentification);
        emailPromotion.setChecked(model.consentPromotions);
        identification.setText(model.customerIdentification);
        showPhone(phone, model.phone);
        if (model.birthday != null){
            birthdayDate.setTime(model.birthday);
            birthday.setText(dateOnlyFormat(model.birthday));
        }
        showIntegralInteger(bonusPoints, model.loyaltyPoints);
        loyaltyBarcode.setText(model.loyaltyBarcode);
    }

    @Override
    public void collectDataToModel(CustomerModel model) {
        model.street = street.getText().toString();
        model.complementary = street2.getText().toString();
        model.city = city.getText().toString();
        model.state = state.getText().toString();
        model.country = country.getText().toString();
        model.zip = zip.getText().toString();
        model.phone = onlyDigits(phone.getText().toString());
        model.customerIdentification = identification.getText().toString();
        model.sex = gender.getSelectedItem() == Sex.MALE;
        model.consentPromotions = emailPromotion.isChecked();
        model.customerIdentification = identification.getText().toString();
        if (birthdaySet){
            model.birthday = birthdayDate.getTime();
        }
        model.loyaltyPlanId = ((LoyaltyPlanModel) loyaltyPlan.getSelectedItem()).guid;
        model.loyaltyBarcode = loyaltyBarcode.getText().toString();
        model.loyaltyPoints = _decimal(bonusPoints.getText().toString());
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

    @Click
    protected void birthdayClicked(){
        showDatePicker();
    }

    private void showDatePicker(){
        DatePickerDialogFragment.show(getActivity(), birthdayDate, new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                birthdayDate.set(year, monthOfYear, dayOfMonth);
                birthday.setText(dateOnlyFormat(birthdayDate.getTime()));
                birthdaySet = true;
            }
        });
    }

    private LoaderCallbacks<List<LoyaltyPlanModel>> loyaltyPlanLoader = new LoaderCallbacks<List<LoyaltyPlanModel>>() {
        @Override
        public Loader<List<LoyaltyPlanModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(LoyaltyPlanTable.URI_CONTENT))
                    .wrap(new Function<Cursor, List<LoyaltyPlanModel>>() {
                        @Override
                        public List<LoyaltyPlanModel> apply(Cursor input) {
                            ArrayList<LoyaltyPlanModel> result = new ArrayList<>(input.getCount() + 1);
                            result.add(new LoyaltyPlanModel(null, "None"));
                            while (input.moveToNext()){
                                result.add(new LoyaltyPlanModel(
                                        input.getString(input.getColumnIndex(LoyaltyPlanTable.GUID)),
                                        input.getString(input.getColumnIndex(LoyaltyPlanTable.NAME))
                                ));
                            }
                            return result;
                        }
                    })
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<LoyaltyPlanModel>> loader, List<LoyaltyPlanModel> data) {
            loyaltyPlanAdapter.changeCursor(data);
            int position = AdapterView.INVALID_POSITION;
            if (getCustomer() != null && getCustomer().loyaltyPlanId != null){
                position = loyaltyPlanAdapter.getPosition(getCustomer().loyaltyPlanId);
            } else if (getCustomer() == null && getApp().getShopInfo().defaultLoyaltyPlanId != null){
                position = loyaltyPlanAdapter.getPosition(getApp().getShopInfo().defaultLoyaltyPlanId);
            }
            if (position != AdapterView.INVALID_POSITION)
                loyaltyPlan.setSelection(position);
        }

        @Override
        public void onLoaderReset(Loader<List<LoyaltyPlanModel>> loader) {
            loyaltyPlanAdapter.changeCursor(null);
        }
    };

    @Override
    public void onBarcodeReceived(String barcode) {
        if (loyaltyBarcode != null)
            loyaltyBarcode.setText(barcode);
    }

    private class LoyaltyPlanAdapter extends ObjectsCursorAdapter<LoyaltyPlanModel> {

        public LoyaltyPlanAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return LayoutInflater.from(getActivity()).inflate(R.layout.spinner_item, parent, false);
        }

        @Override
        protected View bindView(View convertView, int position, LoyaltyPlanModel item) {
            TextView label = (TextView) convertView;
            label.setText(item.name);
            return convertView;
        }

        @Override
        protected View newDropDownView(int position, ViewGroup parent) {
            return LayoutInflater.from(getActivity()).inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        public int getPosition(String guid){
            for (int i = 0; i < getCount(); i++){
                if (guid.equals(getItem(i).guid))
                    return i;
            }
            return AdapterView.INVALID_POSITION;
        }
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
