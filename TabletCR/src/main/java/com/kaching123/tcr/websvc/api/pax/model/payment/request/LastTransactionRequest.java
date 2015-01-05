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
public class LastTransactionRequest implements Parcelable {

    @Expose
    @SerializedName(PAX_API.Command.PARAM_MESSAGE)
    protected String message;

    public LastTransactionRequest() {
        this.message = PAX_API.LastTransactionCommand.Request.ACTION;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static Creator<LastTransactionRequest> CREATOR = new Creator<LastTransactionRequest>() {

        @Override
        public LastTransactionRequest createFromParcel(Parcel source) {
            LastTransactionRequest user = new LastTransactionRequest();
            return user;
        }

        @Override
        public LastTransactionRequest[] newArray(int size) {
            return new LastTransactionRequest[size];
        }
    };
}
