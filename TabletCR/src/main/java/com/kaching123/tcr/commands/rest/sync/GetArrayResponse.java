package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.util.JdbcJSONArray;

/**
 * Created by gdubina on 19/03/14.
 */
public class GetArrayResponse extends RestCommand.JsonArrayResponse {

    public GetArrayResponse(String status, String message, JdbcJSONArray entity) {
        super(status, message, entity);
    }

}
