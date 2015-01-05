package com.kaching123.tcr.websvc.handler.blackstone.pinserve;

import com.google.gson.stream.JsonReader;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.response.DoSettlementResponse;
import com.kaching123.tcr.util.GSON;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.DoSettlementResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;
import com.mayer.framework.web.utils.StringUtils;

import java.io.PushbackInputStream;

/**
 * Created by pkabakov on 23.05.2014.
 */
public class DoSettlementResultHandler extends RESTResultHandler<DoSettlementResult> {

    private String stringResponse;

    public DoSettlementResultHandler(DoSettlementResult webServiceResult) {
        super(webServiceResult);
    }

    @Override
    protected void preHandle(PushbackInputStream pushbackStream) {
        stringResponse = StringUtils.getString(pushbackStream, PUSHSTREAM_BUFFER_SIZE);
    }

    @Override
    public boolean handleJSON(JsonReader json) {
        Logger.d("About to handle JSON DoSettlementResultHandler");
        try {
            DoSettlementResponse response = GSON.getInstance().getGson().fromJson(json, DoSettlementResponse.class);

            if (response != null && TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY != response.getResponseCode())
                Logger.e("DoSettlementResultHandler.handleJSON(): error response: " + stringResponse);
            stringResponse = null;

            Logger.d("DoSettlementResultHandler.handleJSON(): response: " + response.toDebugString());
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
