package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.RestCommand.PlainTextResponse;

import java.util.Date;

public class GetCurrentTimestampResponse extends PlainTextResponse {

    public GetCurrentTimestampResponse(String status, String message, String entity) {
        super(status, message, entity);
    }

    public Long getCurrentTimestamp() {
        try {
            Date date = entity == null ? null : Sync2Util.formatMillisec(entity);
            return date == null ? null : date.getTime();
        } catch (Exception e) {
            Logger.e("GetCurrentTimestampResponse error", e);
        }
        return null;
    }

}
