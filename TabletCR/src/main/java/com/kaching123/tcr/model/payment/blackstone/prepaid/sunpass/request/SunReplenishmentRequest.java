package com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request;

import com.kaching123.tcr.model.payment.blackstone.prepaid.PaymentRequest;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SunReplenishmentRequest extends PaymentRequest {

    public String mID;
    public String tID;
    public String password;
    public String cashier;
    public long transactionId;
    public String accountNumber;
    public BigDecimal amount;
    public double feeAmount;
    public String purchaseId;
    public String transactionMode;

    @Override
    public void setOrderId(long orderId) {
        transactionId = orderId;
    }

    @Override
    public long getOrderId() {
        return transactionId;
    }

    @Override
    public String toString() {
        return "SunReplenishmentRequest{" +
                "mID='" + mID + '\'' +
                ", tID='" + tID + '\'' +
                ", password='" + password + '\'' +
                ", cashier='" + cashier + '\'' +
                ", transactionId=" + transactionId +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", feeAmount=" + feeAmount +
                ", purchaseId='" + purchaseId + '\'' +
                ", transactionMode='" + transactionMode + '\'' +
                '}';
    }
}