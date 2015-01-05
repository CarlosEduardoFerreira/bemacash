package com.kaching123.tcr.model.payment.blackstone.prepaid;

/**
 * Created by pkabakov on 16.04.2014.
 */
public abstract class PaymentRequest extends RequestBase {

    public abstract void setOrderId(long orderId);
    public abstract long getOrderId();

}
