package com.kaching123.tcr.service;

import com.kaching123.tcr.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by gdubina on 05/03/14.
 */
public class BatchSqlCommand implements ISqlCommand {

    public final ArrayList<SingleSqlCommand> sql = new ArrayList<SingleSqlCommand>();

    private final String method;

    public BatchSqlCommand(){
        this.method = "batch";
    }

    public BatchSqlCommand(SingleSqlCommand c) {
        this.method = c.getMethod();
        sql.add(c);
    }

    public BatchSqlCommand(String method) {
        this.method = method;
    }

    public BatchSqlCommand add(SingleSqlCommand sql) {
        this.sql.add(sql);
        return this;
    }

    public BatchSqlCommand add(ISqlCommand sql) {
        if (sql == null)
            return this;
        if (sql.getClass() == SingleSqlCommand.class) {
            add((SingleSqlCommand) sql);
        } else if (sql.getClass() == BatchSqlCommand.class) {
            BatchSqlCommand bSql = (BatchSqlCommand) sql;
            this.sql.addAll(bSql.sql);
        }
        return this;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray operations = new JSONArray();
            for (SingleSqlCommand s : sql) {
                operations.put(new JSONObject(s.getCmd()));
            }
            jsonObject.put("method", method);
            jsonObject.put("operations", operations);
        } catch (JSONException e) {
            Logger.e("SQL serialization error", e);
        }
        return jsonObject.toString();
    }

    public static BatchSqlCommand fromJson(String str) throws JSONException {

        JSONObject json = new JSONObject(str);
        String method = json.getString("method");

        BatchSqlCommand batch = new BatchSqlCommand(method);
        JSONArray operations = json.getJSONArray("operations");
        for (int i = 0; i < operations.length(); i++) {
            batch.add(new SingleSqlCommand(null, operations.getJSONObject(i).toString()));
        }
        return batch;

    }

}