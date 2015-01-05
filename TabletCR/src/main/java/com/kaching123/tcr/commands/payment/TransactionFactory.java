package com.kaching123.tcr.commands.payment;

import java.util.UUID;

/**
 * @author Ivan v. Rikhmayer
 */
public abstract class TransactionFactory {

    public static String getTransactionUID() {
        return UUID.randomUUID().toString();
    }

}
