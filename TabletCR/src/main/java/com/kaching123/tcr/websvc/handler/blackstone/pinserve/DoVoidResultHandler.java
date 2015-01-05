package com.kaching123.tcr.websvc.handler.blackstone.pinserve;

import com.google.gson.stream.JsonReader;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.payment.blackstone.payment.response.DoVoidResponse;
import com.kaching123.tcr.util.GSON;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.DoVoidResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to parse blackstone.pinserve results
 */
public class DoVoidResultHandler extends RESTResultHandler<DoVoidResult> {

    public DoVoidResultHandler(DoVoidResult webServiceResult) {
        super(webServiceResult);
    }

    @Override
    protected boolean handleJSON(JsonReader json) {
        Logger.d("About to handle JSON SaleResultHandler");
        try {
            DoVoidResponse response = GSON.getInstance().getGson().fromJson(json, DoVoidResponse.class);
            Logger.d("DoVoidResultHandler.handleJSON(): response: " + response.toDebugString());
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
