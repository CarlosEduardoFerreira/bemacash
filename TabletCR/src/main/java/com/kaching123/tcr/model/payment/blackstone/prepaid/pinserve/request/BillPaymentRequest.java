package com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request;

import com.kaching123.tcr.model.payment.blackstone.prepaid.PaymentRequest;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class BillPaymentRequest  extends PaymentRequest {

    public String mID;
    public String tID;
    public String password;
    public String cashier;
    public String vendorId;
    public String accountNumber;
    public String altAccountNumber;
    public String additAccountNumber1;
    public String additAccountNumber2;
    public BigDecimal paymentAmount;
    public double feeAmount;
    public String customerFirstName;
    public String customerLastName;
    public String paymentType;
    public String senderFirstName;
    public String senderLastName;
    public long transactionId;
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
        return "BillPaymentRequest{" +
                "mID='" + mID + '\'' +
                ", tID='" + tID + '\'' +
                ", password='" + password + '\'' +
                ", cashier='" + cashier + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", altAccountNumber='" + altAccountNumber + '\'' +
                ", additAccountNumber1='" + additAccountNumber1 + '\'' +
                ", additAccountNumber2='" + additAccountNumber2 + '\'' +
                ", paymentAmount=" + paymentAmount +
                ", feeAmount=" + feeAmount +
                ", customerFirstName='" + customerFirstName + '\'' +
                ", customerLastName='" + customerLastName + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", senderFirstName='" + senderFirstName + '\'' +
                ", senderLastName='" + senderLastName + '\'' +
                ", transactionId=" + transactionId +
                ", transactionMode='" + transactionMode + '\'' +
                '}';
    }
}
