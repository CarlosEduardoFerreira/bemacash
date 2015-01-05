package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class AddTipsResponse implements Serializable {

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_RESPONSE)
    private int response;

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_DETAILS)
    private Sale details;

    public AddTipsResponse(int response, Sale details) {
        this.response = response;
        this.details = details;
    }

    public int getResponse() {
        return response;
    }

    public Sale getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "AddTipsResponse{" +
                "response=" + response +
                ", details=" + details +
                '}';
    }
}
