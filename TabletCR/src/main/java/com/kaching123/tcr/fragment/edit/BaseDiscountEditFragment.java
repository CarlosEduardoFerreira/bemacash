package com.kaching123.tcr.fragment.edit;

import android.os.Bundle;
import android.text.InputFilter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.PercentFormatInputFilter;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;

/**
 * Created by gdubina on 14/11/13.
 */
@EFragment
public abstract class BaseDiscountEditFragment extends DecimalEditFragment {

    @ViewById
    protected Switch discountPercentType;

    @FragmentArg
    protected DiscountType discountType;

    @FragmentArg
    protected BigDecimal maxValue;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        discountPercentType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editText.setFilters(new InputFilter[]{
                        isChecked ? new PercentFormatInputFilter() : new CurrencyFormatInputFilter()
                });
                checkPositiveButtonCondition();
            }
        });
        discountPercentType.setChecked(discountType == DiscountType.PERCENT);

    }

    @Override
    protected BigDecimal validateForm(){
        BigDecimal value = getDecimalValue();
        boolean notZero = value != null && value.compareTo(BigDecimal.ZERO) != -1;
        if(discountPercentType.isChecked()){
            return notZero && value.compareTo(CalculationUtil.ONE_HUNDRED) == -1 ? value : null;
        }
        return notZero && (maxValue == null || value.compareTo(maxValue) == -1) ? value : null;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.edit_discount_dialog_fragment;
    }

}
