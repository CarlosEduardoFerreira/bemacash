package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.RestCommand.PlainTextResponse;

public class GetPrepaidOrderIdResponse extends PlainTextResponse {

    public GetPrepaidOrderIdResponse(String status, String message, String entity) {
        super(status, message, entity);
    }

    public Long getOrderId() {
        try {
            return entity == null ? null : Long.parseLong(entity);
        } catch (Exception e) {
            Logger.e("GetPrepaidOrderIdResponse error", e);
        }
        return null;
    }

}
