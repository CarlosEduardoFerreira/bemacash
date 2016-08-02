package com.kaching123.tcr.model;

public enum PriceType {
    FIXED("Fixed"),
    OPEN("Open"),
    UNIT_PRICE("Unit Price");

    private String name;

    PriceType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static PriceType valueOf(int id) {
        return values()[id];
    }
}
