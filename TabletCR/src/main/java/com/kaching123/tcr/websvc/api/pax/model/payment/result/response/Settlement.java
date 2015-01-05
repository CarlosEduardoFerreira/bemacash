package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API.SettlementCommand.SettlementObject;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class Settlement implements Serializable {

    @Expose
    @SerializedName(SettlementObject.PARAM_AMOUNTSETTLED)
    private String amountSettled;

    @Expose
    @SerializedName(SettlementObject.PARAM_RESPONSECODE)
    private int responseCode;

    @Expose
    @SerializedName(SettlementObject.PARAM_TRANSACTIONCOUNT)
    private int transactionCount;

    @Expose
    @SerializedName(SettlementObject.PARAM_TRANSACTIONS)
    private SettlementItem[] transactions;

    @Expose
    @SerializedName(SettlementObject.PARAM_VERBIAGE)
    private String verbiage;

    public String getAmountSettled() {
        return amountSettled;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public SettlementItem[] getTransactions() {
        return transactions;
    }

    public String getVerbiage() {
        return verbiage;
    }
}
