package com.kaching123.tcr.model.payment.blackstone.payment.response;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.model.payment.blackstone.payment.PaymentPlanInfo;
import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.websvc.WebAPI;

import java.util.ArrayList;

public class PreauthResponse extends ResponseBase {

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_AVS)
    private String avs;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_CV)
    private String cv;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_MSOFT_CODE)
    private String msoft_code;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_PAYMENTPLANINFO)
    private PaymentPlanInfo paymentPlanInfo;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_PHARD_CODE)
    private String phard_code;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_SERVICEREFERENCENUMBER)
    private String serviceReferenceNumber;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_VERBIAGE)
    private String verbiage;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_AUTHORIZATIONNUMBER)
    private String authorizationNumber;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_CARDTYPE)
    private String cardType;

    public PreauthResponse(String avs,
                           String cv,
                           ArrayList<String> msg,
                           String msoft_code,
                           PaymentPlanInfo paymentPlanInfo,
                           String phard_code,
                           TransactionStatusCode responseCode,
                           String serviceReferenceNumber,
                           String verbiage,
                           String authorizationNumber,
                           String cardType) {
        super(msg, responseCode);
        this.avs = avs;
        this.cv = cv;
        this.msoft_code = msoft_code;
        this.paymentPlanInfo = paymentPlanInfo;
        this.phard_code = phard_code;
        this.responseCode = responseCode;
        this.serviceReferenceNumber = serviceReferenceNumber;
        this.verbiage = verbiage;
        this.authorizationNumber = authorizationNumber;
        this.cardType = cardType;
    }

    public PreauthResponse() {
    }

    public String getAvs() {
        return avs;
    }

    public void setAvs(String avs) {
        this.avs = avs;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getMsoft_code() {
        return msoft_code;
    }

    public void setMsoft_code(String msoft_code) {
        this.msoft_code = msoft_code;
    }

    public PaymentPlanInfo getPaymentPlanInfo() {
        return paymentPlanInfo;
    }

    public void setPaymentPlanInfo(PaymentPlanInfo paymentPlanInfo) {
        this.paymentPlanInfo = paymentPlanInfo;
    }

    public String getPhard_code() {
        return phard_code;
    }

    public void setPhard_code(String phard_code) {
        this.phard_code = phard_code;
    }

    public String getServiceReferenceNumber() {
        return serviceReferenceNumber;
    }

    public void setServiceReferenceNumber(String serviceReferenceNumber) {
        this.serviceReferenceNumber = serviceReferenceNumber;
    }

    public String getVerbiage() {
        return verbiage;
    }

    public void setVerbiage(String verbiage) {
        this.verbiage = verbiage;
    }

    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    public void setAuthorizationNumber(String authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("avs : ").append(avs)
                .append("\ncv : ").append(cv)
                .append("\nmsg : ").append(msg == null ? "" : msg)
                .append("\nmsoft_code : ").append(msoft_code)
                .append("\n\npaymentPlanInfo : ").append(paymentPlanInfo == null ? "" : paymentPlanInfo)
                .append("\n\nphard_code : ").append(phard_code)
                .append("\nresponseCode : ").append(responseCode == null ? "" : responseCode)
                .append("\nserviceReferenceNumber : ").append(serviceReferenceNumber)
                .append("\nverbiage : ").append(verbiage)
                .append("\nauthorizationNumber : ").append(authorizationNumber)
                .append("\ncardType : ").append(cardType == null ? "" : cardType);
        return sb.toString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avs);
        dest.writeString(cv);
        dest.writeList(msg);
        dest.writeString(msoft_code);
        dest.writeParcelable(paymentPlanInfo, flags);
        dest.writeString(phard_code);
        dest.writeInt(responseCode.getCode());
        dest.writeString(serviceReferenceNumber);
        dest.writeString(verbiage);
        dest.writeString(authorizationNumber);
        dest.writeString(cardType);
    }

    public static Creator<PreauthResponse> CREATOR = new Creator<PreauthResponse>() {

        @Override
        public PreauthResponse createFromParcel(Parcel source) {
            String avs = source.readString();
            String cv = source.readString();
            ArrayList<String> msg = new ArrayList<String>();
            source.readList(msg, ArrayList.class.getClassLoader());
            String msoft_code = source.readString();
            PaymentPlanInfo paymentPlanInfo = source.readParcelable(PaymentPlanInfo.class.getClassLoader());
            String phard_code = source.readString();
            TransactionStatusCode responseCode = TransactionStatusCode.valueOf(source.readInt());
            String serviceReferenceNumber = source.readString();
            String verbiage = source.readString();
            String authorizationNumber = source.readString();
            String cardType = source.readString();
            return new PreauthResponse(avs,
                                    cv,
                                    msg,
                                    msoft_code,
                                    paymentPlanInfo,
                                    phard_code,
                                    responseCode,
                                    serviceReferenceNumber,
                                    verbiage,
                                    authorizationNumber,
                                    cardType);
        }

        @Override
        public PreauthResponse[] newArray(int size) {
            return new PreauthResponse[size];
        }
    };
}
