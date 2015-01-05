package com.kaching123.tcr.websvc.api.pax.model.payment.request;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API;

/**
 * Created by pkabakov on 25.06.2014.
 */
public abstract class BaseMIRequest implements Parcelable {

    @Expose
    @SerializedName(PAX_API.Command.PARAM_MESSAGE)
    protected String message;

    public BaseMIRequest() {
        this.message = getMessage();
    }

    protected abstract String getMessage();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

}
