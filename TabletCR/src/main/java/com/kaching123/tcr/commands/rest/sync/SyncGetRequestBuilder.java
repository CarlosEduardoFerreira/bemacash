package com.kaching123.tcr.commands.rest.sync;

import android.text.TextUtils;

import com.kaching123.tcr.service.SyncCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by gdubina on 19/03/14.
 */
public class SyncGetRequestBuilder {

    public static JSONObject getRequest(SyncCommand.MaxUpdateTime updateTime, int limit) throws JSONException {
        JSONObject request = new JSONObject();
        if(updateTime == null || updateTime.time == 0) {
            request.put("date_from", JSONObject.NULL);
        }else{
            request.put("date_from", SyncUtil.formatMillisec(new Date(updateTime.time)));
        }
        request.put("limit", limit);
        return request;
    }

    public static JSONObject getRequestFull(String table, SyncCommand.MaxUpdateTime updateTime, String guidColumn, String parentIdColumn, boolean isChild, int limit) throws JSONException {

        JSONObject request = new JSONObject();
        request.put("table", table);

        JSONObject from = new JSONObject();

        if(updateTime == null || updateTime.time == 0) {
            from.put("date", JSONObject.NULL);
            from.put("id", JSONObject.NULL);
        }else{
            from.put("date", SyncUtil.formatMillisec(new Date(updateTime.time)));

            if (TextUtils.isEmpty(updateTime.guid)) {
                from.put("id", JSONObject.NULL);
            } else {
                JSONObject whereId = new JSONObject();
                whereId.put(guidColumn, updateTime.guid);
                from.put("id", whereId);
            }
        }

        request.put("from", from);

        if(parentIdColumn != null){
            JSONObject where = new JSONObject();
            where.put(parentIdColumn, isChild ? "CHILD" : "PARENT");
            request.put("where", where);
        }else{
            request.put("where", JSONObject.NULL);
        }

        request.put("limit", limit);
        return request;
    }

    public static JSONObject getHistoryLimitRequest(long limitDate, SyncCommand.MaxUpdateTime updateTime, String guidColumn, int limit) throws JSONException {
        JSONObject request = new JSONObject();

        request.put("limit_date", SyncUtil.formatMillisec(new Date(limitDate)));

        JSONObject from = new JSONObject();

        if(updateTime == null || updateTime.time == 0) {
            from.put("update_time", JSONObject.NULL);
            from.put("id", JSONObject.NULL);
        }else{
            from.put("update_time", SyncUtil.formatMillisec(new Date(updateTime.time)));

            JSONObject whereId = new JSONObject();
            whereId.put(guidColumn, updateTime.guid);
            from.put("id", whereId);
        }

        request.put("from", from);

        request.put("limit", limit);
        return request;
    }

    public static JSONObject getOldActiveOrdersRequest(long limitDate) throws JSONException {
        JSONObject request = new JSONObject();

        request.put("limit_date", SyncUtil.formatMillisec(new Date(limitDate)));

        return request;
    }

}
