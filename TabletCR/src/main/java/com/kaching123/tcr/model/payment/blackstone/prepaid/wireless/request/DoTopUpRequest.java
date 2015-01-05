package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request;

import com.kaching123.tcr.model.payment.blackstone.prepaid.PaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessType;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class DoTopUpRequest extends PaymentRequest {

    public String mID;
    public String tID;
    public String password;
    public String cashier;
    public String productMaincode;
    public BigDecimal topUpAmount;
    public String phoneNumber;
    public String countryCode;
    public long orderID;
    public int profileID;
    public String transactionMode;
    public WirelessType type;


    @Override
    public void setOrderId(long orderId) {
        this.orderID = orderId;
    }

    @Override
    public long getOrderId() {
        return orderID;
    }

    @Override
    public String toString() {
        return "DoTopUpRequest{" +
                "mID='" + mID + '\'' +
                ", tID='" + tID + '\'' +
                ", password='" + password + '\'' +
                ", cashier='" + cashier + '\'' +
                ", productMaincode='" + productMaincode + '\'' +
                ", topUpAmount=" + topUpAmount +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", orderID=" + orderID +
                ", profileID=" + profileID +
                ", transactionMode='" + transactionMode + '\'' +
                ", type=" + type +
                '}';
    }
}
