package com.kaching123.tcr.websvc.handler.blackstone.pinserve;

import com.google.gson.stream.JsonReader;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.payment.blackstone.payment.PaymentPlanInfo;
import com.kaching123.tcr.model.payment.blackstone.payment.response.PreauthResponse;
import com.kaching123.tcr.util.GSON;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.PreauthResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

public class PreauthResultHandler extends RESTResultHandler<PreauthResult> {

    private PaymentPlanInfo plan;

    public PreauthResultHandler(PreauthResult webServiceResult) {
        super(webServiceResult);
    }

    @Override
    public boolean handleJSON(JsonReader json) {
        Logger.d("About to handle JSON PreauthResultHandler");
        try {
            PreauthResponse response = GSON.getInstance().getGson().fromJson(json, PreauthResponse.class);
            Logger.d("PreauthResultHandler.handleJSON(): response: " + response.toDebugString());
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
