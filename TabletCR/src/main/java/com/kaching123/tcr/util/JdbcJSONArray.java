package com.kaching123.tcr.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gdubina on 20/03/14.
 */
public class JdbcJSONArray extends JSONArray{

    public JdbcJSONArray(String str) throws JSONException {
        super(str);
    }

    public JdbcJSONArray(JSONArray json) throws JSONException {
        super();
        for(int i = 0; i < json.length(); i++){
            put(json.get(i));
        }
    }

    @Override
    public JdbcJSONObject getJSONObject(int index) throws JSONException {
        JSONObject json = super.getJSONObject(index);
        if(json == null)
            return null;
        return new JdbcJSONObject(json);
    }
}
