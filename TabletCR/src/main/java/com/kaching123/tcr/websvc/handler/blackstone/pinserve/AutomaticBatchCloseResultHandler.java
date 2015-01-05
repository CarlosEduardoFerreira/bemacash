package com.kaching123.tcr.websvc.handler.blackstone.pinserve;

import com.google.gson.stream.JsonReader;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.payment.blackstone.payment.response.AutomaticBatchCloseResponse;
import com.kaching123.tcr.util.GSON;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.AutomaticBatchCloseResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

/**
 * Created by pkabakov on 11.06.2014.
 */
public class AutomaticBatchCloseResultHandler extends RESTResultHandler<AutomaticBatchCloseResult> {

    public AutomaticBatchCloseResultHandler(AutomaticBatchCloseResult webServiceResult) {
        super(webServiceResult);
    }

    @Override
    public boolean handleJSON(JsonReader json) {
        Logger.d("About to handle JSON SetAutomaticBatchCloseResultHandler");
        try {
            AutomaticBatchCloseResponse response = GSON.getInstance().getGson().fromJson(json, AutomaticBatchCloseResponse.class);
            Logger.d("SetAutomaticBatchCloseResultHandler.handleJSON(): response: " + response.toDebugString());
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
