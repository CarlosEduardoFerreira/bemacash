package com.kaching123.tcr.model;

public enum PaymentTenderType {
	CASH, CREDIT_CARD;

    public static PaymentTenderType valueOf(int id) {
        return values()[id];
    }
}
