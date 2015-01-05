package com.kaching123.tcr.util;

import android.text.TextUtils;

/**
 * Created by vkompaniets on 12.05.2014.
 */
public class PhoneUtil {

    // (AREA_CODE) PREFIX(3D)-SUFIX(4D)
    private static final int AREA_LEN = 3;
    private static final int PREFIX_LEN = 3;
    private static final int SUFIX_LEN = 4;

    private PhoneUtil(){};

    public static String parse(String phone) {
        if (phone == null)
            return null;

        String onlyDigits = onlyDigits(phone);
        int len = onlyDigits.length();
        if (len < PREFIX_LEN + SUFIX_LEN){
            return null;
        }

        String areaCode = onlyDigits.substring(0, len - PREFIX_LEN - SUFIX_LEN);
        String prefix = onlyDigits.substring(len - PREFIX_LEN - SUFIX_LEN, len - SUFIX_LEN);
        String sufix = onlyDigits.substring(len - SUFIX_LEN);

        return TextUtils.isEmpty(areaCode) ? String.format("%s-%s", prefix, sufix) : String.format("(%s) %s-%s", areaCode, prefix, sufix);
    }

    public static boolean isValid(String phone){
        if (TextUtils.isEmpty(phone))
            return false;

        return onlyDigits(phone).length() == AREA_LEN + SUFIX_LEN + PREFIX_LEN;
    }

    public static String onlyDigits(String phone){
        if (phone == null)
            return null;

        return phone.replaceAll("[^\\d]", "");
    }

}
