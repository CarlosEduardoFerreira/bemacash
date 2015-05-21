package com.kaching123.tcr.websvc.api.pax.model.payment.result.handler;

import com.google.gson.stream.JsonReader;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SerialResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SerialResult;
import com.mayer.framework.web.Logger.Logger;
import com.mayer.framework.web.model.rest.RESTResultHandler;
import com.mayer.framework.web.utils.GSON;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SerialResultHandler extends RESTResultHandler<SerialResult> {


    public SerialResultHandler(SerialResult webServiceResult) {
        super(webServiceResult);
    }

    @Override
    public boolean handleJSON(JsonReader json) {
        Logger.d("About to handle JSON HELLOResultHandler");
        try {
            SerialResponse response = GSON.getInstance().getGson().fromJson(json, SerialResponse.class);
            Logger.d("json,", response.getResponse());
            getWebServiceResult().setData(response);
        } catch (com.google.gson.JsonIOException e) {
            Logger.e("JSON parsing problem", e);
            return false;
        } catch (com.google.gson.JsonSyntaxException e) {
            Logger.e("JSON parsing problem", e);
            return false;
        }
        return false;
    }
}
