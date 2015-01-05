package com.kaching123.tcr.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by pkabakov on 27.06.2014.
 */
public class RequestsUtil {

    private static ThreadLocal<DecimalFormat> centsFormat = new ThreadLocal<DecimalFormat>() {
        protected DecimalFormat initialValue() {
            return new DecimalFormat("000");
        }
    };

    public static String centsFormat(BigDecimal centsAmount) {
        if (centsAmount == null)
            return null;
        return centsFormat.get().format(centsAmount);
    }

}
