package com.kaching123.tcr.util;

import android.text.TextUtils;
import android.util.Patterns;

import com.kaching123.tcr.TcrApplication;

import java.util.regex.Pattern;

/**
 * Created by gdubina on 18/02/14.
 */
public final class Validator {

    private static Pattern IP = Patterns.IP_ADDRESS;
    private static Pattern MAC = Pattern.compile("^([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])$");

    private Validator(){}

    public static boolean isIp(String ip){
        return IP.matcher(ip).matches();
    }

    public static boolean isMAc(String mac){
        return MAC.matcher(mac).matches();
    }

    public static boolean isIMEI(String imei) {
        return LuhnUtil.isLuhnValid(imei);
    }

    public static boolean isEanValid(String ean) {
        return (!TextUtils.isEmpty(ean) && ean.length() <= TcrApplication.EAN_UPC_CODE_MAX_LEN && TextUtils.isDigitsOnly(ean));
    }

    public static boolean isProductCodeValid(String code) {
        return (!TextUtils.isEmpty(code) && code.length() <= TcrApplication.PRODUCT_CODE_MAX_LEN);
    }
}
