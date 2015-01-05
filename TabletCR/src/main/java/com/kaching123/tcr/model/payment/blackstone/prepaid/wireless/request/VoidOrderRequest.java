package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request;

import com.kaching123.tcr.model.payment.blackstone.prepaid.RequestBase;

/**
 * Created by pkabakov on 11.04.2014.
 */
public class VoidOrderRequest extends RequestBase {
    public String mID;
    public String tID;
    public String password;
    public String cashier;
    public String transactionMode;
    public long orderID;

    @Override
    public String toString() {
        return "VoidOrderRequest{" +
                "mID='" + mID + '\'' +
                ", tID='" + tID + '\'' +
                ", password" +
                ", cashier='" + cashier + '\'' +
                ", transactionMode='" + transactionMode + '\'' +
                ", orderID=" + orderID +
                '}';
    }
}
