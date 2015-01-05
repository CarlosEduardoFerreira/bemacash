package com.kaching123.tcr.websvc.api.pax.model.payment.request;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API.TipsCommand.Request;

/**
 * @author Ivan v. Rikhmayer
 */
public class AddTipsRequest implements Parcelable {

    @Expose
    @SerializedName(PAX_API.Command.PARAM_MESSAGE)
    protected String message;

    @Expose
    @SerializedName(Request.PARAM_TRANSACTION_NUMBER)
    protected String transactionNumber;

    @Expose
    @SerializedName(Request.PARAM_AMOUNT)
    protected String amount;

    public AddTipsRequest(String transactionNumber, String amount) {
        this.message = PAX_API.TipsCommand.Request.ACTION;
        this.transactionNumber = transactionNumber;
        this.amount = amount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transactionNumber);
        dest.writeString(amount);
    }

    public static Creator<AddTipsRequest> CREATOR = new Creator<AddTipsRequest>() {

        @Override
        public AddTipsRequest createFromParcel(Parcel source) {
            return new AddTipsRequest(source.readString(), source.readString());
        }

        @Override
        public AddTipsRequest[] newArray(int size) {
            return new AddTipsRequest[size];
        }
    };

    @Override
    public String toString() {
        return "AddTipsRequest{" +
                "message='" + message + '\'' +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}
