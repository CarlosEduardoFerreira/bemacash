package com.kaching123.tcr.websvc.api.pax.model.payment.request;

import android.os.Parcel;

import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold up data common for all requests
 */
public class MIRequest extends BaseMIRequest {

    public MIRequest() {
        super();
    }

    @Override
    protected String getMessage() {
        return PAX_API.MIDownloadCommand.ACTION_EASY;
    }

    public static Creator<MIRequest> CREATOR = new Creator<MIRequest>() {

        @Override
        public MIRequest createFromParcel(Parcel source) {
            return new MIRequest();
        }

        @Override
        public MIRequest[] newArray(int size) {
            return new MIRequest[size];
        }
    };
}
