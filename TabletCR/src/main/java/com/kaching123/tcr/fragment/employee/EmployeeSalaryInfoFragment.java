package com.kaching123.tcr.fragment.employee;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseEmployeeActivity;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.SignedCurrencyFormatInputFilter;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.util.CalculationUtil;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by mboychenko on 5/11/2017.
 */
@EFragment(R.layout.employee_salary_info_fragment)
public class EmployeeSalaryInfoFragment extends EmployeeBaseFragment implements EmployeeView {

    @ViewById
    protected EditText hourlyRate;
    @ViewById
    protected EditText overtimeRate;
    @ViewById
    protected EditText overtimeStarts;
    @ViewById
    protected CheckBox clockInMandatory;
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

    protected CurrencyTextWatcher currencyTextWatcher;

    @Override
    protected void setViews() {
        InputFilter[] signedDecimalFilter = new InputFilter[]{new SignedCurrencyFormatInputFilter()};
        InputFilter[] decimalFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
        currencyTextWatcher = new CurrencyTextWatcher(hourlyRate);
        hourlyRate.setFilters(signedDecimalFilter);
        hourlyRate.addTextChangedListener(currencyTextWatcher);
        commissions.setFilters(decimalFilter);


        tipsEligible.setVisibility(getApp().isTipsEnabled() ? View.VISIBLE : View.GONE);
        commissionsEligibleContainer.setVisibility(getApp().isCommissionsEnabled() ? View.VISIBLE : View.GONE);
        commissionsContainer.setVisibility(getApp().isCommissionsEnabled() ? View.VISIBLE : View.GONE);

        if (getMode() == BaseEmployeeActivity.EmployeeMode.CREATE) {
            tipsEligible.setChecked(getApp().isTipsEnabled());
        }
    }

    @Override
    protected void setEmployee() {
        EmployeeModel model = getEmployee();
        showPrice(hourlyRate, model.hRate);
        tipsEligible.setChecked(model.tipsEligible);
        commissionsEligible.setChecked(model.commissionEligible);
        showPrice(commissions, model.commission);

        showPrice(overtimeRate, model.overtimeRate);
        overtimeStarts.setText(model.overtimeStartsFrom);
        clockInMandatory.setChecked(model.clockInMandatory);

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(6);
        hourlyRate.setFilters(filterArray);
        overtimeRate.setFilters(filterArray);
        commissions.setFilters(filterArray);
    }

    @Override
    public void collectDataToModel(EmployeeModel model) {
        model.hRate = parseBigDecimal(hourlyRate, BigDecimal.ZERO);
        model.tipsEligible = tipsEligible.isChecked();
        model.commissionEligible = commissionsEligible.isChecked();
        model.commission = parseBigDecimal(commissions, BigDecimal.ZERO);
        model.overtimeRate = parseBigDecimal(overtimeRate, BigDecimal.ZERO);
        model.overtimeStartsFrom = overtimeStarts.getText().toString();
        model.clockInMandatory = clockInMandatory.isChecked();
    }

    @Override
    public void setFieldsEnabled(boolean enabled) {

    }

    @Override
    public boolean validateView() {
        if (!TextUtils.isEmpty(commissions.getText())) {
            if (parseBigDecimal(commissions, BigDecimal.ZERO).compareTo(CalculationUtil.ONE_HUNDRED) == 1) {
                Toast.makeText(getContext(), R.string.commission_validation_alert_msg, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasChanges(EmployeeModel initModel) {
        DecimalFormat formatar = new DecimalFormat("###,###,###,###,###.##");
        formatar.setMinimumFractionDigits(2);

        BigDecimal hR1 = initModel.hRate == null ? BigDecimal.ZERO : initModel.hRate;
        String hR2String = hourlyRate.getText().toString().replaceAll(",", "");
        BigDecimal hR2 = hourlyRate.getText().toString().equals("") ? BigDecimal.ZERO : new BigDecimal(hR2String);
        String hRate1 = formatar.format(hR1);
        String hRate2 = formatar.format(hR2);


        BigDecimal co1 = initModel.commission == null ? BigDecimal.ZERO : initModel.commission;
        String co2String = commissions.getText().toString().replaceAll(",", "");
        BigDecimal co2 = commissions.getText().toString().equals("") ? BigDecimal.ZERO : new BigDecimal(co2String);
        String commission1 = formatar.format(co1);
        String commission2 = formatar.format(co2);



        if (!hRate1.equals(hRate2)) {
            Log.d("BemaCarl3", "EmployeeSalaryInfoFragment.employeeHasChanges.hourlyRate: |" + hRate1 + "|" + hRate2 + "|");
            return true;
        }
        if (initModel.tipsEligible != tipsEligible.isChecked()) {
            Log.d("BemaCarl3", "EmployeeSalaryInfoFragment.employeeHasChanges.tipsEligible");
            return true;
        }
        if (initModel.commissionEligible != commissionsEligible.isChecked()) {
            Log.d("BemaCarl3", "EmployeeSalaryInfoFragment.employeeHasChanges.commissionsEligible");
            return true;
        }
        if (!commission1.equals(commission2)) {
            Log.d("BemaCarl3", "EmployeeSalaryInfoFragment.employeeHasChanges.commissions");
            return true;
        }
        return false;
    }
}
