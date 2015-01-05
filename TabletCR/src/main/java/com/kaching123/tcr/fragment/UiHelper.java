package com.kaching123.tcr.fragment;

import android.text.TextUtils;
import android.widget.TextView;

import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.PhoneUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by gdubina on 06/11/13.
 */
public final class UiHelper {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private static final DecimalFormat integerFormat = new DecimalFormat("0.##");
    private static final DecimalFormat priceFormat = new DecimalFormat("0.00");
    private static final DecimalFormat percentFormat = new DecimalFormat("0.##");
    private static final DecimalFormat integralIntegerFormat = new DecimalFormat("0");
    private static final DecimalFormat quantityFormat = new DecimalFormat("0.000");
    private static final DecimalFormat quantityIntegerFormat = new DecimalFormat("0.###");

    //private static final DecimalFormat percentInBracketsFormat = new DecimalFormat(" (0.##)");

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');

        decimalFormat.setDecimalFormatSymbols(otherSymbols);
        integerFormat.setDecimalFormatSymbols(otherSymbols);
        priceFormat.setDecimalFormatSymbols(otherSymbols);
        percentFormat.setDecimalFormatSymbols(otherSymbols);
        integralIntegerFormat.setDecimalFormatSymbols(otherSymbols);
        quantityFormat.setDecimalFormatSymbols(otherSymbols);
        quantityIntegerFormat.setDecimalFormatSymbols(otherSymbols);

        integralIntegerFormat.setRoundingMode(RoundingMode.DOWN);
    }

    private UiHelper() {
    }

    public static BigDecimal parseBigDecimal(TextView editText, BigDecimal def) {
        return parseBigDecimal(editText.getText().toString(), def);
    }

    public static BigDecimal parseBigDecimal(String str, BigDecimal def) {
        if (TextUtils.isEmpty(str))
            return def;
        try {
            return new BigDecimal(str);
        } catch (Exception e) {
            return def;
        }
    }

    public static void showDecimal(TextView textView, BigDecimal decimal) {
        if (decimal == null) {
            textView.setText(null);
        } else {
            textView.setText(decimalFormat.format(decimal));
        }
    }

    public static void showInteger(TextView textView, BigDecimal decimal) {
        if (decimal == null) {
            textView.setText(null);
        } else {
            textView.setText(integerFormat.format(decimal));
        }
    }

    public static void showQuantityInteger(TextView textView, BigDecimal decimal) {
        if (decimal == null) {
            textView.setText(null);
        } else {
            textView.setText(quantityIntegerFormat.format(decimal));
        }
    }

    public static void showIntegralInteger(TextView textView, BigDecimal decimal) {
        if (decimal == null) {
            textView.setText(null);
        } else {
            textView.setText(integralIntegerFormat.format(decimal));
        }
    }

    public static void showPrice(TextView textView, BigDecimal price) {
        if (textView == null) {
            return;
        }
        if (price == null) {
            textView.setText(null);
        } else {
            textView.setText(priceFormat.format(price));
        }
    }

    public static void showAddonPrice(TextView textView, BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            textView.setText(null);
        } else {
            textView.setText("+ " + priceFormat.format(price));
        }
    }

    public static void showPercent(TextView textView, BigDecimal percent) {
        if (percent == null) {
            textView.setText(null);
        } else {
            textView.setText(String.format(Locale.US, "%s %%", percentFormat.format(percent)));
        }
    }

    public static void showPercentInBrackets(TextView textView, BigDecimal percent) {
        if (percent == null) {
            textView.setText(null);
        } else {
            textView.setText(String.format(Locale.US, " (%s %%)", percentFormat.format(percent)));
        }
    }

    public static void showQuantity(TextView textView, BigDecimal decimal) {
        if (decimal == null) {
            textView.setText(null);
        } else {
            textView.setText(quantityFormat.format(decimal));
        }
    }

    public static void showPhone(TextView textView, String phone) {
        if (phone == null) {
            textView.setText(null);
        } else {
            textView.setText(PhoneUtil.isValid(phone) ? PhoneUtil.parse(phone) : phone);
        }
    }

    public static String priceFormat(BigDecimal price) {
        if (price == null)
            return null;
        return priceFormat.format(price);
    }

    public static String qtyFormat(BigDecimal qty) {
        if (qty == null)
            return null;
        return quantityFormat.format(qty);
    }

    public static String trimFormat(BigDecimal qty) {
        if (qty == null)
            return null;
        return integerFormat.format(qty);
    }

    public static String valueOf(BigDecimal value) {
        if (value == null)
            return null;
        return priceFormat(CalculationUtil.value(value));
    }

    public static String formatLastFour(String value) {
        if (value == null)
            return null;
        return String.format("####-####-####-%s", value);
    }

    public static String formatPercent(BigDecimal percent) {
        if (percent == null)
            return null;
        return String.format(Locale.US, "%s %%", percentFormat.format(percent));
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static String concatFullname(String firstName, String lastName){
        boolean firstEmpty = TextUtils.isEmpty(firstName);
        boolean lastEmpty = TextUtils.isEmpty(lastName);

        if (firstEmpty && lastEmpty){
            return null;
        } else if (firstEmpty){
            return lastName;
        } else if (lastEmpty){
            return firstName;
        } else {
            return firstName + " " + lastName;
        }
    }

}