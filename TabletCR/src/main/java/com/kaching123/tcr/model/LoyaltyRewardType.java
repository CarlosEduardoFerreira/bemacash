package com.kaching123.tcr.model;

import com.kaching123.tcr.R;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public enum LoyaltyRewardType {
    DISCOUNT(R.string.loyalty_reward_type_discount),
    GIFT_CARD(R.string.loyalty_reward_type_gift_card),
    ITEM(R.string.loyalty_reward_type_item);

    private int label;

    LoyaltyRewardType(int label) {
        this.label = label;
    }

    public int getLabel() {
        return label;
    }

    public static LoyaltyRewardType valueOf(int id){
        return values()[id];
    }
}
