package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI.PAX_API.MIDownloadCommand;

import java.io.Serializable;

public class MerchantDetailsResponse implements Serializable {

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_RESPONSECODE)
    protected int responseCode;

    @Expose
    @SerializedName(MIDownloadCommand.PARAM_MERCHANTDETAILS)
    protected MerchantDetails details;

    public MerchantDetailsResponse(int responseCode, String details) {

        this.responseCode = responseCode;
    }

    public MerchantDetailsResponse() {
    }

    public int getResponse() {
        return responseCode;
    }



    public void setResponse(int response) {
        this.responseCode = response;
    }

    public MerchantDetails getDetails() {
        return details;
    }
}
