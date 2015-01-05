package com.kaching123.tcr.service;

import com.kaching123.tcr.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gdubina on 05/03/14.
 */
public class SingleSqlCommand implements ISqlCommand {

    public static final String JSON_METHOD = "method";
    public static final String JSON_CMD = "cmd";

    private final String method;
    private final String cmd;

    public SingleSqlCommand(String method, String cmd) {
        this.cmd = cmd;
        this.method = method;
    }

    /*public final long shopId;
            public final String sql;
            public final ArrayList<QueryParam> params = new ArrayList<QueryParam>();

            public SingleSqlCommand(long shopId, String sql, ArrayList<QueryParam> params) {
                this.shopId = shopId;
                this.sql = sql;
                this.params.addAll(params);
            }

            public SingleSqlCommand(long shopId, String sql, Iterable<QueryParam>... params) {
                this.shopId = shopId;
                this.sql = sql;
                if (params != null) {
                    for (Iterable<QueryParam> it : params) {
                        for (QueryParam p : it) {
                            this.params.add(p);
                        }
                    }
                }
            }

            public void add(int type, Object value) {
                params.add(new QueryParam(type, value));
            }

            public void add(QueryParam p) {
                params.add(p);
            }
        */
    public String toJson(){
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_METHOD, method);
            json.put(JSON_CMD, cmd);
        } catch (JSONException e) {
            Logger.e("SQL serialization error: " + cmd, e);
        }
        return json.toString();
    }

    public static SingleSqlCommand fromJson(String s) throws JSONException {
        JSONObject json = new JSONObject(s);

        String method = json.getString(JSON_METHOD);
        String cmd = json.getString(JSON_CMD);
        return new SingleSqlCommand(method, cmd);
    }

    public String getCmd() {
        return cmd;
    }

    String getMethod() {
        return method;
    }
}