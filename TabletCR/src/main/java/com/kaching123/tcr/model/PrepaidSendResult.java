package com.kaching123.tcr.model;

import android.content.Intent;

import com.kaching123.tcr.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by Teli on 2016/3/14.
 */
public class PrepaidSendResult {
    public String action;
    public String error;
    public String errorMsg;
    public String transactionId;
    public String itemName;
    public String itemDetails;
    public BigDecimal itemQty;
    public BigDecimal itemPrice;
    public boolean itemTaxable;
    public BigDecimal taxAmount;
    protected static final String ARG_ACTION = "ACTION";
    protected static final String ARG_ERROR = "ERROR";
    protected static final String ARG_ERRORMSG = "ERRORMSG";
    protected static final String ARG_TRANSACTIONID = "TRANSACTIONID";
    protected static final String ARG_ITEMNAME = "ITEMNAME";
    protected static final String ARG_ITEMDETAILS = "ITEMDETAILS";
    protected static final String ARG_ITEMQTY = "ITEMQTY";
    public static final String ARG_ITEMPRICE = "ITEMPRICE";
    protected static final String ARG_ITEMTAXABLE = "ITEMTAXABLE";
    protected static final String ARG_TAXAMOUNT = "ARG_TAXAMOUNT";

    public PrepaidSendResult(Intent data) {
        action = data.getStringExtra(ARG_ACTION);
        error = data.getStringExtra(ARG_ERROR);
        errorMsg = data.getStringExtra(ARG_ERRORMSG);
        transactionId = data.getStringExtra(ARG_TRANSACTIONID);
        itemName = data.getStringExtra(ARG_ITEMNAME);
        itemDetails = data.getStringExtra(ARG_ITEMDETAILS);
        itemQty = new BigDecimal(data.getIntExtra(ARG_ITEMQTY, 1));
        itemPrice = new BigDecimal(data.getStringExtra(ARG_ITEMPRICE));
        itemTaxable = Boolean.parseBoolean(data.getStringExtra(ARG_ITEMTAXABLE));
        taxAmount = new BigDecimal(data.getStringExtra(ARG_TAXAMOUNT) == null ? "0" : data.getStringExtra(ARG_TAXAMOUNT));
    }

    public void print() {
        Logger.d("Prepaid item = " + action + " " + error + " " + errorMsg + " " + transactionId + " " + itemName + " " + itemDetails + " " + itemQty + " " +
                itemPrice + " " + itemTaxable +" "+taxAmount);
    }
}
