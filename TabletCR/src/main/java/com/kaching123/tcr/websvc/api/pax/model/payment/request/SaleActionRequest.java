package com.kaching123.tcr.websvc.api.pax.model.payment.request;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold up data common for all requests
 */
public class SaleActionRequest implements Parcelable {

    @Expose
    @SerializedName(PAX_API.Command.PARAM_MESSAGE)
    protected String message;

    @Expose
    @SerializedName(PAX_API.SaleCommand.Request.PARAM_PAYMENT_TRANSACTION)
    protected int paymentTransaction;

    @Expose
    @SerializedName(PAX_API.SaleCommand.Request.PARAM_AMOUNT)
    protected String amount;


    @Expose
    @SerializedName(PAX_API.SaleCommand.Request.TRANSACTION_ID)
    protected String transactionId;

    public SaleActionRequest(int paymentTransaction, String amount, String transactionId) {
        this.message = PAX_API.SaleCommand.Request.ACTION;
        this.paymentTransaction = paymentTransaction;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public SaleActionRequest(String message, int paymentTransaction, String amount, String transactionId) {
        this.message = message;
        this.paymentTransaction = paymentTransaction;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public SaleActionRequest() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeInt(paymentTransaction);
        dest.writeString(amount);
        dest.writeString(transactionId);
    }

    public static Creator<SaleActionRequest> CREATOR = new Creator<SaleActionRequest>() {

        @Override
        public SaleActionRequest createFromParcel(Parcel source) {
            SaleActionRequest item = new SaleActionRequest(source.readString(), source.readInt(), source.readString(), source.readString());
            return item;
        }

        @Override
        public SaleActionRequest[] newArray(int size) {
            return new SaleActionRequest[size];
        }
    };

    @Override
    public String toString() {
        return "SaleActionRequest{" +
                "message='" + message + '\'' +
                ", paymentTransaction=" + paymentTransaction +
                ", amount='" + amount + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
