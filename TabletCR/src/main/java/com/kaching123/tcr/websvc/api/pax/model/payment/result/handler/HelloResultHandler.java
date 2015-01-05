package com.kaching123.tcr.websvc.api.pax.model.payment.result.handler;

import com.google.gson.stream.JsonReader;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.HelloResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.HelloResult;
import com.mayer.framework.web.Logger.Logger;
import com.mayer.framework.web.model.rest.RESTResultHandler;
import com.mayer.framework.web.utils.GSON;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class HelloResultHandler extends RESTResultHandler<HelloResult> {


    public HelloResultHandler(HelloResult webServiceResult) {
        super(webServiceResult);
    }

    @Override
    public boolean handleJSON(JsonReader json) {
        Logger.d("About to handle JSON HELLOResultHandler");
        try {
            HelloResponse response = GSON.getInstance().getGson().fromJson(json, HelloResponse.class);
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
