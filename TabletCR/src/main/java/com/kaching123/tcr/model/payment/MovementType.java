package com.kaching123.tcr.model.payment;

/**
 * Created by vkompaniets on 04.06.2014.
 */
public enum MovementType {
    DROP, PAYOUT;

    public static MovementType valueOf(int id){
        return values()[id];
    }
}
