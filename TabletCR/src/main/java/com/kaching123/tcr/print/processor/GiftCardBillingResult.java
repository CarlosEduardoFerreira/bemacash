package com.kaching123.tcr.print.processor;

import com.kaching123.tcr.model.SaleOrderItemViewModel;

import java.io.Serializable;

/**
 * Created by Teli on 2016/7/14.
 */
public class GiftCardBillingResult implements Serializable {
    public String msg;
    public SaleOrderItemViewModel model;
    public String balance;

    public GiftCardBillingResult(String result, SaleOrderItemViewModel model, String balance) {
        this.msg = result;
        this.model = model;
        this.balance = balance;
    }
}
