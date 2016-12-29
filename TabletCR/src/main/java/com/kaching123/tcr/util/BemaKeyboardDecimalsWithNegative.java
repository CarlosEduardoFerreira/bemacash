package com.kaching123.tcr.util;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ferre on 28/12/2016.
 */

public class BemaKeyboardDecimalsWithNegative implements InputFilter {

    Pattern mPattern;

    public BemaKeyboardDecimalsWithNegative(int digitsBeforeZero,int digitsAfterZero) {
        mPattern= Pattern.compile("[-+]?[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        Matcher matcher=mPattern.matcher(dest);
        if(!matcher.matches())
            return "";
        return null;
    }

}