package com.kaching123.tcr.model.payment.blackstone.payment;

import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.WebAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 * ResponseBase class
 */
public abstract class ResponseBase implements Parcelable {

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_MSG)
    protected List<String> msg;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_RESPONSECODE)
    protected TransactionStatusCode responseCode;

    public TransactionStatusCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(TransactionStatusCode responseCode) {
        this.responseCode = responseCode;
    }

    public List<String> getMsg() {
        return msg;
    }

    public void setMsg(ArrayList<String> msg) {
        this.msg = msg;
    }

    public ResponseBase( List<String> msg, TransactionStatusCode responseCode) {
        this.msg = msg;
        this.responseCode = responseCode;
    }

    public ResponseBase() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nmsg : ").append(msg).append("\nresponseCode : ").append(responseCode);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        if (responseCode != null) {
            sb.append("The payment gateway responded with a message : ")
                    .append(getResponseCode().getDescription())
                    .append("\n");
        }
        if (msg != null && msg.size() > 0) {
            sb.append("Additional error information :\n");
            for (String s : msg) {
                sb.append(s).append("\n");
            }
        }
        return sb.toString();
    }
}
