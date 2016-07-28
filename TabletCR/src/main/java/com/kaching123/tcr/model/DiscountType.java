package com.kaching123.tcr.model;

/**
 * Created by gdubina on 06/11/13.
 */
public enum DiscountType {
    PERCENT("Percent"),
    VALUE("Value");

    private String name;

    DiscountType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static DiscountType valueOf(int id) {
        return values()[id];
    }

}
