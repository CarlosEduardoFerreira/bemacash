package com.kaching123.tcr.model;

/**
 * Created by arthur on 23/04/15.
 */
public enum ItemRefType {
    Simple,
    Reference;

    public static ItemRefType valueOf(int id) {
        return values()[id];
    }
}