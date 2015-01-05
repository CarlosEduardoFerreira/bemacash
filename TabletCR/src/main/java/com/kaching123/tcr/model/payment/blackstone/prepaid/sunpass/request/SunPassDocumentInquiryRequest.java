package com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request;

import com.kaching123.tcr.model.payment.blackstone.prepaid.RequestBase;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SunPassDocumentInquiryRequest extends RequestBase implements Serializable {
    public String mID;
    public String tID;
    public String password;
    public String cashier;
    public String accountNumber;
    public String licensePlateNumber;
    public long transactionId;
    public String transactionMode;

    @Override
    public String toString() {
        return "SunPassDocumentInquiryRequest{" +
                "mID='" + mID + '\'' +
                ", tID='" + tID + '\'' +
                ", password" +
                ", cashier='" + cashier + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", licensePlateNumber ='"+licensePlateNumber + '\'' +
                ", transactionId=" + transactionId +
                ", transactionMode='" + transactionMode + '\'' +
                '}';
    }
}
