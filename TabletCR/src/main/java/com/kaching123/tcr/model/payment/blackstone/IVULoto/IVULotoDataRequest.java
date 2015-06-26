package com.kaching123.tcr.model.payment.blackstone.IVULoto;

import com.kaching123.tcr.model.payment.blackstone.prepaid.PaymentRequest;

import java.math.BigDecimal;

/**
 * Created by teli on 6/26/2015.
 */
public class IVULotoDataRequest  extends PaymentRequest {
    public String mID;
    public String tID;
    public String password;
    public BigDecimal paymentAmount;
    public String paymentType;
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
                ", paymentAmount='" + paymentAmount + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", transactionId=" + transactionId +
                ", transactionMode='" + transactionMode + '\'' +
                '}';
    }
}
