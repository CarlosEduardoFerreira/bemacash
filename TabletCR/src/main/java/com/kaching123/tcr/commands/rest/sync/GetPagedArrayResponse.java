package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.util.JdbcJSONArray;

/**
 * Created by gdubina on 19/03/14.
 */
public class GetPagedArrayResponse extends GetArrayResponse {

    public final int rows;

    public GetPagedArrayResponse(String status, String message, JdbcJSONArray entity, int rows) {
        super(status, message, entity);
        this.rows = rows;
    }

}
