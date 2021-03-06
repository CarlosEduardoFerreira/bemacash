package com.kaching123.tcr.model;

import com.kaching123.tcr.Logger;

import java.io.Serializable;

/**
 * Created by Teli on 2016/3/14.
 */
public class PrepaidReleaseResult implements Serializable {
    public String action;
    public String error;
    public String errorMSG;
    public String receipt;
    public SaleOrderItemViewModel model;

    public PrepaidReleaseResult(String action, String error, String errorMSG, String receipt, SaleOrderItemViewModel model) {
        this.action = action;
        this.error = error;
        this.errorMSG = errorMSG;
        this.receipt = receipt;
        this.model = model;
    }

    public void print() {
        Logger.d("Prepaid item = " + action + " " + error + " " + errorMSG + " " + receipt);
    }
}