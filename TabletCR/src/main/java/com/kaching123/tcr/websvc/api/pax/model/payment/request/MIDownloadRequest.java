package com.kaching123.tcr.websvc.api.pax.model.payment.request;

import android.os.Parcel;

import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold up data common for all requests
 */
public class MIDownloadRequest extends BaseMIRequest {

    public MIDownloadRequest() {
        super();
    }

    @Override
    protected String getMessage() {
        return PAX_API.MIDownloadCommand.ACTION;
    }

    public static Creator<MIDownloadRequest> CREATOR = new Creator<MIDownloadRequest>() {

        @Override
        public MIDownloadRequest createFromParcel(Parcel source) {
            return new MIDownloadRequest();
        }

        @Override
        public MIDownloadRequest[] newArray(int size) {
            return new MIDownloadRequest[size];
        }
    };
}
