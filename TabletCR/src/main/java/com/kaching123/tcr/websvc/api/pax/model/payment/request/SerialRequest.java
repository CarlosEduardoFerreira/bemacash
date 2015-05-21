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
public class SerialRequest implements Parcelable {

    @Expose
    @SerializedName(PAX_API.Command.PARAM_MESSAGE)
    protected String message;

    public SerialRequest() {
        this.message = PAX_API.SerialCommand.SERIAL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static Creator<SerialRequest> CREATOR = new Creator<SerialRequest>() {

        @Override
        public SerialRequest createFromParcel(Parcel source) {
            SerialRequest user = new SerialRequest();
            return user;
        }

        @Override
        public SerialRequest[] newArray(int size) {
            return new SerialRequest[size];
        }
    };
}
