package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SettlementResponse implements Serializable {

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_RESPONSE)
    private int response;

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_DETAILS)
    private Settlement details;

    public int getResponse() {
        return response;
    }

    public Settlement getDetails() {
        return details;
    }
}
