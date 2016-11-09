package com.kaching123.tcr.util;

import com.kaching123.tcr.model.PriceType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hamsterksu on 11.12.13.
 */
public final class UnitUtil {

    public static final String LBS_LABEL = "lbs";
    public static final String PCS_LABEL = "pcs";
    private static final String PCS_DOT = "pcs.";

    private final static String REGEXP = "[^\\x20-\\x7E]|[\\x7C]";
    private static Pattern pattern = Pattern.compile(REGEXP);

    public final static int MAX_LENGTH = 6;


    private UnitUtil(){}

//    public static boolean isPcs(String unitLabel){
//        return PCS_LABEL.equalsIgnoreCase(unitLabel) || PCS_DOT.equalsIgnoreCase(unitLabel);
//    }

    public static boolean isPcs(PriceType type){
        return !PriceType.UNIT_PRICE.equals(type);
    }

    public static boolean isNotUnitPriceType(PriceType type) {
        return !PriceType.UNIT_PRICE.equals(type);
    }

    public static boolean isContainInvalidChar(CharSequence shortcut) {
        Matcher matcher = pattern.matcher(shortcut);
        return matcher.find();
    }

    public static boolean isUnitLbs(String  shortCut) {
       return shortCut.equalsIgnoreCase(LBS_LABEL);
    }

}
