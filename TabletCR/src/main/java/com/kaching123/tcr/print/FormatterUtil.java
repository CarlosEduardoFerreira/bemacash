package com.kaching123.tcr.print;

import com.kaching123.tcr.TcrApplication;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by gdubina on 12.02.14.
 */
public final class FormatterUtil {

    private FormatterUtil(){}

    private static DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
    static{
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
    }

    private static ThreadLocal<DecimalFormat> priceFormat = new ThreadLocal<DecimalFormat>(){
        protected DecimalFormat initialValue() {
            DecimalFormat format = new DecimalFormat("0.00");
            format.setDecimalFormatSymbols(otherSymbols);
            return format;
        }
    };

    private static ThreadLocal<DecimalFormat> decimalPercentFormat = new ThreadLocal<DecimalFormat>(){
        protected DecimalFormat initialValue() {
            DecimalFormat format = new DecimalFormat("0.000");
            format.setDecimalFormatSymbols(otherSymbols);
            return format;
        }
    };

    private static ThreadLocal<DecimalFormat> commaPriceFormat = new ThreadLocal<DecimalFormat>(){
        protected DecimalFormat initialValue() {
            DecimalFormat format = new DecimalFormat("#,##0.00");
            format.setDecimalFormatSymbols(otherSymbols);
            return format;
        }
    };

    private static ThreadLocal<DecimalFormat> commaFormat = new ThreadLocal<DecimalFormat>(){
        protected DecimalFormat initialValue() {
            DecimalFormat format = new DecimalFormat("#,##0.00");
            format.setDecimalFormatSymbols(otherSymbols);
            return format;
        }
    };

    private static ThreadLocal<DecimalFormat> commaBracketsPriceFormat = new ThreadLocal<DecimalFormat>(){
        protected DecimalFormat initialValue() {
            DecimalFormat format = new DecimalFormat("$ (#,##0.00)");
            format.setDecimalFormatSymbols(otherSymbols);
            return format;
        }
    };

    public static String priceFormat(BigDecimal price){
        return priceFormat.get().format(price);
    }

    public static String percentFormat(BigDecimal percent){
        return String.format(Locale.US, "%s %%", decimalPercentFormat.get().format(percent));
    }

    public static String commaPriceFormat(BigDecimal value){
        if(value.compareTo(BigDecimal.ZERO) == -1) {
            return String.format("-%1$s %2$s", TcrApplication.getCountryFunctionality().currencySymbol(), commaPriceFormat.get().format(value.abs()));
        } else {
            return String.format("%1$s %2$s", TcrApplication.getCountryFunctionality().currencySymbol(), commaPriceFormat.get().format(value));
        }
    }

    public static String commaFormat(BigDecimal value){
        return commaFormat.get().format(value);
    }

    public static String commaBracketsPriceFormat(BigDecimal value){
        if(value.compareTo(BigDecimal.ZERO) == -1) {
            return String.format("-%1$s(%2$s)", TcrApplication.getCountryFunctionality().currencySymbol(), commaPriceFormat.get().format(value.abs()));
        } else {
            return String.format("%1$s(%2$s)", TcrApplication.getCountryFunctionality().currencySymbol(), commaPriceFormat.get().format(value));
        }
    }
}
