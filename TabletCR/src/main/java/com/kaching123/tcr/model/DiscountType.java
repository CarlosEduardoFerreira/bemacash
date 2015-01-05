package com.kaching123.tcr.model;

/**
 * Created by gdubina on 06/11/13.
 */
public enum DiscountType {
    PERCENT, VALUE;

    public static DiscountType valueOf(int id) {
        return values()[id];
    }

}
