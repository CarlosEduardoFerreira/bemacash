package com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request;

import com.kaching123.tcr.model.payment.blackstone.prepaid.RequestBase;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SunEntryRequest extends RequestBase implements Serializable {
    public String mID;
    public String tID;
    public String password;
    public String cashier;
    public String accountNumber;
    public long transactionId;
    public String transactionMode;

    @Override
    public String toString() {
        return "SunEntryRequest{" +
                "mID='" + mID + '\'' +
                ", tID='" + tID + '\'' +
                ", password" +
                ", cashier='" + cashier + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", transactionId=" + transactionId +
                ", transactionMode='" + transactionMode + '\'' +
                '}';
    }
}
