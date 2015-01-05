package com.kaching123.tcr.model;

public enum PriceType {
    FIXED, OPEN, UNIT_PRICE;


    public static PriceType valueOf(int id) {
        return values()[id];
    }
}
