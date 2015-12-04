package com.kaching123.tcr.util;

import com.kaching123.tcr.model.PriceType;

/**
 * Created by hamsterksu on 11.12.13.
 */
public final class UnitUtil {

    public static final String PCS_LABEL = "pcs";
    private static final String PCS_DOT = "pcs.";

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
}
