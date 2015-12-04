package com.kaching123.tcr.model;

import com.google.gson.GsonBuilder;
import com.kaching123.tcr.commands.rest.sync.GetResponse;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by idyuzheva on 27.11.2015.
 */
public class PlanOptionsResponse extends GetResponse {

    public PlanOptionsResponse(String status, String message, JdbcJSONObject entity) {
        super(status, message, entity);
    }

    public PlanOptions get() {
        return new GsonBuilder().create().fromJson(String.valueOf(getEntity()),
                PlanOptions.class);
    }
}