package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by gdubina on 19/03/14.
 */
public class GetResponse extends RestCommand.JsonResponse {

    public static final String SINGLE_INT = "SINGLE_INT";

    public GetResponse(String status, String message, JdbcJSONObject entity) {
        super(status, message, entity);
    }

    public JdbcJSONArray getResponse(long id) {
        try {
            return entity.getJSONArray(String.valueOf(id));
        } catch (Exception e) {
            Logger.e("GetResponse.getResponse error", e);
            return null;
        }
    }

    /*public int getSingleInt() {
        try {
            return getResponse(SyncGetRequestBuilder.DEFAULT_QUERY_ID).getJSONObject(0).getInt(SINGLE_INT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }*/
}
