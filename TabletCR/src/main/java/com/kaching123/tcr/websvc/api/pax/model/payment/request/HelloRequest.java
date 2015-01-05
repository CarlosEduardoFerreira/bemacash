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
public class HelloRequest implements Parcelable {

    @Expose
    @SerializedName(PAX_API.Command.PARAM_MESSAGE)
    protected String message;

    @Expose
    @SerializedName(PAX_API.DisplayCommand.PARAM_DISPLAY)
    protected String display;

    @Expose
    @SerializedName(PAX_API.DisplayCommand.PARAM_USE_INTERNAL_PRINTER)
    protected int printer;

    public HelloRequest() {
        this.message = PAX_API.DisplayCommand.ACTION;
        this.printer = PAX_API.DisplayCommand.ARG_IGNORE_PRINTER;
    }

    public HelloRequest setDisplay(String display){
        this.display = display;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static Creator<HelloRequest> CREATOR = new Creator<HelloRequest>() {

        @Override
        public HelloRequest createFromParcel(Parcel source) {
            HelloRequest user = new HelloRequest();
            return user;
        }

        @Override
        public HelloRequest[] newArray(int size) {
            return new HelloRequest[size];
        }
    };
}
