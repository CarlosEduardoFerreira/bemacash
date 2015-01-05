package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class Details implements Serializable {

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_SALE)
    private Sale sale;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_TRANSACTIONNUMBER)
    private String transactionNumber;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_TRANSACTION_AMOUNT)
    private String amount;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_CARDLASTDIGITS)
    private String digits;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_CUSTOMSTATUS)
    private int status;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_VERBIAGE)
    private String verbiage;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_FEES)
    private String fees;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_SALEAMOUNT)
    private String saleAmount;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.Response.PARAM_CASHBACKAMOUNT)
    private String cashBackAmount;


    public Sale getSale() {
        return sale;
    }
    public String getTransactionNumber() {
        return transactionNumber;
    }
    public String getAmount() {
        return amount;
    }
    public String getDigits() {
        return digits;
    }
    public int getStatus() {
        return status;
    }
    public String getVerbiage() {
        return verbiage;
    }
    public String getFees() {
        return fees;
    }
    public String getSaleAmount() {
        return saleAmount;
    }
    public String getCashBackAmount() {
        return cashBackAmount;
    }

    @Override
    public String toString() {
        return "Details{" +
                "sale=" + sale +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", amount='" + amount + '\'' +
                ", digits='" + digits + '\'' +
                ", status=" + status +
                ", verbiage='" + verbiage + '\'' +
                ", fees='" + fees + '\'' +
                ", saleAmount='" + saleAmount + '\'' +
                ", cashBackAmount='" + cashBackAmount + '\'' +
                '}';
    }
}
