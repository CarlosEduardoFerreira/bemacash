package com.kaching123.tcr.model.payment.blackstone.prepaid;

/**
 * Created by vkompaniets on 19.05.2014.
 */
public class PrePaidInfo implements IPrePaidInfo {

    protected boolean fieldsEmty;

    protected boolean transactionFee;

    public boolean isFieldsEmty() {
        return fieldsEmty;
    }
    public boolean hasTransactionFee() {
        return transactionFee;
    }

}
