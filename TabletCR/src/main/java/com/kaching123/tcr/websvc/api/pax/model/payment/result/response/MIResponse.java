package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class MIResponse implements Serializable {

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_RESPONSE)
    protected int response;

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_DETAILS)
    protected MerchantDetailsResponse details;

    public MIResponse(int response, String details) {

        this.response = response;
    }

    public MIResponse() {
    }

    public int getResponse() {
        return response;
    }



    public void setResponse(int response) {
        this.response = response;
    }

    public MerchantDetailsResponse getDetails() {
        return details;
    }
}
