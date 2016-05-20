package com.kaching123.tcr.component;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.kaching123.tcr.fragment.UiHelper;

import java.math.BigDecimal;

/**
 * Created by idyuzheva on 12.08.2014.
 */
public class CurrencyTextWatcher implements TextWatcher {

    private static final char DECIMAL_SEPARATOR = '.';

    private boolean isDeleting;
    protected boolean isEditMode;
    private boolean isDecimalPart;

    private final TextView view;

    public CurrencyTextWatcher(final TextView view) {
        this.view = view;
    }

    @Override
    public synchronized void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (after == 0) {
            isDeleting = true;
        } else {
            isDeleting = false;
        }
    }

    @Override
    public synchronized void afterTextChanged(Editable amount) {
        final String value = amount.toString();
        if (TextUtils.isEmpty(value)) {
            return;
        }
        if (isEditMode) {
            isEditMode = false;
            return;
        }
        isDecimalPart = !isSeparatorDeleted(value);
        if (isInput()) {
            if (isNegativeCharacter(value)) {
                return;
            }
            if (isSeparatorCharacter(value)) {
                isDecimalPart = true;
                String intPart = value.replaceAll("\\,", "").replaceAll(String.valueOf("\\."), "");
                if (TextUtils.isEmpty(intPart)) {
                    intPart = "0";
                }
                final String priceFormattedString = UiHelper.priceFormat(new BigDecimal(intPart));
                isEditMode = true;
                amount.clear();
                isEditMode = true;
                this.view.setText(priceFormattedString.substring(0, priceFormattedString.indexOf(DECIMAL_SEPARATOR) + 1));
                setSelection();
            } else if (isDecimalPart) {
                final String decimalPart = value.substring(value.indexOf(DECIMAL_SEPARATOR) + 1);
                if (decimalPart.length() > 3) {
                    String intPart = value.replaceAll("\\,", "");
                    intPart = intPart.substring(0, intPart.indexOf(DECIMAL_SEPARATOR) + 3);
                    final String priceFormattedString = UiHelper.priceFormat(UiHelper.parseBrandDecimalInput(intPart));
                    isEditMode = true;
                    amount.clear();
                    isEditMode = true;
                    this.view.setText(priceFormattedString);
                    setSelection();
                }
            }
        }
        if (!isDecimalPart) {
            final String intPart = value.replaceAll("\\,", "");
            final String priceFormattedString = UiHelper.priceFormat(UiHelper.parseBrandDecimalInput(intPart));
            isEditMode = true;
            amount.clear();
            isEditMode = true;
            //amount.append(priceFormattedString.substring(0, priceFormattedString.indexOf(DECIMAL_SEPARATOR)));
            this.view.setText(priceFormattedString.substring(0, priceFormattedString.indexOf(DECIMAL_SEPARATOR)));
            setSelection();
        }
    }

    private void setSelection() {
        if (view instanceof EditText) {
            ((EditText) this.view).setSelection(view.getText().length());
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private boolean isSeparatorCharacter(final String inputText) {
        return inputText.endsWith(".") || inputText.endsWith(",");
    }

    private boolean isNegativeCharacter(final String inputText) {
        return inputText.equals("-");
    }

    private boolean isSeparatorDeleted(String amount) {
        return !contains(amount, DECIMAL_SEPARATOR);
    }

    private boolean isInput() {
        return !isDeleting;
    }

    private boolean contains(String str, char searchChar) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return str.indexOf(searchChar) >= 0;
    }
}