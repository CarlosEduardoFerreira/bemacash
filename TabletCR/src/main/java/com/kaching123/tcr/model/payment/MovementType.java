package com.kaching123.tcr.model.payment;

/**
 * Created by vkompaniets on 04.06.2014.
 */
public enum MovementType {
    DROP(0), PAYOUT(1);
    private int value = 0;
    private MovementType(int value)
    {
        this.value = value;
    }
    public int getValue()
    {
        return value;
    }

    public static MovementType valueOf(int id){
        return values()[id];
    }
}
