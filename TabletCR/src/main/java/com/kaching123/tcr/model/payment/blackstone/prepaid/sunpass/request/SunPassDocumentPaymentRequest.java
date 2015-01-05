package com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request;

import com.kaching123.tcr.websvc.api.prepaid.VectorDocument;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SunPassDocumentPaymentRequest extends SunReplenishmentRequest {


    public String licensePateleNumber;
    public VectorDocument paidDocuments;

    @Override
    public String toString() {
        return "SunPassDocumentPaymentRequest{" +
                "mID='" + mID + '\'' +
                ", tID='" + tID + '\'' +
                ", password='" + password + '\'' +
                ", cashier='" + cashier + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", licensePateleNumber='" + licensePateleNumber + '\'' +
                ", amount=" + amount +
                ", feeAmount=" + feeAmount +
                ", purchaseId='" + purchaseId + '\'' +
                ", paidDocuments='" + paidDocuments + '\'' +
                ", transactionMode='" + transactionMode + '\'' +
                '}';
    }
}