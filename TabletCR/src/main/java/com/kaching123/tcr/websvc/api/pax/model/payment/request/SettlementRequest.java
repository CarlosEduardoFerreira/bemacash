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
public class SettlementRequest implements Parcelable {

    @Expose
    @SerializedName(PAX_API.Command.PARAM_MESSAGE)
    protected String message;

    public SettlementRequest() {
        this.message = PAX_API.SettlementCommand.Request.ACTION;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static Creator<SettlementRequest> CREATOR = new Creator<SettlementRequest>() {

        @Override
        public SettlementRequest createFromParcel(Parcel source) {
            SettlementRequest user = new SettlementRequest();
            return user;
        }

        @Override
        public SettlementRequest[] newArray(int size) {
            return new SettlementRequest[size];
        }
    };
}
