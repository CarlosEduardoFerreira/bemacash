package com.kaching123.tcr.service;

import com.kaching123.tcr.commands.rest.sync.Sync2Util;

import org.json.JSONException;

import java.sql.Types;
import java.util.Date;

/**
 * Created by gdubina on 05/03/14.
 */
public class QueryParam {

    /*public static final String JSON_TYPE = "TYPE";
    public static final String JSON_OBJECT = "OBJECT";*/

    public int type;
    public Object value;

    public QueryParam(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Object toJson() throws JSONException {
        if (type == Types.TIMESTAMP) {
            if (value == null)
                return null;
            return Sync2Util.format((Date)value);
        }
        return this.value;
    }

    public boolean isNull() {
        return value == null;
    }

    /*public static QueryParam fromJson(JSONObject pJson) throws JSONException {
        int type = pJson.getInt(JSON_TYPE);
        Object value;
        switch (type){
            case Types.DECIMAL:
                value = _decimalCheckScale(pJson.getString(JSON_OBJECT));
                break;
            case Types.BIGINT:
                value = _long(pJson.getString(JSON_OBJECT));
                break;
            case Types.TIMESTAMP:
                value = _date(pJson.getString(JSON_OBJECT));
                break;
            default:
                value = pJson.opt(JSON_OBJECT);
        }
        return new QueryParam(type, value);
    }*/
}
