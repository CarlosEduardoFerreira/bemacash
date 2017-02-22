package com.kaching123.tcr.commands.rest.sync;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.commands.rest.RestCommand.JsonResponse;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by gdubina on 19/03/14.
 */
public class UploadResponse extends JsonResponse {

    public UploadResponse(String status, String message, JdbcJSONObject entity) {
        super(status, message, entity);
    }

    public long optFailedId(long fallback) {
        return entity == null ? fallback : entity.optLong("error_id", fallback);
    }

    @Override
    public String toString() {
        return "\t status:\t" + status +
                ";\t entity:\t" + (entity == null ? "" : entity.toString()) +
                ";\t message:\t" + message;
    }

}