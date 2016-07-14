package com.kaching123.tcr.fragment;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.PhoneUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import static com.kaching123.tcr.util.CalculationUtil.negativeQty;

/**
 * Created by gdubina on 06/11/13.
 */
public final class UiHelper {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private static final DecimalFormat integerFormat = new DecimalFormat("0.##");
    private static final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");
    private static final DecimalFormat percentFormat = new DecimalFormat("0.000");
    private static final DecimalFormat integralIntegerFormat = new DecimalFormat("0");

    private static final DecimalFormat quantityFormat = new DecimalFormat("#,##0.000");
    private static final DecimalFormat quantityIntegerFormat = new DecimalFormat("0.###");

    private static final DecimalFormat brandQtyFormat = new DecimalFormat("#,##0.000");
    private static final DecimalFormat brandQrtyIntFormat = new DecimalFormat("#,###");
    private static final DecimalFormat priceInputFormat = new DecimalFormat("#,###.##");

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');

        decimalFormat.setDecimalFormatSymbols(otherSymbols);
        integerFormat.setDecimalFormatSymbols(otherSymbols);


        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        priceFormat.setDecimalFormatSymbols(symbols);
        priceFormat.setParseBigDecimal(true);
        priceFormat.setRoundingMode(RoundingMode.HALF_UP);

        priceInputFormat.setDecimalFormatSymbols(symbols);
        priceInputFormat.setRoundingMode(RoundingMode.HALF_UP);
        priceInputFormat.setParseBigDecimal(true);
        priceInputFormat.setNegativePrefix("-");

        percentFormat.setDecimalFormatSymbols(otherSymbols);
        integralIntegerFormat.setDecimalFormatSymbols(otherSymbols);
        quantityFormat.setDecimalFormatSymbols(otherSymbols);
        quantityIntegerFormat.setDecimalFormatSymbols(otherSymbols);

        integralIntegerFormat.setRoundingMode(RoundingMode.DOWN);

        brandQrtyIntFormat.setDecimalFormatSymbols(symbols);
        brandQrtyIntFormat.setParseBigDecimal(true);
        brandQrtyIntFormat.setRoundingMode(RoundingMode.FLOOR);

        brandQtyFormat.setDecimalFormatSymbols(symbols);
        brandQtyFormat.setParseBigDecimal(true);
        brandQtyFormat.setRoundingMode(RoundingMode.FLOOR);
    }

    private UiHelper() {
    }

    public static BigDecimal parseBigDecimal(String text) {
        BigDecimal value = BigDecimal.ZERO;
        try {
            value = (BigDecimal) priceFormat.parseObject(text);
        } catch (ParseException e) {
            Logger.e("Parse error: " + e.toString());
        }
        return value;
    }

    public static BigDecimal parseBigDecimal(TextView editText, BigDecimal def) {
        return parseBigDecimal(editText.getText().toString(), def);
    }

    public static BigDecimal parseBigDecimal(String str, BigDecimal def) {
        if (TextUtils.isEmpty(str))
            return def;
        try {
            str = str.replaceAll(",","");
            return new BigDecimal(str);
        } catch (Exception e) {
            try {
                return new BigDecimal(str.replaceAll(",", "."));
            } catch (Exception e2) {
                return def;
            }
        }
    }

    public static BigDecimal parseBrandDecimalInput(String text) {
        BigDecimal value = BigDecimal.ZERO;
        try {
            value = (BigDecimal) priceInputFormat.parseObject(text);
        } catch (ParseException e) {
            Logger.e("Parse error: " + e.toString());
        }
        return value;
    }

    public static void showBrandQty(TextView textView, BigDecimal price) {
        if (textView == null) {
            return;
        }
        if (price == null) {
            textView.setText(null);
        } else {
            textView.setText(brandQtyFormat.format(price));
        }
    }

    public static void showBrandQtyInteger(TextView textView, BigDecimal price) {
        if (textView == null) {
            return;
        }
        if (price == null) {
            textView.setText(null);
        } else {
            textView.setText(brandQrtyIntFormat.format(price));
        }
    }

    public static BigDecimal getDecimalValue(TextView editText) {
        String text = editText.getText().toString().replaceAll("\\,", "");
        try {
            if (text.endsWith("-")) {
                return negativeQty(new BigDecimal(text.substring(0, text.length() - 1)));
            } else {
                return new BigDecimal(text);
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }


    public static String brandFormat(BigDecimal price) {
        if (price == null)
            return null;
        return brandQtyFormat.format(price);
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
            String formattedPrice = priceFormat.format(price);
            textView.setText(formattedPrice);
        }
    }

    public static String brandQtyFormat(BigDecimal qty) {
        if (qty == null)
            return null;
        return brandQtyFormat.format(qty);
    }

    public static String brandQtyIntFormat(BigDecimal qty) {
        if (qty == null) {
            return null;
        }
        return brandQrtyIntFormat.format(qty);
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

    public static void showQuantity(TextView textView, BigDecimal decimal, boolean isPcsUnit) {
        if (decimal == null) {
            textView.setText(null);
        } else if (isPcsUnit){
            textView.setText(quantityIntegerFormat.format(decimal));
        } else{
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

    public static BigDecimal parseBrandQtyInput(String text) {
        BigDecimal value = BigDecimal.ZERO;

        if(!TextUtils.isEmpty(text)) {
        try {
            value = (BigDecimal) brandQrtyIntFormat.parseObject(text.replaceAll("\\,", ""));
        } catch (ParseException e) {
            Logger.e("Parse error: " + e.toString());
        }
        }
        return value;
    }

    public static String qtyFormat(BigDecimal qty, boolean isPcsUnit) {
        if (qty == null){
            return null;
        }else if (isPcsUnit){
            return quantityIntegerFormat.format(qty);
        }else {
            return quantityFormat.format(qty);
        }
    }

    public static String integerFormat(BigDecimal decimal) {
        if (decimal == null)
            return null;
        return integerFormat.format(decimal);
    }

    public static String integralIntegerFormat(BigDecimal decimal) {
        if (decimal == null)
            return null;
        return integralIntegerFormat.format(decimal);
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

    public static String percentFormat(BigDecimal percent) {
        if (percent == null)
            return null;
        return String.format(Locale.US, "%s%%", integerFormat.format(percent));
    }

    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return true; //empty email do not forbidden
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static String concatFullname(String firstName, String lastName) {
        boolean firstEmpty = TextUtils.isEmpty(firstName);
        boolean lastEmpty = TextUtils.isEmpty(lastName);

        if (firstEmpty && lastEmpty) {
            return null;
        } else if (firstEmpty) {
            return lastName;
        } else if (lastEmpty) {
            return firstName;
        } else {
            return firstName + " " + lastName;
        }
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
