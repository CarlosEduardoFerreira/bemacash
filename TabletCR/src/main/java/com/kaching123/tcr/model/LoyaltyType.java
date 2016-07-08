package com.kaching123.tcr.model;

import com.kaching123.tcr.R;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public enum LoyaltyType {
    POINTS(R.string.loyalty_type_label_points),
    BIRTHDAY(R.string.loyalty_type_label_birthday);

    private int label;

    LoyaltyType(int label) {
        this.label = label;
    }

    public int getLabel() {
        return label;
    }

    public static LoyaltyType valueOf(int id){
        return values()[id];
    }
}
