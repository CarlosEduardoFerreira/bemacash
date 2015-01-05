package com.kaching123.tcr.commands.rest.sync.v1;

import com.kaching123.tcr.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by gdubina on 05/03/14.
 */
public class SingleSqlCommandV1{

    public static final String JSON_SQL = "SQL";
    public static final String JSON_SHOP_ID = "SHOP_ID";
    public static final String JSON_PARAMS = "PARAMS";

    public final long shopId;
    public final String sql;
    public final ArrayList<QueryParamV1> params = new ArrayList<QueryParamV1>();

    public SingleSqlCommandV1(long shopId, String sql, ArrayList<QueryParamV1> params) {
        this.shopId = shopId;
        this.sql = sql;
        this.params.addAll(params);
    }

    public SingleSqlCommandV1(long shopId, String sql, Iterable<QueryParamV1>... params) {
        this.shopId = shopId;
        this.sql = sql;
        if (params != null) {
            for (Iterable<QueryParamV1> it : params) {
                for (QueryParamV1 p : it) {
                    this.params.add(p);
                }
            }
        }
    }

    public void add(int type, Object value) {
        params.add(new QueryParamV1(type, value));
    }

    public void add(QueryParamV1 p) {
        params.add(p);
    }

    public String toJson(){
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_SQL, sql);
            json.put(JSON_SHOP_ID, shopId);
            JSONArray jsonArray = new JSONArray();
            for(QueryParamV1 p : params){
                jsonArray.put(p.toJson());
            }
            json.put(JSON_PARAMS, jsonArray);
        } catch (JSONException e) {
            Logger.e("SQL serialization error: " + sql, e);
        }
        return json.toString();
    }

    public static SingleSqlCommandV1 fromJson(String s) throws JSONException {
        JSONObject json = new JSONObject(s);

        String sql = json.getString(JSON_SQL);
        long shopId = json.getLong(JSON_SHOP_ID);
        JSONArray params = json.getJSONArray(JSON_PARAMS);
        ArrayList<QueryParamV1> queryParams = new ArrayList<QueryParamV1>(params.length());
        for(int i = 0; i < params.length(); i++){
            JSONObject pJson = params.getJSONObject(i);
            queryParams.add(QueryParamV1.fromJson(pJson));
        }
        return new SingleSqlCommandV1(shopId, sql, queryParams);
    }

}