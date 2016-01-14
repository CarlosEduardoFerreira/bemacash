package com.kaching123.tcr.component;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.kaching123.tcr.fragment.UiHelper;

import java.math.BigDecimal;

/**
 * Created by Hans
 */
public class BrandTextWatcher implements TextWatcher {

    private static final char DECIMAL_SEPARATOR = '.';

    private boolean isDeleting;
    protected boolean isEditMode;
    private boolean isDecimalPart;

    private final static int MAX_INT = 6;
    private final static int MAX_DEC = 3;

    private final TextView view;

    public BrandTextWatcher(final TextView view) {
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
            if (isSeparatorCharacter(value)) {
                isDecimalPart = true;
                String intPart = value.replaceAll("\\,", "").replaceAll(String.valueOf("\\."), "");
                if (TextUtils.isEmpty(intPart)){
                    intPart = "0";
                }
                final String priceFormattedString = UiHelper.brandFormat(new BigDecimal(intPart));
                isEditMode = true;
                amount.clear();
                isEditMode = true;
                this.view.setText(priceFormattedString.substring(0, priceFormattedString.indexOf(DECIMAL_SEPARATOR) + 1));
                setSelection();
            } else if (isDecimalPart) {
                final String decimalPart = value.substring(value.indexOf(DECIMAL_SEPARATOR) + 1);
                if (decimalPart.length() > 2) {
                    String intPart = value.replaceAll("\\,", "");
                    intPart = intPart.substring(0, intPart.indexOf(DECIMAL_SEPARATOR) + 4);
                    String[] sp = intPart.split(String.valueOf("\\."));

                    String integerPart = sp[0];
                    if (integerPart.length() > MAX_INT) {
                        integerPart = integerPart.substring(0, MAX_INT);
                    }
                    String decPart = sp[1];
                    intPart = integerPart + String.valueOf(DECIMAL_SEPARATOR) + decPart;
                    final String priceFormattedString = UiHelper.brandFormat(UiHelper.parseBrandDecimalInput(intPart));
                    isEditMode = true;
                    amount.clear();
                    isEditMode = true;
                    this.view.setText(priceFormattedString);
                    setSelection();
                }
            }
        }
        if (!isDecimalPart) {
            String intPart = value.replaceAll("\\,", "");
            if (intPart.length() > MAX_INT) {
                intPart = intPart.substring(0, MAX_INT);
            }
            final String priceFormattedString = UiHelper.brandFormat(UiHelper.parseBrandDecimalInput(intPart));
            isEditMode = true;
            amount.clear();
            isEditMode = true;
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