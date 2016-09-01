package com.kaching123.tcr.model.payment;

/**
 * Created by vkompaniets on 01.09.2016.
 */
public enum ModifierGroupCondition {
    EQUAL("="),
    LESS_EQUAL("<=");

    ModifierGroupCondition(String sign) {
        this.sign = sign;
    }

    private String sign;

    @Override
    public String toString() {
        return sign;
    }

    public static ModifierGroupCondition valueOf(int order){
        return ModifierGroupCondition.values()[order];
    }
}
