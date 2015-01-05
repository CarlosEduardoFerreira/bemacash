package com.kaching123.tcr.websvc.api.pax.model.payment.result.handler;

import com.google.gson.stream.JsonReader;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResult;
import com.mayer.framework.web.Logger.Logger;
import com.mayer.framework.web.model.rest.RESTResultHandler;
import com.mayer.framework.web.utils.GSON;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SaleActionResultHandler extends RESTResultHandler<SaleActionResult> {


    public SaleActionResultHandler(SaleActionResult webServiceResult) {
        super(webServiceResult);
    }

    @Override
    public boolean handleJSON(JsonReader json) {
        Logger.d("About to handle JSON SaleResultHandler");
        try {
            SaleActionResponse response = GSON.getInstance().getGson().fromJson(json, SaleActionResponse.class);
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
