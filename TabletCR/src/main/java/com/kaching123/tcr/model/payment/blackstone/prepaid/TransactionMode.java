package com.kaching123.tcr.model.payment.blackstone.prepaid;

import com.kaching123.tcr.websvc.api.prepaid.Broker;

/**
 * Created by pkabakov on 03.07.2014.
 */
public class TransactionMode {

    public static boolean isTestMode(String transactionMode) {
        return Broker.TRANSACTION_MODE_TEST.equals(transactionMode);
    }

    public static String getTransactionMode(boolean isTestMode) {
        return isTestMode ? Broker.TRANSACTION_MODE_TEST : Broker.TRANSACTION_MODE_LIVE;
    }

}
