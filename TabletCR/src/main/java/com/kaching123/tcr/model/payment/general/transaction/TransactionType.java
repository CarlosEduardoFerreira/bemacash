package com.kaching123.tcr.model.payment.general.transaction;

import com.kaching123.tcr.commands.payment.PaymentGateway;

/**
 * credit card TransactionTypes
 * @author Ivan v. Rikhmayer
 */
public enum TransactionType {

    UNKNOWN("Unknown", 0),
    CREDIT("Credit transaction", 1),
    CREDIT_WITH_TOKEN("Credit with token transaction", 2),
    PRE_AUTHORIZATION("Credit preauthorization request", 3),
    PRE_AUTHORIZATION_COMPLETION("Credit preauthorization completion", 4),
    SALEDEBIT("Debit Card Sale transaction", 5),
    CASH("Payed with cash", 6),
    PREPAID_CARD("Payed with a prepaid card", 7),
    CREDIT_CHECK("Payed with a check", 8),
    OFFLINE_CREDIT("Offline credit transaction", 9),
    CHECK("Payed with a customer check", 10),
    PAX("Payed via PAX - credit terminal", 11),
    PAX_DEBIT("Payed via PAX - debit terminal", 12),
    PAX_EBT_FOODSTAMP("Payed via PAX - ebtFoodstamp terminal", 13),
    PAX_EBT_CASH("Payed via PAX - ebtFoodstamp terminal", 14),
    PAX_GIFT_CARD("Payed via PAX - giftCard terminal", 15),;


    private String name;
    private int intvalue;

    TransactionType(String name, int intvalue) {
        this.name = name;
        this.intvalue = intvalue;
    }

    @Override
    public String toString() {
        return name;
    }

    public int value() {
        return intvalue;
    }

    public static TransactionType valueOf(int temp) {
        return temp >= 0 && temp < TransactionType.values().length ? TransactionType.values()[temp] : UNKNOWN;
    }

    public static TransactionType valueOf(PaymentGateway temp) {
        switch (temp) {
            case BLACKSTONE: return CREDIT;
            case CASH: return CASH;
            case PAYPAL: return CREDIT;
            case OFFLINE_CREDIT: return OFFLINE_CREDIT;
            case CHECK: return CHECK;
            case PAX: return PAX;
            case PAX_DEBIT: return PAX_DEBIT;
            case PAX_EBT_FOODSTAMP: return PAX_EBT_FOODSTAMP;
            case PAX_EBT_CASH: return PAX_EBT_CASH;
            case PAX_GIFT_CARD: return PAX_GIFT_CARD;
        }
        return UNKNOWN;
    }
}
