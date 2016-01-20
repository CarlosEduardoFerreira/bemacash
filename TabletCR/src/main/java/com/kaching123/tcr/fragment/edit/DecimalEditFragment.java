package com.kaching123.tcr.fragment.edit;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.CustomEditBox;

import java.math.BigDecimal;

import static com.kaching123.tcr.util.CalculationUtil.negative;

/**
 * Created by gdubina on 14/11/13.
 */
@EFragment
public abstract class DecimalEditFragment extends KeyboardDialogFragment {

    @ViewById
    protected CustomEditBox editText;

    @FragmentArg
    protected String saleItemGuid;

    @FragmentArg
    protected BigDecimal decimalValue;

    @FragmentArg
    protected boolean isInteger;

    @ColorRes(R.color.light_gray)
    protected int normalTextColor;

    protected CurrencyTextWatcher currencyTextWatcher;


    @ColorRes(R.color.subtotal_tax_empty)
    protected int errorTextColor;

    @AfterViews
    protected void attachViews() {
        currencyTextWatcher = new CurrencyTextWatcher(editText);
        editText.setKeyboardSupportConteiner(this);
        editText.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        keyboard.attachEditView(editText);
        keyboard.setDotEnabled(!isInteger);
        editText.addTextChangedListener(currencyTextWatcher);
        editText.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                callInternalListener(submitListener);
                return false;
            }
        });
    }

    @AfterTextChange
    protected void editTextAfterTextChanged(Editable s) {
        checkPositiveButtonCondition();
    }

    protected void checkPositiveButtonCondition() {
        BigDecimal value = validateForm();
        enablePositiveButtons(value != null);
        editText.setTextColor(value != null ? normalTextColor : errorTextColor);
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return submitListener;
    }

    protected boolean onSubmitForm() {
        BigDecimal value = validateForm();
        if (value == null) {
            return false;
        }
        callCommand(saleItemGuid, value);
        if (onResultListener != null) {
            onResultListener.onComplete();
        }
        return true;
    }

    protected abstract void callCommand(String saleItemGuid, BigDecimal value);

    protected BigDecimal validateForm() {
        BigDecimal value = getDecimalValue();
        BigDecimal maxValue = getMaxValue();
        return value != null && value.compareTo(BigDecimal.ZERO) == 1 && (maxValue == null || value.compareTo(maxValue) != 1) ? value : null;
    }

    protected BigDecimal getMaxValue() {
        return null;
    }

    protected BigDecimal getDecimalValue() {
        String text = editText.getText().toString().replaceAll(",", "");;
        try {
            if (text.endsWith("-")){
                return negative(new BigDecimal(text.substring(0, text.length() - 1)));
            }else {
                return new BigDecimal(text);
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        editText.requestFocus();
        enablePositiveButtons(false);
     }

    protected OnDialogClickListener submitListener = new OnDialogClickListener() {
        @Override
        public boolean onClick() {
            return onSubmitForm();
        }
    };

    private OnResultListener onResultListener;

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public static interface OnResultListener {
        void onComplete();
    }
}
