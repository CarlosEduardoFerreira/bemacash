package com.kaching123.tcr.commands.rest.sync.v1;

import com.kaching123.tcr.commands.rest.RestCommand.JsonResponse;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by gdubina on 19/03/14.
 */
public class UploadResponseV1 extends JsonResponse {

    public UploadResponseV1(String status, String message, JdbcJSONObject entity) {
        super(status, message, entity);
    }

    public long optFailedId(long fallback) {
        return entity == null ? fallback : entity.optLong("error_id", fallback);
    }
}
