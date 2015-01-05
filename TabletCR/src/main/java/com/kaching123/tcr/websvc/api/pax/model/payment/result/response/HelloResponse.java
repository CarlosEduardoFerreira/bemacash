package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.api.pax.api.WebAPI;

import java.io.Serializable;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class HelloResponse implements Serializable {

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_RESPONSE)
    protected int response;

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_DETAILS)
    protected String details;

    @Expose
    @SerializedName(WebAPI.PAX_API.Command.PARAM_VERSION)
    protected String version;

    public HelloResponse(int response, String details, String version) {

        this.response = response;
        this.details = details;
        this.version = version;
    }

    public HelloResponse() {
    }

    public int getResponse() {
        return response;
    }

    public String getDetails() {
        return details;
    }

    public String getVersion() {
        return version;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
