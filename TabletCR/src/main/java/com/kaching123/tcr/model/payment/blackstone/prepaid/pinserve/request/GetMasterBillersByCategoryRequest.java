package com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request;

import com.kaching123.tcr.model.payment.blackstone.prepaid.RequestBase;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GetMasterBillersByCategoryRequest extends RequestBase {

    public String MID;
    public String TID;
    public String Password;
    public String Cashier;
    public String CaregoryId;
    public long transactionId;
    public BigDecimal amount;
    public String TransactionMode;

    @Override
    public String toString() {
        return "GetMasterBillersByCategoryRequest{" +
                "MID='" + MID + '\'' +
                ", TID='" + TID + '\'' +
                ", Password" +
                ", Cashier='" + Cashier + '\'' +
                ", CaregoryId='" + CaregoryId + '\'' +
                ", transactionId=" + transactionId +
                ", amount=" + amount +
                ", TransactionMode='" + TransactionMode + '\'' +
                '}';
    }
}
