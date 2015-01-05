package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API.SettlementCommand.SettlementObject;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SettlementItem implements Serializable {

    @Expose
    @SerializedName(SettlementObject.PARAM_SETTLED)
    private boolean settled;

    @Expose
    @SerializedName(SettlementObject.PARAM_BATCHNUMBER)
    private int batchNumber;

    @Expose
    @SerializedName(SettlementObject.PARAM_AMOUNT)
    private double amount;

    @Expose
    @SerializedName(SettlementObject.PARAM_REFERENCENUMBER)
    private String referenceNumber;

    @Expose
    @SerializedName(SettlementObject.PARAM_TRANSACTIONNUMBER)
    private String transactionNumber;

    @Expose
    @SerializedName(SettlementObject.PARAM_DATE)
    private String date;

    public boolean isSettled() {
        return settled;
    }

    public int getBatchNumber() {
        return batchNumber;
    }

    public double getAmount() {
        return amount;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public String getDate() {
        return date;
    }
}
