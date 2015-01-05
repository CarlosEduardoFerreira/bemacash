package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class Sale implements Serializable {

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_AVS)
    private String avs;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_SERVICEREFERENCENUMBER)
    private String transactionNumber;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_MSOFT_CODE)
    private String msoft;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_PHARD_CODE)
    private String phard;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_CV)
    private String cv;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_VERBIAGE)
    private String verbiage;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_AUTHORIZATIONNUMBER)
    private String authNumber;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_CARDTYPE)
    private String type;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_LASTFOUR)
    private String last4;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_BALANCE)
    private String balance;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_RESPONSECODE)
    private int responseCode;

    @Expose
    @SerializedName(WebAPI.PAX_API.SaleCommand.SaleObject.PARAM_MSG)
    private String[] message;

    public String getAvs() {
        return avs;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public String getMsoft() {
        return msoft;
    }

    public String getPhard() {
        return phard;
    }

    public String getCv() {
        return cv;
    }

    public String getVerbiage() {
        return verbiage;
    }

    public String getAuthNumber() {
        return authNumber;
    }

    public String getType() {
        return type;
    }

    public String getLast4() {
        return last4;
    }

    public String getBalance() {
        return balance;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String[] getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "avs='" + avs + '\'' +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", msoft='" + msoft + '\'' +
                ", phard='" + phard + '\'' +
                ", cv='" + cv + '\'' +
                ", verbiage='" + verbiage + '\'' +
                ", authNumber='" + authNumber + '\'' +
                ", type='" + type + '\'' +
                ", last4='" + last4 + '\'' +
                ", balance='" + balance + '\'' +
                ", responseCode=" + responseCode +
                ", message=" + Arrays.toString(message) +
                '}';
    }
}
